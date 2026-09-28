# Linux x64 Runtime (`libAdvSmartSdk.so`)

`libAdvSmartSdk.so` is the Linux x64 ELF shared object for the **Beetech Universal RFID SmartSdk** Native AOT engine.

---

## Download Options

1. **GitHub Releases**: Download pre-packaged `.tar.gz` archive from [Releases](https://github.com/anhdovan/advautoid.dev/releases).
2. **Docker Edge Gateway**: Built directly into the `beetech/autoid-gateway` container image running on Ubuntu 22.04 LTS / Debian 12.

---

## Build from Source (Linux x64)

### Prerequisites
```bash
sudo apt-get update
sudo apt-get install -y clang zlib1g-dev libkrb5-dev
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

Copy the generated `libAdvSmartSdk.so` into `/usr/local/lib` or your application's directory:
```bash
sudo cp ./dist/linux-x64/libAdvSmartSdk.so /usr/local/lib/
sudo ldconfig
```
