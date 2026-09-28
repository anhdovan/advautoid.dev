# CloudEvents Webhook Integration

The AutoID Edge Gateway streams RFID events using the CNCF **CloudEvents v1.0** JSON specification. This standard guarantees out-of-the-box interoperability with Kafka, AWS EventBridge, Azure Event Grid, GCP Cloud Pub/Sub, and enterprise webhooks.

---

## 1. CloudEvents Schema

Each HTTP POST payload delivered to your endpoint adheres to:

```json
{
  "specversion": "1.0",
  "id": "e6a2b0c1-3f41-479e-b9b2-a42e5d9c71a0",
  "source": "/plants/plant-01/gateways/gw-dock01",
  "type": "com.beetech.autoid.tag.read",
  "datacontenttype": "application/json",
  "time": "2026-09-28T08:15:30.123Z",
  "data": {
    "readerId": 1,
    "epc": "30142A9BF4000000000F3DD5",
    "rssi": -58,
    "antenna": 2,
    "timestamp": 1774743330123,
    "direction": "OutToIn",
    "confidence": 0.98,
    "velocityMps": 1.42
  }
}
```

---

## 2. Event Types

| Event Type (`type`) | Trigger | Payload Summary |
| :--- | :--- | :--- |
| `com.beetech.autoid.tag.read` | Raw tag read observation | EPC, RSSI, Antenna, Raw Timestamp |
| `com.beetech.autoid.portal.transit` | Direction classification completed | EPC, Direction (`InToOut` / `OutToIn`), Velocity, Confidence |
| `com.beetech.autoid.reader.status` | Reader connected / disconnected | ReaderId, Status (`Connected`, `Error`), ErrorCode |
| `com.beetech.autoid.incident.alarm` | Security or system alarm triggered | AlarmCode, Details, CameraSnapshotUrl |

---

## 3. HMAC-SHA256 Signature Verification

To verify that the webhook originated from your authentic AutoID Gateway, each request includes the header:
```http
X-AutoID-Signature: sha256=d3b07384d113edec49eaa6238ad5ff00 ...
```

### Verification in Node.js / Express
```javascript
const crypto = require('crypto');

function verifyWebhook(req, secret) {
  const signature = req.headers['x-autoid-signature'];
  const expected = 'sha256=' + crypto
    .createHmac('sha256', secret)
    .update(req.rawBody)
    .digest('hex');
  return crypto.timingSafeEqual(Buffer.from(signature), Buffer.from(expected));
}
```
