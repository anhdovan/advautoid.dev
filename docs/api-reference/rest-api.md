# Edge Gateway REST API Reference

The AutoID Edge Gateway exposes a lightweight REST API on default port `18080`.

---

## 1. System Endpoints

### `GET /api/health`
Checks gateway health, engine status, and active reader count.

**Response `200 OK`**:
```json
{
  "status": "Healthy",
  "version": "1.0.0",
  "engine": "Native AOT C-ABI",
  "activeReaders": 2,
  "uptimeSeconds": 18230
}
```

### `GET /api/fingerprint`
Retrieves machine hardware node-locking fingerprint.

**Response `200 OK`**:
```json
{
  "hardwareFingerprint": "BT-7F3A-89E2-11C0-994B"
}
```

---

## 2. Reader Management Endpoints

### `GET /api/readers`
Returns list of all configured and discovered readers.

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
  }
]
```

### `POST /api/readers`
Registers a new reader dynamically.

**Request Body**:
```json
{
  "readerId": 3,
  "driverId": "urovo",
  "address": "192.168.1.116:8088",
  "powerDbm": 27.0,
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
