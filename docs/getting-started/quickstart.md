# 5-Minute Quickstart
 
This walkthrough takes you from zero to streaming RFID tags and direction events in under five minutes.

We provide three tracks:
- **Track 0 (Fastest - 60s)**: Zero-Hardware Scaffolding (`npx create-autoid-app` or `dotnet new autoid-gate`)
- **Track A**: Web & Microservice Track (JavaScript / TypeScript with Edge Gateway)
- **Track B**: Native Embedded Track (Python / C++ with unmanaged native engine)

---

## ⚡ Track 0: 60-Second Zero-Hardware Scaffolding

If you do not have physical RFID readers or edge containers running, you can generate and run a fully working project immediately:

### Option A: Interactive Scaffolding Wizard (Web Radar, Node Worker, C# Gate)
```bash
npx create-autoid-app my-rfid-project
```
Select from:
1. `web-radar-dashboard`: Modern Vite + Canvas Radar visualizer with sound effect and live simulated tag inventory.
2. `node-stream-worker`: Node.js high-throughput deduplication stream consumer.
3. `dotnet-gate-service`: C# .NET 9 Background Worker for warehouse portal gates.

### Option B: .NET Warehouse Gate Service Template
```bash
dotnet new install Beetech.OmniAutoId.Templates
dotnet new autoid-gate -n WarehousePortalGate
cd WarehousePortalGate && dotnet run
```

---

## 🛠️ Step 0: Environment Bootstrap (Test vs. Production)

Before running client applications, bring up the **AutoID Edge Gateway** container and configure your readers.

### Quick Comparison: Test vs. Production

| Step | Test / Sandbox Environment | Production / Staging Environment |
| :--- | :--- | :--- |
| **1. Gateway Runtime** | `docker run` with `DEV_LICENSE_BYPASS=true` | `docker compose` with host networking & mounted volumes |
| **2. Hardware Fingerprint** | Retrieved via `/api/fingerprint` (auto-bypassed) | Query node fingerprint via `/api/fingerprint` or SDK API |
| **3. License Subscription** | Instant free trial from [advautoid.com/trial](https://advautoid.com/trial) or bypass flag | Issue node-locked RSA-2048 commercial license from portal |
| **4. Import & Check** | Auto-bypassed or verify via `/api/health` | Mount `smartsdk.lic` into container, verify via `/api/health` |
| **5. Device Registration** | Virtual Mock Simulator (`driverId: "mock"`) | Physical readers (Impinj R700, Zebra FX9600, Urovo FR2000) |

---

### Step 0.1: Run Edge Gateway Container

#### Option A: Quick Test / Dev Mode (Instant Start, No License Required)

=== "Linux / macOS (Bash)"

    ```bash
    docker run -d \
      --name autoid-gateway \
      -p 18080:18080 \
      -e GATEWAY_PORT=18080 \
      -e DEV_LICENSE_BYPASS=true \
      --restart unless-stopped \
      anhdv74/adv-rfid-gateway:latest
    ```

=== "Windows (PowerShell)"

    ```powershell
    docker run -d `
      --name autoid-gateway `
      -p 18080:18080 `
      -e GATEWAY_PORT=18080 `
      -e DEV_LICENSE_BYPASS=true `
      --restart unless-stopped `
      anhdv74/adv-rfid-gateway:latest
    ```

=== "Windows (Command Prompt)"

    ```cmd
    docker run -d ^
      --name autoid-gateway ^
      -p 18080:18080 ^
      -e GATEWAY_PORT=18080 ^
      -e DEV_LICENSE_BYPASS=true ^
      --restart unless-stopped ^
      anhdv74/adv-rfid-gateway:latest
    ```

> [!TIP]
> If you are using an older image release that binds internally to port 19090, map `-p 18080:19090` instead of `-p 18080:18080`.

---

#### Option B: Production Mode (Licensed & Persistent Volumes)

=== "Linux / macOS (Bash)"

    ```bash
    # 1. Create host directories for config, license, and spool buffer
    mkdir -p ./config ./license ./spool

    # 2. Run container in production mode
    docker run -d \
      --name autoid-gateway \
      -p 18080:18080 \
      -v "$(pwd)/config:/app/data" \
      -v "$(pwd)/license:/app/license:ro" \
      -v "$(pwd)/spool:/app/spool" \
      -e GATEWAY_PORT=18080 \
      -e BEETECH_LICENSE_PATH=/app/license/smartsdk.lic \
      -e DEV_LICENSE_BYPASS=false \
      --restart always \
      anhdv74/adv-rfid-gateway:latest
    ```

=== "Windows (PowerShell)"

    ```powershell
    # 1. Create host directories
    New-Item -ItemType Directory -Force -Path .\config, .\license, .\spool

    # 2. Run container in production mode
    docker run -d `
      --name autoid-gateway `
      -p 18080:18080 `
      -v "${PWD}/config:/app/data" `
      -v "${PWD}/license:/app/license:ro" `
      -v "${PWD}/spool:/app/spool" `
      -e GATEWAY_PORT=18080 `
      -e BEETECH_LICENSE_PATH=/app/license/smartsdk.lic `
      -e DEV_LICENSE_BYPASS=false `
      --restart always `
      anhdv74/adv-rfid-gateway:latest
    ```

=== "Windows (Command Prompt)"

    ```cmd
    mkdir config license spool

    docker run -d ^
      --name autoid-gateway ^
      -p 18080:18080 ^
      -v "%cd%/config:/app/data" ^
      -v "%cd%/license:/app/license:ro" ^
      -v "%cd%/spool:/app/spool" ^
      -e GATEWAY_PORT=18080 ^
      -e BEETECH_LICENSE_PATH=/app/license/smartsdk.lic ^
      -e DEV_LICENSE_BYPASS=false ^
      --restart always ^
      anhdv74/adv-rfid-gateway:latest
    ```

---

### Step 0.2: Get Hardware Fingerprint & Verify Health

#### 1. Query Host Machine Fingerprint

=== "Linux / macOS (Bash)"

    ```bash
    curl http://localhost:18080/api/fingerprint
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-RestMethod -Uri "http://localhost:18080/api/fingerprint"
    # or: curl.exe http://localhost:18080/api/fingerprint
    ```

=== "Windows (Command Prompt)"

    ```cmd
    curl http://localhost:18080/api/fingerprint
    ```

*Expected response:*
```json
{
  "hardwareFingerprint": "BT-6CFE-6F8D-F6B6-BE4B"
}
```

#### 2. Subscribe / Generate License

- **For evaluation**: Visit the self-service trial portal at **[https://advautoid.com/trial](https://advautoid.com/trial)**.
- Enter your fingerprint `BT-XXXX-XXXX-XXXX-XXXX` and download your signed `smartsdk.lic`.
- **For enterprise production**: Contact your SI Partner portal for node-locked RSA-2048 licenses.

#### 3. Check Health and License Status

=== "Linux / macOS (Bash)"

    ```bash
    curl http://localhost:18080/api/health
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-RestMethod -Uri "http://localhost:18080/api/health"
    ```

=== "Windows (Command Prompt)"

    ```cmd
    curl http://localhost:18080/api/health
    ```

*Expected response:*
```json
{
  "status": "Healthy",
  "version": "1.0.0",
  "engine": "Native AOT C-ABI",
  "licenseStatus": "Trial/Bypassed",
  "licensedTo": "Evaluation / Sandbox",
  "allowedReaders": 32,
  "activeReaders": 0
}
```

---

### Step 0.3: Add Devices (Mock Simulator or Physical Readers)

You can register reader instances dynamically via the REST API or configure them statically.

#### Add a Virtual Mock Reader (For immediate testing without physical hardware):

=== "Linux / macOS (Bash)"

    ```bash
    curl -X POST http://localhost:18080/api/readers \
      -H "Content-Type: application/json" \
      -d '{
        "readerId": 10,
        "driverId": "mock",
        "name": "Portal Simulator 01",
        "address": "virtual:loopback",
        "powerDbm": 30.0
      }'
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-RestMethod -Uri "http://localhost:18080/api/readers" `
      -Method Post `
      -ContentType "application/json" `
      -Body '{"readerId": 10, "driverId": "mock", "name": "Portal Simulator 01", "address": "virtual:loopback", "powerDbm": 30.0}'
    ```

=== "Windows (Command Prompt)"

    ```cmd
    curl -X POST http://localhost:18080/api/readers -H "Content-Type: application/json" -d "{\"readerId\": 10, \"driverId\": \"mock\", \"name\": \"Portal Simulator 01\", \"address\": \"virtual:loopback\", \"powerDbm\": 30.0}"
    ```

#### Add a Physical Reader (e.g., Impinj Speedway/R700 or Zebra FX9600):

=== "Linux / macOS (Bash)"

    ```bash
    curl -X POST http://localhost:18080/api/readers \
      -H "Content-Type: application/json" \
      -d '{
        "readerId": 20,
        "driverId": "impinj-r700",
        "name": "Dock Door 01 - Impinj R700",
        "address": "192.168.1.150:5084",
        "power": 31.5
      }'
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-RestMethod -Uri "http://localhost:18080/api/readers" `
      -Method Post `
      -ContentType "application/json" `
      -Body '{"readerId": 20, "driverId": "impinj-r700", "name": "Dock Door 01 - Impinj R700", "address": "192.168.1.150:5084", "power": 31.5}'
    ```

#### Verify Registered Readers:

=== "Linux / macOS (Bash)"

    ```bash
    curl http://localhost:18080/api/readers
    ```

=== "Windows (PowerShell)"

    ```powershell
    Invoke-RestMethod -Uri "http://localhost:18080/api/readers"
    ```

=== "Windows (Command Prompt)"

    ```cmd
    curl http://localhost:18080/api/readers
    ```

---

## Track A: Web & Microservice (JavaScript / TypeScript)

Once your Edge Gateway is running (from Step 0), connect using the official client SDK:

### 1. Install Client SDK

In your project directory:

```bash
npm install @beetech-autoid/smartsdk-client
```

### 2. Connect and Stream

Create a file named `app.ts` (or `app.js`):

```typescript
import { SmartSdkClient } from '@beetech-autoid/smartsdk-client';

async function main() {
  // Connect to Edge Gateway (defaults to port 18080)
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

  // 3. Start real-time tag subscription over WebSocket
  const unsubscribe = client.subscribeTags((tag) => {
    console.log(`[RFID] EPC: ${tag.epc} | RSSI: ${tag.rssi} dBm | Ant: ${tag.antenna} | Timestamp: ${tag.timestamp}`);
    if (tag.direction) {
      console.log(`  --> Direction: ${tag.direction}`);
    }
  });

  // 4. Start reader #10 (mock simulator registered in Step 0.3)
  const readerId = 10;
  console.log(`Starting Reader #${readerId}...`);
  await client.startReader(readerId);
  console.log(`Reader #${readerId} started. Streaming tags for 10 seconds...`);

  // Stream tags for 10 seconds
  await new Promise((resolve) => setTimeout(resolve, 10000));

  // 5. Cleanup
  console.log(`Stopping Reader #${readerId}...`);
  await client.stopReader(readerId);
  unsubscribe();
  client.disconnect();
  console.log('Done!');
}

main().catch(console.error);
```

### 3. Run the App

=== "Linux / macOS (Bash)"

    ```bash
    npx ts-node app.ts
    # or for plain JavaScript:
    # node app.js
    ```

=== "Windows (PowerShell & CMD)"

    ```powershell
    npx ts-node app.ts
    # or for plain JavaScript:
    # node app.js
    ```

---

## Track B: Native Embedded (Python)

If you are developing directly on industrial IPCs, PLCs, or embedded Linux systems without running an HTTP gateway, use the native C-ABI shared library (`AdvSmartSdk.dll` on Windows or `libAdvSmartSdk.so` on Linux).

### 1. Prerequisites & Setup

- **Python 3.8+** (64-bit)
- Place the following two files in your application folder:
  1. **`smart_sdk.py`**: The official Python ctypes binding wrapper ([Download from samples/python/smart_sdk.py](https://github.com/anhdovan/advautoid.dev/blob/main/samples/python/smart_sdk.py)).
  2. **Native Runtime Library** ([Download Runtimes](https://github.com/anhdovan/advautoid.dev/tree/main/runtimes)):
     - **Windows**: `AdvSmartSdk.dll` (from `runtimes/win-x64/`)
     - **Linux**: `libAdvSmartSdk.so` (from `runtimes/linux-x64/`)

=== "Linux / macOS (Bash)"

    ```bash
    # Download wrapper and native runtime into current directory
    curl -O https://raw.githubusercontent.com/anhdovan/advautoid.dev/main/samples/python/smart_sdk.py
    curl -O https://raw.githubusercontent.com/anhdovan/advautoid.dev/main/runtimes/linux-x64/libAdvSmartSdk.so
    chmod +x libAdvSmartSdk.so
    ```

=== "Windows (PowerShell)"

    ```powershell
    # Download wrapper and native runtime into current directory
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/anhdovan/advautoid.dev/main/samples/python/smart_sdk.py" -OutFile "smart_sdk.py"
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/anhdovan/advautoid.dev/main/runtimes/win-x64/AdvSmartSdk.dll" -OutFile "AdvSmartSdk.dll"
    ```

---

### 2. Script: `quickstart.py`

Create a file named `quickstart.py`:

```python
import os
import sys
import time
from smart_sdk import SmartSdk

# 1. Initialize native engine (auto-detects Windows DLL vs Linux SO)
lib_file = "AdvSmartSdk.dll" if sys.platform == "win32" else "libAdvSmartSdk.so"
sdk = SmartSdk(lib_file)

# 2. Hardware Fingerprint & Licensing
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
    reader.disconnect()

print("Quickstart completed successfully.")
```

### 3. Run

=== "Linux / macOS (Bash)"

    ```bash
    python3 quickstart.py
    ```

=== "Windows (PowerShell & CMD)"

    ```powershell
    python quickstart.py
    ```

*Expected output:*
```
Machine Hardware Fingerprint: BT-FB2E-F36E-9492-9788
Streaming tags from reader #1 for 5 seconds...
[NATIVE TAG] EPC: E20000000000000000000001 | RSSI: -50 dBm | Antenna: 1
[NATIVE TAG] EPC: E20000000000000000000003 | RSSI: -50 dBm | Antenna: 1
[NATIVE TAG] EPC: E20000000000000000000006 | RSSI: -50 dBm | Antenna: 1
Quickstart completed successfully.
```

---

## 🔍 Troubleshooting & Common Gotchas

### 1. Port Mapping & Connection Refused
- If `curl http://localhost:18080/api/health` returns `Connection refused`, check your docker port mapping:
  Ensure `-p 18080:18080` is used with `-e GATEWAY_PORT=18080`.
  If you are running the default image without `GATEWAY_PORT`, use `-p 18080:19090` since the internal service defaults to port 19090.

### 2. Windows PowerShell Quoting with `curl.exe`
- When using PowerShell, passing single-quoted JSON strings to `curl.exe` can cause JSON syntax errors due to quote-stripping. Use PowerShell's native **`Invoke-RestMethod`** for clean, seamless API calls on Windows.

### 3. 409 Conflict: Reader ID Already Exists
- Reader ID `1` is configured by default on startup. When registering a new reader via REST, use an unused ID such as `10`, `20`, or unregister the existing reader first via `DELETE /api/v1/readers/1`.

### 4. Linux Permissions for Native Shared Library
- On Linux, ensure `libAdvSmartSdk.so` has execute permissions (`chmod +x libAdvSmartSdk.so`) and that your user has access to `libdl` and standard `glibc` 2.31+.

---

## Next Steps

- [Deep-dive into Edge Gateway Docker Deployment](../integrator-guide/edge-gateway-deployment.md)
- [Licensing & Cryptographic Node Locking](../integrator-guide/licensing-and-fingerprinting.md)
- [Set up physical readers & antenna calibration](hardware-setup.md)
- [Understand Core Concepts & Event Lifecycle](../developer-guide/core-concepts.md)
