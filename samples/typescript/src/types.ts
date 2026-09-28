export type TransitDirection = 'InToOut' | 'OutToIn' | 'Unknown';

export interface TagReadItem {
  epc: string;
  rssi: number;
  antenna: number;
  timestamp: number;
  readerId?: number;
  phaseAngle?: number;
  dopplerHz?: number;
}

export interface DirectionEvent {
  epc: string;
  direction: TransitDirection;
  confidence: number;
  velocityMps?: number;
  firstSeenTimestamp: number;
  lastSeenTimestamp: number;
}

export interface ReaderInfo {
  readerId: number;
  driverId: string;
  name: string;
  address: string;
  status: 'Disconnected' | 'Connecting' | 'Ready' | 'InventoryRunning' | 'Error';
  powerDbm: number;
  antennas: number[];
}

export interface GatewayHealth {
  status: string;
  version: string;
  engine: string;
  activeReaders: number;
  uptimeSeconds: number;
}
