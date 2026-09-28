# TypeScript Samples

This directory provides TypeScript samples utilizing strongly-typed interfaces for the **AutoID Edge Gateway** and client-side portal analytics.

---

## Files

- `src/types.ts`: TypeScript models for tags, transit events, and reader information.
- `src/index.ts`: High-level `AutoIdClient` supporting health checks, reader operations, and real-time WebSocket subscriptions.
- `src/portal-tracker.ts`: Standalone client-side RSSI Centroid direction classifier for antenna transit scoring.

---

## Quick Start

### 1. Install Dependencies
```bash
npm install
```

### 2. Compile TypeScript
```bash
npm run build
```

### 3. Run Gateway Client Demo
```bash
npm start
```

### 4. Run Direction Tracker Test
```bash
npm run tracker
```
