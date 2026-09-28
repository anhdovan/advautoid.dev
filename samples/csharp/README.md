# C# .NET 8 Samples

This directory contains two C# samples demonstrating integration styles for .NET developers:

---

## 1. `Quickstart/` (High-Level Gateway Client)
- Connects to the **AutoID Edge Gateway** over REST and WebSockets.
- Fully asynchronous using `System.Net.Http` and `System.Net.WebSockets`.
- Works on Windows, Linux, and macOS without requiring native binaries locally.

### Run:
```bash
dotnet run --project Quickstart/Quickstart.csproj
```

---

## 2. `NativePInvoke/` (Zero-Overhead Direct C-ABI Interop)
- Directly invokes native unmanaged functions in `AdvSmartSdk.dll` via P/Invoke.
- Demonstrates zero-allocation bitwise GS1 SGTIN codec, ChaCha20 encryption, and direct hardware callbacks.
- Recommended for embedded IPCs, automated machinery, and extreme high-throughput sorting lines (>2,000 tags/sec).

### Run:
```bash
dotnet run --project NativePInvoke/NativePInvoke.csproj
```
