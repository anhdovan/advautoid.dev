import { TagReadItem, DirectionEvent, TransitDirection } from './types';

/**
 * Client-Side RSSI Centroid Direction Classifier
 * Useful when raw antenna reads are streamed to the frontend or cloud worker.
 */
export class ClientPortalTracker {
  private windowMs: number;
  private observations: Map<string, Array<{ antenna: number; rssi: number; timestamp: number }>> = new Map();

  constructor(windowMs: number = 2500) {
    this.windowMs = windowMs;
  }

  public feed(read: TagReadItem): DirectionEvent | null {
    const now = read.timestamp;
    if (!this.observations.has(read.epc)) {
      this.observations.set(read.epc, []);
    }

    const list = this.observations.get(read.epc)!;
    list.push({ antenna: read.antenna, rssi: read.rssi, timestamp: now });

    // Prune old reads
    const valid = list.filter(item => now - item.timestamp <= this.windowMs);
    this.observations.set(read.epc, valid);

    if (valid.length < 5) return null; // Need minimum confidence observations

    // Inside zone: Ant 1 & 3 (+1.0), Outside zone: Ant 2 & 4 (-1.0)
    let sumWeight = 0;
    let sumWeightedPos = 0;
    const firstHalf = valid.slice(0, Math.floor(valid.length / 2));
    const secondHalf = valid.slice(Math.floor(valid.length / 2));

    const getCentroid = (items: typeof valid) => {
      let linSum = 0;
      let posSum = 0;
      for (const item of items) {
        const lin = Math.pow(10, item.rssi / 10);
        const pos = (item.antenna === 1 || item.antenna === 3) ? 1.0 : -1.0;
        linSum += lin;
        posSum += lin * pos;
      }
      return linSum > 0 ? posSum / linSum : 0;
    };

    const c1 = getCentroid(firstHalf);
    const c2 = getCentroid(secondHalf);
    const delta = c2 - c1;

    let dir: TransitDirection = 'Unknown';
    if (delta > 0.4) dir = 'OutToIn'; // Moving from outside (-1) to inside (+1)
    else if (delta < -0.4) dir = 'InToOut'; // Moving from inside (+1) to outside (-1)

    if (dir === 'Unknown') return null;

    return {
      epc: read.epc,
      direction: dir,
      confidence: Math.min(1.0, Math.abs(delta)),
      firstSeenTimestamp: valid[0].timestamp,
      lastSeenTimestamp: valid[valid.length - 1].timestamp
    };
  }
}

// Example usage
const tracker = new ClientPortalTracker();
const mockReads: TagReadItem[] = [
  { epc: '30142A9BF4000000000F3DD5', rssi: -52, antenna: 2, timestamp: 1000 },
  { epc: '30142A9BF4000000000F3DD5', rssi: -50, antenna: 2, timestamp: 1200 },
  { epc: '30142A9BF4000000000F3DD5', rssi: -58, antenna: 1, timestamp: 1400 },
  { epc: '30142A9BF4000000000F3DD5', rssi: -51, antenna: 1, timestamp: 1600 },
  { epc: '30142A9BF4000000000F3DD5', rssi: -48, antenna: 1, timestamp: 1800 }
];

for (const r of mockReads) {
  const result = tracker.feed(r);
  if (result) {
    console.log(`Detected Transit for EPC: ${result.epc} -> Direction: ${result.direction} (Confidence: ${(result.confidence * 100).toFixed(1)}%)`);
  }
}
