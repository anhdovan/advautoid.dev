# Installation Guide

The AutoID platform can be installed through standard package managers, Docker containers, or direct native binary integration.

---

## 1. Package Manager Installs

### TypeScript / JavaScript (Node.js & Browsers)

```bash
# NPM
npm install @beetech-autoid/smartsdk-client

# Yarn
yarn add @beetech-autoid/smartsdk-client

# PNPM
pnpm add @beetech-autoid/smartsdk-client
```

### Python

```bash
# Install dependencies for REST / Webhook
pip install requests websockets pydantic

# For native C-ABI usage, ctypes is built into standard Python 3.9+!
```

### C# / .NET 8

```bash
# Add reference to the SDK managed wrapper
dotnet add package Beetech.Adv.SmartSdk --version 1.0.0
```

---

## 2. Docker Edge Gateway Deployment

The AutoID Edge Gateway is distributed as a multi-arch container image (`linux/amd64`, `linux/arm64`, `windows/amd64`).

### Quick Start with Docker Run

```bash
docker run -d \
  --name autoid-gateway \
  -p 18080:18080 \
  -v autoid-data:/app/data \
  -e GATEWAY_PORT=18080 \
  -e DEV_LICENSE_BYPASS=true \
  --restart unless-stopped \
  beetech/autoid-gateway:latest
```

### Production `docker-compose.yml`

```yaml
version: '3.8'

services:
  autoid-gateway:
    image: beetech/autoid-gateway:latest
    container_name: autoid-gateway
    restart: always
    network_mode: "host" # Recommended for reader multicast discovery & LLRP
    environment:
      - GATEWAY_PORT=18080
      - LOG_LEVEL=Information
      - DEV_LICENSE_BYPASS=false
      - LICENSE_FILE=/app/license/smartsdk.lic
    volumes:
      - ./config/readers.json:/app/config/readers.json:ro
      - ./config/sinks.json:/app/config/sinks.json:ro
      - ./license:/app/license:ro
      - ./spool:/app/spool
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:18080/api/health"]
      interval: 10s
      timeout: 3s
      retries: 3
```

---

## 3. Native Engine Library Integration (`AdvSmartSdk`)

For native C++, Python, and Java developers using the compiled shared library:

### Windows x64
- Ensure `AdvSmartSdk.dll` is located in:
  - Your executable directory, OR
  - Windows `System32` or directory present in `PATH`.
- Requirements: Visual C++ 2015-2022 Redistributable (x64).

### Linux x64
- Ensure `libAdvSmartSdk.so` is in `/usr/local/lib` or `LD_LIBRARY_PATH`.
- Requirements: `glibc >= 2.31`, `libstdc++6`.

---

## 4. Verification

Verify your installation with curl:

```bash
curl http://localhost:18080/api/health
```

Expected JSON response:
```json
{
  "status": "Healthy",
  "version": "1.0.0",
  "engine": "Native AOT C-ABI",
  "activeReaders": 0,
  "uptimeSeconds": 142
}
```
