# Linux x64 Runtime (`libAdvSmartSdk.so`)

`libAdvSmartSdk.so` is the compiled Linux x64 ELF shared object for the **Beetech Universal RFID SmartSdk** Native AOT engine.

---

## 📥 Direct Download

- **File**: [`libAdvSmartSdk.so`](libAdvSmartSdk.so) (24.1 MB)
- **Direct Raw Download**: [📥 Download `libAdvSmartSdk.so`](https://github.com/anhdovan/advautoid.dev/raw/main/runtimes/linux-x64/libAdvSmartSdk.so)
- **Format**: ELF 64-bit LSB shared object, x86-64, dynamically linked, stripped
- **SHA-256 Checksum**: `61A06A1424CEBD0FF365A51131A4D647086B11BF3028485761F5C937B60C5BFA`

---

## 🚀 Installation on Linux

Copy `libAdvSmartSdk.so` to `/usr/local/lib` or your application directory:

```bash
sudo cp libAdvSmartSdk.so /usr/local/lib/
sudo ldconfig
```

Or set `LD_LIBRARY_PATH`:
```bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$(pwd)
```

---

## 🛠️ Build from Source (via WSL / Linux)

### Prerequisites (Ubuntu / Debian)
```bash
sudo apt-get update
sudo apt-get install -y dotnet-sdk-8.0 clang build-essential zlib1g-dev libkrb5-dev
```

### Compile Command
```bash
dotnet publish Src/Windows/Beetech.Adv.SmartSdk.Native \
  -c Release \
  -r linux-x64 \
  /p:PublishAot=true \
  /p:NativeLib=Shared \
  -o ./dist/linux-x64
```
