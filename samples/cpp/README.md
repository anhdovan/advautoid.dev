# C++17 Samples

This directory provides native C++17 samples that dynamically bind to the **Adv.SmartSdk** unmanaged shared library (`AdvSmartSdk.dll` / `libAdvSmartSdk.so`).

---

## Files

- `AdvSmartSdk.h`: Production-ready C++ header with RAII `AdvSmartSdkReader` wrapper and unmanaged C-ABI dynamic loading.
- `main.cpp`: Complete executable demonstrating dynamic loading, hardware fingerprinting, ChaCha20 encryption, SGTIN-96 conversion, and real-time inventory streaming.
- `CMakeLists.txt`: Cross-platform build script for CMake (Windows MSVC, Linux GCC/Clang).
- `build.bat`: Quick compile batch script using Visual Studio MSVC command line.

---

## Building & Running

### Using CMake (Recommended)
```bash
cmake -B build
cmake --build build --config Release
./build/Release/autoid_sample
```

### Using MSVC Developer Command Prompt (Windows)
```cmd
build.bat
autoid_sample.exe
```
