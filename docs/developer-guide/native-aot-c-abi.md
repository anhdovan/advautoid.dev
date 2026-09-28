# Native AOT C-ABI Specifications

The core engine of **Beetech Adv.SmartSdk** (`AdvSmartSdk.dll` on Windows, `libAdvSmartSdk.so` on Linux) is compiled using .NET 8 Ahead-of-Time (**Native AOT**).

---

## 1. Why Native AOT?

1. **Anti-Decompilation Security**:
   - Compiles directly to native machine code (`IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR = 0`).
   - Zero Intermediate Language (IL) bytecode or metadata headers remain.
   - Decompilation tools (ILSpy, dnSpy, dotPeek, JADX) cannot decompile or reverse-engineer the proprietary algorithms.
2. **Zero Overhead C-ABI**:
   - Exposes clean, unmanaged C functions using `[UnmanagedCallersOnly(CallConvs = [typeof(CallConvCdecl)])]`.
   - Call directly from C++, Python (`ctypes`), Java 21 (`java.lang.foreign`), Go (`cgo`), and Rust (`extern "C"`).
3. **Nanosecond Startup & Deterministic Memory**:
   - Zero JIT warm-up time. Instantaneous startup on embedded hardware.

---

## 2. Memory Layout & Calling Conventions

- **Calling Convention**: `__cdecl` (standard C calling convention).
- **String Encoding**: UTF-8 null-terminated (`const char*`). Buffer outputs require caller-allocated buffers with size parameters to prevent buffer overflows.
- **Pointers**: `void*` opaque handles for readers and direction detectors.

```c
// Native Tag Read Callback Signature
typedef void (*FnTagReadCallback)(
    void* readerHandle,
    const char* epcUtf8,
    int rssi,
    int antPort,
    long long timestampMs
);

// Native Direction Event Callback Signature
typedef void (*FnDirectionCallback)(
    void* detectorHandle,
    const char* epcUtf8,
    int direction,       // 1 = In-to-Out (Departure), 2 = Out-to-In (Arrival)
    double confidence    // 0.0 to 1.0 statistical score
);
```

---

## 3. Core Export Table

| Export Symbol | Signature | Description |
| :--- | :--- | :--- |
| `Sdk_GetHardwareFingerprint` | `int(char* outFp, int outLen)` | Generates unique machine hardware fingerprint string |
| `Sdk_ValidateLicense` | `int(const char* licPath, char* err, int errLen)` | Cryptographically verifies RSA/ECDSA license signature |
| `Sdk_SetDevLicenseBypass` | `void(int enabled)` | Toggles developer mode bypass (1 = enabled, 0 = enforce) |
| `Sdk_EncryptEpc` | `int(int64 tagId, const char* type, char* outHex, int outLen)` | ChaCha20 EPC encryption |
| `Sdk_DecryptEpc` | `int(const char* hex, int64* tagId, char* outType, int outLen)` | ChaCha20 EPC decryption |
| `Sdk_GetSgtin` | `int(const char* gtin, const char* sn, int pfxLen, char* outHex, int outLen)` | GS1 SGTIN-96 bitwise encoding |
| `Sdk_TranslateEpcToGtin`| `int(const char* hex, char* outGtin, int gLen, char* outSn, int sLen)` | GS1 SGTIN-96 bitwise decoding |
| `Sdk_CreateReader` | `int(const char* drv, int id, const char* addr, void** outH)` | Instantiates reader driver instance |
| `Sdk_RegisterTagCallback`| `int(void* handle, FnTagReadCallback cb)` | Registers unmanaged C tag streaming callback |
| `Sdk_Connect` | `int(void* handle)` | Opens hardware connection |
| `Sdk_Disconnect` | `int(void* handle)` | Closes hardware connection |
| `Sdk_StartInventory` | `int(void* handle)` | Begins RF tag read stream |
| `Sdk_StopInventory` | `int(void* handle)` | Stops RF tag read stream |
| `Sdk_FreeReader` | `int(void* handle)` | Releases unmanaged driver memory |
| `Sdk_CreateDirectionDetector` | `int(int mode, void** outH)` | Initializes RSSI centroid direction tracking engine |
| `Sdk_ProcessTagRead` | `int(void* handle, const char* epc, int port, double rssi, int64 ts)` | Feeds tag read into trajectory model |
| `Sdk_FreeDirectionDetector` | `int(void* handle)` | Releases direction tracker memory |
