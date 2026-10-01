# 5-Minute Quickstart

This walkthrough takes you from zero to streaming RFID tags and direction events in under five minutes.

We provide two tracks:
- **Track A**: Web & Microservice Track (JavaScript / TypeScript with Edge Gateway)
- **Track B**: Native Embedded Track (Python / C++ with unmanaged engine)

---

## 🛠️ Step 0: Environment Bootstrap (Test vs. Production)

Before running client applications, you must bring up the **AutoID Edge Gateway** runtime and configure your devices.

### Quick Comparison: Test vs. Production

| Step | Test / Sandbox Environment | Production / Staging Environment |
| :--- | :--- | :--- |
| **1. Gateway Runtime** | `docker run` with `DEV_LICENSE_BYPASS=true` | `docker compose` with host networking & mounted volumes |
| **2. Hardware Fingerprint** | Bypassed or retrieved via `/api/fingerprint` | Query node fingerprint via `/api/fingerprint` or SDK API |
| **3. License Subscription** | Instant free trial from [advautoid.com/trial](https://advautoid.com/trial) or bypass flag | Issue node-locked RSA-2048 commercial license from portal |
| **4. Import & Check** | Auto-bypassed or verify via `/api/health` | Mount `smartsdk.lic` into `/app/license/`, verify via `/api/health` |
| **5. Device Registration** | Virtual Mock Simulator (`driverId: "mock"`) | Physical readers (Impinj R700, Zebra FX9600, Urovo FR2000) |

---

### Step 0.1: Run Edge Gateway Container

#### Option A: Quick Test/Dev Mode (Instant Start, No License Required)
```bash
docker run -d \
  --name autoid-gateway \
  -p 18080:18080 \
  -e GATEWAY_PORT=18080 \
  -e DEV_LICENSE_BYPASS=true \
  --restart unless-stopped \
  beetech/autoid-gateway:latest
```

#### Option B: Production Mode (Licensed & Persistent Volumes)
```bash
# 1. Create host directories for config and license
mkdir -p ./config ./license ./spool

# 2. Run container in production mode
docker run -d \
  --name autoid-gateway \
  -p 18080:18080 \
  -v $(pwd)/config:/app/config:ro \
  -v $(pwd)/license:/app/license:ro \
  -v $(pwd)/spool:/app/spool \
  -e GATEWAY_PORT=18080 \
  -e DEV_LICENSE_BYPASS=false \
  -e LICENSE_FILE=/app/license/smartsdk.lic \
  --restart always \
  beetech/autoid-gateway:latest
```

---

### Step 0.2: Get Hardware Fingerprint & Subscribe for License

1. **Query Host Machine Fingerprint**:
   ```bash
   curl http://localhost:18080/api/fingerprint
   ```
   *Example response:*
   ```json
   {
     "hardwareFingerprint": "BT-7F3A-89E2-11C0-994B"
   }
   ```

2. **Subscribe / Generate License**:
   - For evaluation: Visit the self-service portal at **[https://advautoid.com/trial](https://advautoid.com/trial)**.
   - Enter your fingerprint `BT-XXXX-XXXX-XXXX-XXXX` and download your signed `smartsdk.lic`.
   - For enterprise production licenses: Request activation via your SI Partner Portal.

3. **Import & Verify License**:
   - Place `smartsdk.lic` into `./license/smartsdk.lic` on the host.
   - Check gateway health and license validation status:
     ```bash
     curl http://localhost:18080/api/health
     ```
     *Response verifies valid license:*
     ```json
     {
       "status": "Healthy",
       "version": "1.0.0",
       "engine": "Native AOT C-ABI",
       "licenseStatus": "Valid",
       "licensedTo": "Acme Logistics",
       "allowedReaders": 8,
       "activeReaders": 0
     }
     ```

---

### Step 0.3: Add Devices (Mock Simulator or Physical Readers)

The Gateway manages reader instances. You can register a reader dynamically via REST or preconfigure it in `./config/readers.json`.

#### Add a Virtual Mock Reader (For immediate testing without physical hardware):
```bash
curl -X POST http://localhost:18080/api/readers \
  -H "Content-Type: application/json" \
  -d '{
    "readerId": 1,
    "driverId": "mock",
    "name": "Portal Simulator 01",
    "address": "virtual:loopback",
    "powerDbm": 30.0,
    "antennas": [1, 2]
  }'
```

#### Add a Physical Reader (e.g., Impinj Speedway/R700 or Zebra FX9600):
```bash
curl -X POST http://localhost:18080/api/readers \
  -H "Content-Type: application/json" \
  -d '{
    "readerId": 2,
    "driverId": "impinj",
    "name": "Dock Door 01 - Impinj R700",
    "address": "192.168.1.150:5084",
    "powerDbm": 31.5,
    "antennas": [1, 2, 3, 4]
  }'
```

*Verify registered readers:*
```bash
curl http://localhost:18080/api/readers
```

---

## Track A: Web & Microservice (JavaScript / TypeScript)

Once your gateway is running (from Step 0), connect with the official client SDK:

### 1. Install Client SDK

```bash
npm install @beetech-autoid/smartsdk-client
```

### 2. Connect and Stream

Create a file named `app.ts` (or `app.js`):

```typescript
import { SmartSdkClient } from '@beetech-autoid/smartsdk-client';

async function main() {
  // Connect to Edge Gateway (defaults to http://127.0.0.1:18080)
  const client = new SmartSdkClient({
    baseUrl: 'http://127.0.0.1:18080',
    autoReconnect: true
  });

  // 1. Verify health and license
  const health = await client.getHealth();
  console.log(`Gateway Status: ${health.status}, Version: ${health.version}, License: ${health.licenseStatus}`);

  // 2. Query configured readers
  const readers = await client.getReaders();
  console.log(`Discovered ${readers.length} configured readers`);

  // 3. Start real-time tag subscription
  const unsubscribe = client.subscribeTags((tag) => {
    console.log(`[RFID] EPC: ${tag.epc} | RSSI: ${tag.rssi} dBm | Ant: ${tag.antenna} | Timestamp: ${tag.timestamp}`);
    if (tag.direction) {
      console.log(`  --> Portal Direction: ${tag.direction} (Confidence: ${Math.round((tag.confidence ?? 0) * 100)}%)`);
    }
  });

  // 4. Start reader #1 (mock or physical reader registered in Step 0.3)
  await client.startReader(1);
  console.log('Reader #1 started. Listening for tags...');

  // Run for 30 seconds
  await new Promise(r => setTimeout(r, 30000));
  unsubscribe();
  await client.stopReader(1);
}

main().catch(console.error);
```

### 3. Run the App

```bash
npx ts-node app.ts
```

---

## Track B: Native Embedded (Python)

If you are developing directly on industrial IPCs, PLCs, or embedded Linux systems without running an HTTP gateway, use the native C-ABI shared library (`AdvSmartSdk.dll` on Windows or `libAdvSmartSdk.so` on Linux).

### 1. Prerequisites

- Python 3.9+
- `AdvSmartSdk.dll` or `libAdvSmartSdk.so` placed in your application directory or system PATH ([Download Runtimes](../../runtimes/)).

### 2. Script: `quickstart.py`

```python
import time
from smart_sdk import SmartSdk

# 1. Initialize native engine
sdk = SmartSdk("AdvSmartSdk.dll")

# 2. Hardware Fingerprint & Licensing
# Retrieve the unique node-locked machine fingerprint:
fp = sdk.get_hardware_fingerprint()
print(f"Machine Hardware Fingerprint: {fp}")

# FOR TESTING: Enable dev license bypass
sdk.set_dev_license_bypass(True)

# FOR PRODUCTION: Validate official RSA-2048 license file:
# is_valid, err_msg = sdk.validate_license("smartsdk.lic")
# if not is_valid:
#     raise RuntimeError(f"License check failed: {err_msg}")

# 3. Create and start a Reader (Mock loopback simulator or physical 'impinj'/'zebra')
def on_tag_read(tag):
    print(f"[NATIVE TAG] EPC: {tag.epc} | RSSI: {tag.rssi} dBm | Antenna: {tag.antenna_port}")

with sdk.create_reader("mock", reader_id=1, address="virtual:loopback") as reader:
    reader.start_inventory(on_tag_read)
    print("Streaming tags from reader #1 for 5 seconds...")
    time.sleep(5)
    reader.stop_inventory()

print("Quickstart completed successfully.")
```

### 3. Run

```bash
python quickstart.py
```

---

## What Just Happened?

1. **Environment Bootstrapped**: Gateway container was launched with test bypass or production cryptographic license.
2. **Node Fingerprint & Licensing**: System proved identity via hardware fingerprinting (`BT-XXXX-XXXX-XXXX-XXXX`).
3. **Device Provisioned**: Either a high-fidelity virtual mock reader or a physical Impinj/Zebra device was activated.
4. **Hardware Abstraction**: Whether using Mock or physical Impinj R700 / Zebra FX9600, your code called the exact same `startInventory` and callback signatures.
5. **Zero Polling**: Tags arrive via push events as soon as the reader detects RF backscatter in the field.

---

## Next Steps

- [Deep-dive into Edge Gateway Docker Deployment](../integrator-guide/edge-gateway-deployment.md)
- [Licensing & Cryptographic Node Locking](../integrator-guide/licensing-and-fingerprinting.md)
- [Set up physical readers & antenna calibration](hardware-setup.md)
- [Understand Core Concepts & Event Lifecycle](../developer-guide/core-concepts.md)

