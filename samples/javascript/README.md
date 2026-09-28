# JavaScript Samples

This directory provides JavaScript and Node.js samples for integrating with the **AutoID Edge Gateway**.

---

## Files

- `index.js`: Node.js WebSocket client streaming live tag read frames and direction events.
- `cloudevents-receiver.js`: Express.js server receiving CNCF CloudEvents webhooks with HMAC-SHA256 signature verification.
- `web-portal-monitor.html`: Standalone browser dashboard with live tag table and transit metrics.

---

## Quick Start

### 1. Install Dependencies
```bash
npm install
```

### 2. Run WebSocket Stream Client
```bash
npm start
```

### 3. Run CloudEvents Webhook Receiver
```bash
npm run webhook
```

### 4. Run Browser Portal Monitor
Simply open `web-portal-monitor.html` in Chrome, Firefox, or Edge.
Ensure the AutoID Gateway is running on `http://127.0.0.1:18080`.
