/**
 * AutoID JavaScript WebSocket Live Streaming Sample
 * Connects to AutoID Edge Gateway and streams tag read events.
 */

const WebSocket = require('ws');

const GATEWAY_WS_URL = process.env.GATEWAY_WS_URL || 'ws://127.0.0.1:18080/ws/events';

console.log(`[AutoID JS Client] Connecting to: ${GATEWAY_WS_URL}`);

const ws = new WebSocket(GATEWAY_WS_URL);

ws.on('open', () => {
  console.log('[AutoID JS Client] Connected to AutoID Gateway successfully!');
  
  // Send start command for reader 1
  const startCmd = {
    action: 'start',
    readerId: 1
  };
  ws.send(JSON.stringify(startCmd));
  console.log('[AutoID JS Client] Sent start command for Reader 1');
});

ws.on('message', (data) => {
  try {
    const event = JSON.parse(data.toString());
    
    if (event.type === 'tag') {
      const { epc, rssi, antenna, timestamp } = event.payload;
      const timeStr = new Date(timestamp).toLocaleTimeString();
      console.log(`[${timeStr}] TAG READ | EPC: ${epc} | RSSI: ${rssi} dBm | Antenna: ${antenna}`);
    } else if (event.type === 'direction') {
      const { epc, direction, confidence, velocityMps } = event.payload;
      const dirText = direction === 'InToOut' ? '➡️ IN-TO-OUT (Departure)' : '⬅️ OUT-TO-IN (Arrival)';
      console.log(`⭐ [PORTAL TRANSIT] EPC: ${epc} | Direction: ${dirText} | Speed: ${velocityMps} m/s | Confidence: ${(confidence * 100).toFixed(1)}%`);
    } else if (event.type === 'status') {
      console.log(`[STATUS UPDATE] Reader ${event.payload.readerId}: ${event.payload.status}`);
    }
  } catch (err) {
    console.error('Failed to parse frame:', err.message);
  }
});

ws.on('error', (err) => {
  console.error('[AutoID JS Client] WebSocket error:', err.message);
});

ws.on('close', (code, reason) => {
  console.log(`[AutoID JS Client] Connection closed (${code}): ${reason || 'Normal close'}`);
});
