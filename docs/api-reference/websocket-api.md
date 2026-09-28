# WebSocket Tag Streaming Protocol

The AutoID Edge Gateway streams real-time tag reads and portal direction events over a high-throughput WebSocket channel at `ws://<gateway-host>:18080/ws/events`.

---

## 1. Connecting

### Handshake
```http
GET /ws/events HTTP/1.1
Host: 127.0.0.1:18080
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
```

---

## 2. Inbound Frame Schemas (Server to Client)

### Tag Read Event (`tag`)
Emitted each time an RFID tag is detected in the RF field:

```json
{
  "type": "tag",
  "payload": {
    "readerId": 1,
    "epc": "E280116060000214B001ABCD",
    "rssi": -54,
    "antenna": 2,
    "timestamp": 1774742400123
  }
}
```

### Portal Direction Event (`direction`)
Emitted when a tag trajectory across dual antennas is classified:

```json
{
  "type": "direction",
  "payload": {
    "epc": "E280116060000214B001ABCD",
    "direction": "OutToIn",
    "confidence": 0.96,
    "velocityMps": 1.25,
    "firstSeenTimestamp": 1774742400100,
    "lastSeenTimestamp": 1774742401350
  }
}
```

### Reader Status Change Event (`status`)
Emitted upon hardware connection, disconnection, or faults:

```json
{
  "type": "status",
  "payload": {
    "readerId": 1,
    "status": "InventoryRunning",
    "message": "Antenna 1-4 active"
  }
}
```

---

## 3. Client Control Commands (Client to Server)

You can send JSON frames over the WebSocket to control readers or adjust filters:

### Start Reader
```json
{
  "action": "start",
  "readerId": 1
}
```

### Stop Reader
```json
{
  "action": "stop",
  "readerId": 1
}
```

### Apply EPC Prefix Filter
```json
{
  "action": "setFilter",
  "epcPrefix": "E280"
}
```
