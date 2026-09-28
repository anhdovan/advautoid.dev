# Developer Guide

The Developer Guide covers architectural foundations, memory structures, algorithms, and driver extension mechanisms inside the **Adv.SmartSdk** engine.

> 🌐 **Live Web Platform**: Visit [https://advautoid.com](https://advautoid.com) | [Documentation Hub](https://advautoid.com/docs)

---

## 📑 Guide Sections

1. [Core Concepts](core-concepts.md)  
   Reader lifecycle, streaming tag models, antenna multiplexing, and async event dispatch.

2. [Native AOT C-ABI Specifications](native-aot-c-abi.md)  
   Under the hood of `AdvSmartSdk.dll` / `libAdvSmartSdk.so`: Unmanaged exports, memory layouts, calling conventions, and cross-language interoperability.

3. [ChaCha20 EPC Anti-Clone Cryptography](epc-cryptography.md)  
   Anti-counterfeiting payload encryption, 96-bit packing into standard Gen2 24-character hexadecimal EPCs.

4. [Zero-Allocation GS1 SGTIN-96 Codec](gs1-sgtin-codec.md)  
   Nanosecond-scale bitwise translation between GTIN barcodes, serial numbers, and binary SGTIN-96 EPC tags.

5. [RSSI Centroid Portal Direction Tracking](direction-detection.md)  
   Trajectory classification algorithm separating inbound arrivals from outbound departures using dual-zone antenna centroid tracking.

6. [Writing Custom Reader Driver Plugins](custom-driver-plugin.md)  
   Extend the SDK with custom serial, TCP, or proprietary reader hardware using the unified Driver SPI.
