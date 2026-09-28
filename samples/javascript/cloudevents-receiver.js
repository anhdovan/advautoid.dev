/**
 * AutoID CloudEvents Webhook Receiver (Express / Node.js)
 * Demonstrates receiving CNCF CloudEvents v1.0 and validating HMAC signatures.
 */

const express = require('express');
const crypto = require('crypto');

const app = express();
const PORT = process.env.PORT || 8080;
const WEBHOOK_SECRET = process.env.WEBHOOK_SECRET || 'sample-shared-secret';

// Capture raw body for HMAC verification
app.use(express.json({
  verify: (req, res, buf) => {
    req.rawBody = buf;
  }
}));

app.post('/api/webhook/autoid', (req, res) => {
  const signature = req.headers['x-autoid-signature'];
  
  if (WEBHOOK_SECRET && signature) {
    const expected = 'sha256=' + crypto
      .createHmac('sha256', WEBHOOK_SECRET)
      .update(req.rawBody)
      .digest('hex');

    if (signature !== expected) {
      console.warn('[Webhook] Invalid HMAC signature! Potential spoofing attempt.');
      return res.status(401).json({ error: 'Invalid signature' });
    }
  }

  const cloudEvent = req.body;
  console.log(`\n================= CLOUDEVENT RECEIVED =================`);
  console.log(`Event ID   : ${cloudEvent.id}`);
  console.log(`Source     : ${cloudEvent.source}`);
  console.log(`Type       : ${cloudEvent.type}`);
  console.log(`Timestamp  : ${cloudEvent.time}`);
  console.log(`Payload    :`, JSON.stringify(cloudEvent.data, null, 2));
  console.log(`=======================================================\n`);

  // Handle specific event types
  if (cloudEvent.type === 'com.beetech.autoid.tag.read') {
    // Process tag read
  } else if (cloudEvent.type === 'com.beetech.autoid.portal.transit') {
    console.log(`🚨 Dock Transit Alert: EPC ${cloudEvent.data.epc} went ${cloudEvent.data.direction}`);
  }

  res.status(200).json({ status: 'ACK', receivedAt: new Date().toISOString() });
});

app.listen(PORT, () => {
  console.log(`[AutoID Webhook Receiver] Listening on http://localhost:${PORT}/api/webhook/autoid`);
});
