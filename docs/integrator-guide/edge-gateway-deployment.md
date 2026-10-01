# Docker Edge Gateway Deployment & SI Commissioning Guide

The **AutoID Edge Gateway** encapsulates reader driver management, local direction analytics, local write-ahead log (WAL) persistence, and cloud dispatch into an industrial-grade container.

This guide provides System Integrators (SIs) and DevOps teams with a step-by-step runbook for deploying, licensing, and commissioning production and test environments.

---

## 📋 SI Deployment Checklist (End-to-End)

| Phase | Milestone | Action |
| :--- | :--- | :--- |
| **Phase 1** | **Host Prep & Directory Layout** | Create volume directories (`config/`, `license/`, `spool/`) and `docker-compose.yml` |
| **Phase 2** | **Initial Boot & Fingerprint Query** | Launch container; extract cryptographic hardware fingerprint via `/api/fingerprint` |
| **Phase 3** | **License Subscription & Issuance** | Submit fingerprint to [advautoid.com/trial](https://advautoid.com/trial) or SI portal; receive signed `smartsdk.lic` |
| **Phase 4** | **License Import & Health Check** | Mount `smartsdk.lic` into container; verify `/api/health` indicates `licenseStatus: "Valid"` |
| **Phase 5** | **Device Provisioning** | Add physical readers (Impinj, Zebra, Urovo) or mock simulator in `config/readers.json` or REST API |
| **Phase 6** | **Upstream Sinks & Validation** | Configure CloudEvents webhooks in `config/sinks.json`; verify live tag stream |

---

## 1. Directory Structure on Host

Prepare the deployment directory on your edge server or industrial IPC (e.g., `/opt/autoid-gateway` on Linux or `C:\AutoIdGateway` on Windows):

```
/opt/autoid-gateway/
├── docker-compose.yml
├── config/
│   ├── readers.json          # Hardware reader definitions & antenna tuning
│   └── sinks.json            # Upstream CloudEvents webhooks & MQTT brokers
├── license/
│   └── smartsdk.lic          # Cryptographically signed RSA-2048 node-locked license
└── spool/                    # Persistent WAL buffer (survives network disconnects)
```

Create the directory structure:
```bash
sudo mkdir -p /opt/autoid-gateway/{config,license,spool}
cd /opt/autoid-gateway
```

---

## 2. Docker Compose Configuration

Create `/opt/autoid-gateway/docker-compose.yml`:

```yaml
version: '3.8'

services:
  autoid-gateway:
    image: anhdv74/adv-rfid-gateway:latest
    container_name: autoid-gateway
    restart: always
    # "host" network is highly recommended for LLRP reader discovery & low latency
    network_mode: "host"
    environment:
      - GATEWAY_PORT=18080
      - LOG_LEVEL=Information
      - DEV_LICENSE_BYPASS=false            # Set to 'true' for test/sandbox environments
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

> [!NOTE]
> For **Test / Staging Environments** where physical licensing is pending, set `DEV_LICENSE_BYPASS=true`. This enables all engine features and mock simulators without requiring a license file.

---

## 3. Step-by-Step Commissioning Runbook

### Step 1: Initial Boot to Extract Hardware Fingerprint

Start the container in initial mode:
```bash
docker compose up -d
```

Query the machine's hardware fingerprint via the REST API:
```bash
curl http://localhost:18080/api/fingerprint
```
*Expected JSON output:*
```json
{
  "hardwareFingerprint": "BT-7F3A-89E2-11C0-994B",
  "status": "AwaitingLicense"
}
```

---

### Step 2: Subscribe & Order Cryptographic License

1. Copy the hardware fingerprint string (e.g. `BT-7F3A-89E2-11C0-994B`).
2. **Evaluation / Pilot**: Navigate to **[https://advautoid.com/trial](https://advautoid.com/trial)** to generate an instant 30-day trial license file.
3. **Commercial Production**: Submit the fingerprint through your SI enterprise portal or contact Beetech support for your permanent or subscription license.
4. Save the downloaded license as `smartsdk.lic`.

---

### Step 3: Import & Verify License

1. Copy `smartsdk.lic` to the host directory:
   ```bash
   cp /path/to/downloaded/smartsdk.lic /opt/autoid-gateway/license/smartsdk.lic
   chmod 644 /opt/autoid-gateway/license/smartsdk.lic
   ```
2. Restart the gateway service:
   ```bash
   docker compose restart autoid-gateway
   ```
3. Inspect gateway health to confirm license activation:
   ```bash
   curl http://localhost:18080/api/health
   ```
   *Expected response:*
   ```json
   {
     "status": "Healthy",
     "version": "1.0.0",
     "engine": "Native AOT C-ABI",
     "licenseStatus": "Valid",
     "licensedTo": "Enterprise Logistics Ltd",
     "allowedReaders": 8,
     "activeReaders": 0,
     "uptimeSeconds": 12
   }
   ```

---

### Step 4: Device Provisioning (`config/readers.json`)

Configure physical reader connections in `config/readers.json`:

```json
[
  {
    "readerId": 1,
    "driverId": "impinj",
    "name": "Dock Door 01 - R700",
    "address": "192.168.1.150:5084",
    "powerDbm": 30.0,
    "sensitivityDbm": -70.0,
    "antennas": [1, 2, 3, 4],
    "autoStart": true
  },
  {
    "readerId": 2,
    "driverId": "zebra",
    "name": "Conveyor 02 - FX9600",
    "address": "192.168.1.151:5084",
    "powerDbm": 28.0,
    "antennas": [1, 2],
    "autoStart": true
  },
  {
    "readerId": 3,
    "driverId": "mock",
    "name": "Line 3 Virtual Simulator",
    "address": "virtual:loopback",
    "powerDbm": 30.0,
    "antennas": [1, 2],
    "autoStart": true
  }
]
```

#### Dynamic Device Addition via REST API:
You can also provision or modify readers at runtime without restarting the container:
```bash
curl -X POST http://localhost:18080/api/readers \
  -H "Content-Type: application/json" \
  -d '{
    "readerId": 4,
    "driverId": "urovo",
    "name": "Inbound Gate 04 - FR2000",
    "address": "192.168.1.160:8088",
    "powerDbm": 27.0,
    "antennas": [1, 2]
  }'
```

Verify active readers:
```bash
curl http://localhost:18080/api/readers
```

---

### Step 5: Upstream Dispatch Sinks (`config/sinks.json`)

Configure CloudEvents webhooks to deliver filtered tag and portal direction events into your ERP, WMS, or cloud broker:

```json
[
  {
    "id": "wms-webhook",
    "type": "cloudevents-http",
    "url": "https://wms.enterprise.internal/api/rfid/webhook",
    "secretHeader": "X-AutoID-Signature",
    "secretKey": "enterprise-hmac-sha256-key",
    "batchSize": 100,
    "flushIntervalMs": 500,
    "retryMaxAttempts": 10
  }
]
```

---

## 4. Verification & Diagnostics

1. **Verify Reader Start**:
   ```bash
   curl -X POST http://localhost:18080/api/readers/1/start
   ```
2. **Monitor Live WebSocket Tag Stream**:
   Connect with `websocat` or any WebSocket tool:
   ```bash
   websocat ws://localhost:18080/ws/tags
   ```
3. **Check Container Logs**:
   ```bash
   docker compose logs -f autoid-gateway
   ```

---

## 5. Next Steps

- [Review Licensing & Hardware Fingerprinting details](licensing-and-fingerprinting.md)
- [Configure CloudEvents Webhook sinks](cloudevents-webhook.md)
- [Check Supported Hardware Matrix](supported-hardware-matrix.md)

