# Edge Gateway REST API Reference

The AutoID Edge Gateway exposes a lightweight REST API on default port `18080`.

---

## 1. System Endpoints

### `GET /api/health`
Checks gateway health, engine status, node license state, and active reader count.

**Response `200 OK`**:
```json
{
  "status": "Healthy",
  "version": "1.0.0",
  "engine": "Native AOT C-ABI",
  "licenseStatus": "Valid",
  "licensedTo": "Acme Industrial Logistics",
  "allowedReaders": 8,
  "activeReaders": 2,
  "uptimeSeconds": 18230
}
```

### `GET /api/fingerprint`
Retrieves machine hardware node-locking fingerprint for license activation.

**Response `200 OK`**:
```json
{
  "hardwareFingerprint": "BT-7F3A-89E2-11C0-994B",
  "status": "Ready"
}
```

---

## 2. Reader Management Endpoints

### `GET /api/readers`
Returns list of all configured, active, and discovered readers.

**Response `200 OK`**:
```json
[
  {
    "readerId": 1,
    "driverId": "impinj",
    "name": "Dock Door 01",
    "address": "192.168.1.150:5084",
    "status": "InventoryRunning",
    "powerDbm": 30.0,
    "antennas": [1, 2, 3, 4]
  },
  {
    "readerId": 2,
    "driverId": "mock",
    "name": "Virtual Simulator",
    "address": "virtual:loopback",
    "status": "Ready",
    "powerDbm": 30.0,
    "antennas": [1, 2]
  }
]
```

### `POST /api/readers`
Registers a new reader dynamically (supports physical drivers: `impinj`, `zebra`, `urovo`, `caen`, `unitech`, or virtual simulator `mock`).

**Request Body (Physical Reader)**:
```json
{
  "readerId": 3,
  "driverId": "urovo",
  "name": "Gate 3 FR2000",
  "address": "192.168.1.116:8088",
  "powerDbm": 27.0,
  "antennas": [1, 2]
}
```

**Request Body (Mock Simulator)**:
```json
{
  "readerId": 4,
  "driverId": "mock",
  "name": "Test Portal Simulator",
  "address": "virtual:loopback",
  "powerDbm": 30.0,
  "antennas": [1, 2]
}
```

### `POST /api/readers/{id}/start`
Starts tag inventory reading on reader `{id}`.

### `POST /api/readers/{id}/stop`
Stops tag inventory reading on reader `{id}`.

### `PUT /api/readers/{id}/settings`
Updates reader output power, sensitivity, or antenna enablement.

**Request Body**:
```json
{
  "powerDbm": 31.5,
  "sensitivityDbm": -68.0,
  "activeAntennas": [1, 3]
}
```

---

## 3. Cryptography & Encoding Utility Endpoints

### `POST /api/codec/sgtin/encode`
Encodes GTIN and serial into SGTIN-96 EPC.

**Request Body**:
```json
{
  "gtin": "00860004123451",
  "serial": "998877",
  "companyPrefixLength": 7
}
```
**Response `200 OK`**:
```json
{
  "epcHex": "30142A9BF4000000000F3DD5"
}
```

### `POST /api/codec/sgtin/decode`
Decodes SGTIN-96 EPC into GTIN and serial.

**Request Body**:
```json
{
  "epcHex": "30142A9BF4000000000F3DD5"
}
```
**Response `200 OK`**:
```json
{
  "gtin": "00860004123451",
  "serial": "998877"
}
```
