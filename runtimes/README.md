# AutoID Prebuilt Native SDK Runtimes (`AdvSmartSdk`)

This directory contains prebuilt, standalone Ahead-of-Time (**Native AOT**) unmanaged shared libraries for **Beetech Adv.SmartSdk**.

These libraries expose a pure C-ABI and execute as native machine code without any .NET CLR runtime or JIT dependency.

---

## 📦 Available Native Binaries

| Platform | Target Architecture | Binary File | Format | Status | Download Link |
| :--- | :--- | :--- | :--- | :---: | :--- |
| **Windows** | x64 (AMD64 / Intel 64) | [`AdvSmartSdk.dll`](win-x64/AdvSmartSdk.dll) | PE32+ DLL | **Ready** | [📥 Download `AdvSmartSdk.dll`](win-x64/AdvSmartSdk.dll?raw=true) |
| **Linux** | x64 (glibc >= 2.31) | [`libAdvSmartSdk.so`](linux-x64/libAdvSmartSdk.so) | ELF64 Shared Object | **Ready** | [📥 Download `libAdvSmartSdk.so`](linux-x64/libAdvSmartSdk.so?raw=true) |
| **Android** | arm64-v8a / armeabi-v7a | `libsmartsdk.so` | Native Android JNI | **Release** | Via Android SDK AAR / GitHub Releases |

---

## 🔍 Binary Integrity & Verification

### Windows x64: `AdvSmartSdk.dll`
- **File**: `runtimes/win-x64/AdvSmartSdk.dll`
- **Size**: ~21.4 MB
- **Compilation**: .NET 8 Native AOT (`PublishAot=true`, `NativeLib=Shared`)
- **Header**: PE32+ x64 Unmanaged Shared Library (`IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR = 0`)
- **SHA-256**: `E3292E0346B0AA07EA30A71AA0F29F403BA298E1E570BE324A9E7ECE305718ED`

To verify locally on Windows (PowerShell):
```powershell
Get-FileHash -Path runtimes/win-x64/AdvSmartSdk.dll -Algorithm SHA256
```

### Linux x64: `libAdvSmartSdk.so`
- **File**: `runtimes/linux-x64/libAdvSmartSdk.so`
- **Size**: ~24.1 MB
- **Compilation**: .NET 8 Native AOT compiled under Ubuntu 24.04 (WSL) with Clang 18
- **Format**: ELF 64-bit LSB shared object, x86-64, dynamically linked, stripped
- **SHA-256**: `61A06A1424CEBD0FF365A51131A4D647086B11BF3028485761F5C937B60C5BFA`

To verify on Linux:
```bash
sha256sum runtimes/linux-x64/libAdvSmartSdk.so
```

---

## 🚀 Quick Usage in Polyglot Samples

### C++ (`samples/cpp/`)
Place `AdvSmartSdk.dll` in your executable directory, or let `AdvSmartSdk.h` auto-resolve it from `../../runtimes/win-x64/AdvSmartSdk.dll`.

### Python (`samples/python/`)
```python
from smart_sdk import SmartSdk

# Auto-resolves from runtimes/win-x64/AdvSmartSdk.dll
sdk = SmartSdk("../../runtimes/win-x64/AdvSmartSdk.dll")
```

### C# P/Invoke (`samples/csharp/NativePInvoke/`)
Copy `AdvSmartSdk.dll` to your `bin/Release/net8.0/` output directory.

### Java 21 (`samples/java/`)
Pass the path to `SmartSdk`:
```java
SmartSdk sdk = new SmartSdk(Path.of("../../runtimes/win-x64/AdvSmartSdk.dll"));
```

---

## 🐧 Building `libAdvSmartSdk.so` for Linux x64

To compile `libAdvSmartSdk.so` on Linux or via Docker:

```bash
# Clone the core engine repository
git clone https://github.com/anhdovan/Adv.SmartSdk.git
cd Adv.SmartSdk

# Publish via Native AOT on Linux x64
dotnet publish Src/Windows/Beetech.Adv.SmartSdk.Native \
  -c Release \
  -r linux-x64 \
  /p:PublishAot=true \
  /p:NativeLib=Shared

# Output binary:
# Src/Windows/Beetech.Adv.SmartSdk.Native/bin/Release/net8.0/linux-x64/publish/libAdvSmartSdk.so
```
