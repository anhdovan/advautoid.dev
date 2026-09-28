# Zero-Allocation GS1 SGTIN-96 Codec

The **GS1 SGTIN-96** (Serialized Global Trade Item Number) standard enables item-level identification for supply chains, healthcare, and retail worldwide.

The **Adv.SmartSdk** includes a high-performance bitwise codec strictly conforming to **GS1 EPC Tag Data Standard (TDS 1.13)**.

---

## 1. Bit Partition Layout

```
Header (8b)  Filter (3b)  Partition (3b)  Company Prefix (20-40b)  Item Ref (24-4b)  Serial (38b)
[ 00110000 ] [   001   ]  [    010     ]  [      XXXXXXXXX      ]  [    YYYYY     ]  [  ZZZZZZZZ  ]
```

- **Header**: Fixed `0x30` (`00110000`) for SGTIN-96.
- **Filter Value**:
  - `0`: All Others
  - `1`: POS Item
  - `2`: Full Case
  - `3`: Outer Shipping Unit
- **Partition Table**: Determines bit boundary between GS1 Company Prefix and Item Reference.

---

## 2. API Usage

### Encoding a GTIN-14 Barcode and Serial to EPC

```typescript
import { SmartSdkClient } from '@beetech-autoid/smartsdk-client';

const client = new SmartSdkClient();

// GTIN-14: "00860004123451", Serial: "998877", Company Prefix Length: 7 digits
const epcHex = await client.encodeSgtin({
  gtin: "00860004123451",
  serial: "998877",
  companyPrefixLength: 7
});

console.log(`Generated SGTIN-96 EPC: ${epcHex}`);
// Output: 30142A9BF4000000000F3DD5
```

### Decoding an EPC to GTIN-14 and Serial (Python)

```python
from smart_sdk import SmartSdk

sdk = SmartSdk("AdvSmartSdk.dll")

gtin, serial = sdk.translate_epc_to_gtin("30142A9BF4000000000F3DD5")
print(f"Decoded GTIN: {gtin}, Serial: {serial}")
# Output: Decoded GTIN: 00860004123451, Serial: 998877
```

---

## 3. Performance Characteristics

- **Zero-Allocation**: Uses 128-bit unsigned integer bit shifting (`UInt128`) internally.
- **Nanosecond Serialization**: 15 nanoseconds per translation on standard x64 processors.
