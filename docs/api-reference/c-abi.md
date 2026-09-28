# Native C-ABI Reference

This document describes the native C functions exported by `AdvSmartSdk.dll` (Windows x64) and `libAdvSmartSdk.so` (Linux x64).

---

## Calling Convention & Types

- Calling Convention: `__cdecl`
- Char Encoding: Null-terminated UTF-8
- Return Codes: `0` = Success (`ADV_SUCCESS`), `< 0` = Error (`ADV_ERR_*`)

---

## 1. System & Licensing

### `Sdk_GetHardwareFingerprint`
```c
int Sdk_GetHardwareFingerprint(char* outFpUtf8, int outLen);
```
- **Description**: Populates caller buffer with machine fingerprint string (`BT-XXXX-XXXX-XXXX-XXXX`).
- **Returns**: `0` on success, or negative error code if buffer is too small.

### `Sdk_ValidateLicense`
```c
int Sdk_ValidateLicense(const char* licensePathUtf8, char* outErrorUtf8, int errorLen);
```
- **Description**: Verifies digital signature and machine fingerprint against license file.
- **Returns**: `0` on valid license, `1` if invalid.

### `Sdk_SetDevLicenseBypass`
```c
void Sdk_SetDevLicenseBypass(int enabled);
```
- **Description**: Passes `1` to enable dev bypass or `0` to enforce licensing.

---

## 2. Cryptography & GS1 SGTIN

### `Sdk_EncryptEpc`
```c
int Sdk_EncryptEpc(long long tagId, const char* typeUtf8, char* outEpcHex, int outLen);
```
- **Description**: ChaCha20 encryption of `tagId` and `type` into a 24-character hexadecimal EPC.

### `Sdk_DecryptEpc`
```c
int Sdk_DecryptEpc(const char* epcHexUtf8, long long* outTagId, char* outTypeUtf8, int typeLen);
```
- **Description**: ChaCha20 decryption and MAC verification of EPC.

### `Sdk_GetSgtin`
```c
int Sdk_GetSgtin(const char* gtinUtf8, const char* serialUtf8, int companyPrefixLength, char* outEpcHex, int outLen);
```
- **Description**: Encodes GTIN-14 barcode and serial into GS1 SGTIN-96 hex EPC.

### `Sdk_TranslateEpcToGtin`
```c
int Sdk_TranslateEpcToGtin(const char* epcHexUtf8, char* outGtinUtf8, int gtinLen, char* outSerialUtf8, int serialLen);
```
- **Description**: Decodes GS1 SGTIN-96 hex EPC into GTIN-14 barcode and serial string.

---

## 3. Reader Control & Streaming

### `FnTagReadCallback`
```c
typedef void (*FnTagReadCallback)(
    void* readerHandle,
    const char* epcUtf8,
    int rssi,
    int antPort,
    long long timestampMs
);
```

### `Sdk_CreateReader`
```c
int Sdk_CreateReader(const char* driverIdUtf8, int readerId, const char* addressUtf8, void** outHandle);
```
- `driverIdUtf8`: `"impinj"`, `"zebra"`, `"urovo"`, `"caen"`, `"chainway"`, `"unitech"`, or `"mock"`.
- `outHandle`: Receives opaque reader pointer.

### `Sdk_RegisterTagCallback`
```c
int Sdk_RegisterTagCallback(void* readerHandle, FnTagReadCallback callback);
```

### `Sdk_Connect` & `Sdk_Disconnect`
```c
int Sdk_Connect(void* readerHandle);
int Sdk_Disconnect(void* readerHandle);
```

### `Sdk_StartInventory` & `Sdk_StopInventory`
```c
int Sdk_StartInventory(void* readerHandle);
int Sdk_StopInventory(void* readerHandle);
```

### `Sdk_FreeReader`
```c
int Sdk_FreeReader(void* readerHandle);
```

---

## 4. Direction Detector

### `FnDirectionCallback`
```c
typedef void (*FnDirectionCallback)(
    void* detectorHandle,
    const char* epcUtf8,
    int direction,       // 1 = InToOut, 2 = OutToIn
    double confidence    // 0.0 .. 1.0
);
```

### `Sdk_CreateDirectionDetector`
```c
int Sdk_CreateDirectionDetector(int setupMode, void** outHandle);
```

### `Sdk_RegisterDirectionCallback`
```c
int Sdk_RegisterDirectionCallback(void* detectorHandle, FnDirectionCallback callback);
```

### `Sdk_ProcessTagRead`
```c
int Sdk_ProcessTagRead(void* detectorHandle, const char* epcUtf8, int antPort, double rssi, long long timestampMs);
```

### `Sdk_FreeDirectionDetector`
```c
int Sdk_FreeDirectionDetector(void* detectorHandle);
```
