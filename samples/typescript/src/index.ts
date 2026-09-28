import { TagReadItem, DirectionEvent, ReaderInfo, GatewayHealth } from './types';
import WebSocket from 'ws';

/**
 * Enterprise AutoID Client in TypeScript
 */
export class AutoIdClient {
  private ws: WebSocket | null = null;
  private isConnected = false;

  constructor(private baseUrl: string = 'http://127.0.0.1:18080') {}

  public async getHealth(): Promise<GatewayHealth> {
    const res = await fetch(`${this.baseUrl}/api/health`);
    if (!res.ok) throw new Error(`Health check failed: ${res.statusText}`);
    return (await res.json()) as GatewayHealth;
  }

  public async getReaders(): Promise<ReaderInfo[]> {
    const res = await fetch(`${this.baseUrl}/api/readers`);
    if (!res.ok) throw new Error(`Failed to fetch readers: ${res.statusText}`);
    return (await res.json()) as ReaderInfo[];
  }

  public async startReader(readerId: number): Promise<void> {
    const res = await fetch(`${this.baseUrl}/api/readers/${readerId}/start`, { method: 'POST' });
    if (!res.ok) throw new Error(`Failed to start reader ${readerId}`);
  }

  public subscribe(callbacks: {
    onTag?: (tag: TagReadItem) => void;
    onDirection?: (dir: DirectionEvent) => void;
  }): () => void {
    const wsUrl = this.baseUrl.replace(/^http/, 'ws') + '/ws/events';
    this.ws = new WebSocket(wsUrl);

    this.ws.on('open', () => {
      this.isConnected = true;
      console.log(`[AutoIdClient] WebSocket connected to ${wsUrl}`);
    });

    this.ws.on('message', (data: WebSocket.RawData) => {
      try {
        const frame = JSON.parse(data.toString());
        if (frame.type === 'tag' && callbacks.onTag) {
          callbacks.onTag(frame.payload as TagReadItem);
        } else if (frame.type === 'direction' && callbacks.onDirection) {
          callbacks.onDirection(frame.payload as DirectionEvent);
        }
      } catch (e) {
        console.error('Failed to parse frame:', e);
      }
    });

    return () => {
      if (this.ws) {
        this.ws.close();
        this.ws = null;
      }
    };
  }
}

// Runnable demonstration
async function run() {
  const client = new AutoIdClient();
  try {
    const health = await client.getHealth();
    console.log(`Gateway Status: ${health.status} (${health.engine})`);

    const unsubscribe = client.subscribe({
      onTag: (tag) => console.log(`[TAG] EPC=${tag.epc} RSSI=${tag.rssi} Ant=${tag.antenna}`),
      onDirection: (dir) => console.log(`[TRANSIT] EPC=${dir.epc} Dir=${dir.direction} Conf=${dir.confidence}`)
    });

    // Run for 15 seconds
    setTimeout(() => {
      unsubscribe();
      console.log('Finished streaming.');
      process.exit(0);
    }, 15000);
  } catch (err: any) {
    console.error('Demo error (is gateway running?):', err.message);
  }
}

if (require.main === module) {
  run();
}
