# Java 21+ Samples (Foreign Function & Memory API)

This directory provides modern Java samples utilizing Java 21 **Foreign Function & Memory (FFM) API** (`java.lang.foreign`).

No legacy JNI wrappers, C++ bridging, or JNA overhead required! Java calls the unmanaged Native AOT shared library (`AdvSmartSdk.dll` / `libAdvSmartSdk.so`) directly with native C performance.

---

## Files

- `com/beetech/adv/smartsdk/SmartSdk.java`: FFM wrapper implementing dynamic downcalls to the native C-ABI, upcall stubs for native callbacks, and memory arenas.
- `ReaderApiSample.java`: Interactive console application allowing real-time reader selection, license bypass, tag inventory aggregation, and RSSI tracking.
- `compile_and_run.bat`: Windows batch script for compiling and executing with `--enable-preview --enable-native-access=ALL-UNNAMED`.

---

## Requirements

- **Java JDK 21** or later (OpenJDK, Oracle, Temurin).
- `AdvSmartSdk.dll` (Windows) or `libAdvSmartSdk.so` (Linux).

---

## Compiling & Running

```bash
# Compile
javac --enable-preview --release 21 -d bin com/beetech/adv/smartsdk/*.java ReaderApiSample.java

# Run
java --enable-preview --enable-native-access=ALL-UNNAMED -cp bin ReaderApiSample
```
