# Enterprise Architecture Overview

Modern enterprise RFID installations span distributed facilities: manufacturing plants, automated distribution centers, retail stores, and transport hubs.

The **Adv.SmartSdk** architecture supports three primary deployment topologies:

---

## 1. Topologies

```mermaid
graph TD
    subgraph SiteA["Site A: Distribution Center (Fixed Readers)"]
        DOCK1["Impinj R700 (Dock 1)"]
        DOCK2["Zebra FX9600 (Dock 2)"]
        GW_EDGE["AutoID Edge Gateway Container<br/>(Local Buffer, Portal Direction)"]
        DOCK1 --> GW_EDGE
        DOCK2 --> GW_EDGE
    end

    subgraph SiteB["Site B: Mobile Yard (Handheld Terminals)"]
        HANDHELD1["Zebra MC3300R / RFD40"]
        HANDHELD2["Urovo DT50P"]
        GW_MOBILE["AutoID Android Hybrid Service"]
        HANDHELD1 --> GW_MOBILE
        HANDHELD2 --> GW_MOBILE
    end

    subgraph Central["Central Cloud / Data Center (AWS / Azure / On-Prem)"]
        FLEET["AutoID Central Fleet Portal"]
        KAFKA["Apache Kafka / RabbitMQ Event Bus"]
        ERP["ERP / WMS Core (SAP, Manhattan, Infor)"]
    end

    GW_EDGE -->|"CloudEvents HTTPS Webhook / gRPC"| KAFKA
    GW_MOBILE -->|"Encrypted REST Sync / MQTT"| FLEET
    KAFKA --> ERP
    FLEET -->|"OTA Firmware & Config Sync"| GW_EDGE
    FLEET -->|"OTA Policy Sync"| GW_MOBILE
```

---

## 2. Resilience & Offline Spooling

Industrial networks frequently experience transient switch reboots or WAN outages.

1. **Local Persistent Spooling**:
   - When upstream network connections fail, the Edge Gateway immediately diverts tag events to a local write-ahead log (SQLite / binary spool).
   - Zero tag loss during up to 72 hours of complete network disconnection.
2. **Backpressure & Automatic Replay**:
   - As soon as connectivity is restored, the Gateway replays buffered events in exact chronological order with their original hardware millisecond timestamps.
3. **Health Monitoring & Auto-Restart**:
   - Built-in watchdog services automatically restart failed reader connections and report telemetry to the Fleet Portal.
