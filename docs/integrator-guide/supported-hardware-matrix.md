# Supported Hardware Compatibility Matrix

The **Adv.SmartSdk** platform supports a broad catalog of fixed, desktop, and handheld RFID readers. All devices are accessed through a unified driver architecture.

---

## 1. Fixed Industrial Readers

| Manufacturer | Model Series | Antennas | Protocols | Default Baud / Ports | Max Power | Status |
| :--- | :--- | :---: | :--- | :--- | :---: | :---: |
| **Impinj** | Speedway R420 | 4 (up to 32 w/ Hub) | LLRP, Octane | TCP: 5084 | +32.5 dBm | Production |
| **Impinj** | Speedway R220 / R120 | 2 / 1 | LLRP, Octane | TCP: 5084 | +30.0 dBm | Production |
| **Impinj** | Speedway Revolution R700 | 4 | REST, MQTT, LLRP | TCP: 5084 / 443 | +33.0 dBm | Production |
| **Zebra** | FX9600 Industrial | 4 or 8 | LLRP, Host SDK | TCP: 5084 | +33.0 dBm | Production |
| **Zebra** | FX7500 Fixed Reader | 2 or 4 | LLRP, Host SDK | TCP: 5084 | +31.5 dBm | Production |
| **Urovo** | FR2000 Fixed Gateway | 4 | TCP Socket, RS-232 | TCP: 8088 / COM 115200 | +33.0 dBm | Production |
| **Urovo** | RF2000 Portal Reader | 8 | TCP Socket, RS-232 | TCP: 8088 | +33.0 dBm | Production |
| **Unitech** | RS804 Industrial | 4 | LLRP, WebSocket | TCP: 5084 | +31.5 dBm | Production |

---

## 2. Desktop & Integrated USB Readers

| Manufacturer | Model Series | Interface | Driver ID | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **CAEN RFID** | qIDmini (Keyfob) | USB / Bluetooth | `caen` | Compact UHF desktop / tag commissioner |
| **CAEN RFID** | Slate / Tile Desktop | USB COM / TCP | `caen` | POS checkout, inventory validation pads |
| **CAEN RFID** | Quattro / Quark | Serial UART / SPI | `caen` | Embedded OEM micro-modules |
| **Chainway** | R3 Desktop Pad | USB Virtual COM | `chainway` | Tag enrollment station, high-speed encoding |
| **Virtual Mock** | Loopback Simulator | In-Memory | `mock` | High-throughput loopback testing without hardware |

---

## 3. Handheld Sleds & Terminals

| Manufacturer | Model Series | Interface | Platform |
| :--- | :--- | :--- | :--- |
| **Zebra** | RFD40 / RFD90 / RFD8500 | Pogo pin / BLE | Android & Windows |
| **Zebra** | MC3300R / MC3390R | Integrated Android Terminal | Android |
| **Urovo** | DT50P / DT610 / CT48 | Integrated Android Terminal | Android |
| **Unitech** | RG768 / RP902 / HT730 | Pogo pin / BLE / Integrated | Android |
| **Chainway** | C72 / C66 / C70 | Integrated Android Terminal | Android |
