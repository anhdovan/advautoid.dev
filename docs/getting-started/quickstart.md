# 5-Minute Quickstart

This walkthrough takes you from zero to streaming RFID tags and direction events in under five minutes.

We provide two tracks:
- **Track A**: Web & Microservice Track (JavaScript / TypeScript with Edge Gateway)
- **Track B**: Native Embedded Track (Python / C++ with unmanaged engine)

---

## Track A: Web & Microservice (JavaScript / TypeScript)

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

  // Verify health
  const health = await client.getHealth();
  console.log(`Gateway Status: ${health.status}, Version: ${health.version}`);

  // Query connected readers
  const readers = await client.getReaders();
  console.log(`Discovered ${readers.length} connected readers`);

  // Start real-time tag subscription
  const unsubscribe = client.subscribeTags((tag) => {
    console.log(`[RFID] EPC: ${tag.epc} | RSSI: ${tag.rssi} dBm | Ant: ${tag.antenna} | Timestamp: ${tag.timestamp}`);
    if (tag.direction) {
      console.log(`  --> Portal Direction: ${tag.direction} (Confidence: ${Math.round((tag.confidence ?? 0) * 100)}%)`);
    }
  });

  // Start reader #1
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

If you are developing directly on industrial IPCs or embedded edge systems without running an HTTP gateway, use the native C-ABI shared library (`AdvSmartSdk.dll` or `libAdvSmartSdk.so`).

### 1. Prerequisites

- Python 3.9+
- `AdvSmartSdk.dll` placed in your application directory or system PATH.

### 2. Script: `quickstart.py`

```python
import time
from smart_sdk import SmartSdk

# 1. Initialize native engine
sdk = SmartSdk("AdvSmartSdk.dll")

# 2. In development, enable license bypass
sdk.set_dev_license_bypass(True)

# 3. Read hardware fingerprint
fp = sdk.get_hardware_fingerprint()
print(f"Machine Fingerprint: {fp}")

# 4. Create and start a Mock Reader (or use 'impinj', 'zebra', 'urovo')
def on_tag_read(tag):
    print(f"[NATIVE TAG] EPC: {tag.epc} | RSSI: {tag.rssi} dBm | Antenna: {tag.antenna_port}")

with sdk.create_reader("mock", reader_id=1, address="virtual:loopback") as reader:
    reader.start_inventory(on_tag_read)
    print("Streaming tags from mock reader for 5 seconds...")
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

1. **Hardware Abstraction**: Whether using Mock or physical Impinj R700 / Zebra FX9600, your code called the exact same `startInventory` and callback signatures.
2. **Zero Polling**: Tags arrive via push events as soon as the reader detects RF backscatter in the field.
3. **Deterministic Timestamps**: Millisecond-accurate hardware timestamps allow precise speed and transit calculation.

---

## Next Steps

- [Install production drivers and gateway](installation.md)
- [Set up physical readers](hardware-setup.md)
- [Understand Core Concepts](../developer-guide/core-concepts.md)
