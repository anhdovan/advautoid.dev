# Docker Edge Gateway Deployment

The AutoID Edge Gateway encapsulates reader driver management, local direction analytics, and cloud dispatch into an appliance-grade container.

---

## 1. Directory Structure on Host

```
/opt/autoid-gateway/
├── docker-compose.yml
├── config/
│   ├── readers.json
│   └── sinks.json
├── license/
│   └── smartsdk.lic
└── spool/
```

---

## 2. Configuration Files

### `config/readers.json`
Specifies physical readers managed by this gateway instance:

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
  }
]
```

### `config/sinks.json`
Specifies upstream destinations for tag reads and portal direction events:

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

## 3. Launching Gateway

```bash
cd /opt/autoid-gateway
docker compose up -d
```

Verify service logs:
```bash
docker compose logs -f autoid-gateway
```
