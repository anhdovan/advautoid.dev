# RSSI Centroid Portal Direction Tracking

In warehouse dock doors and automated retail gates, detecting *whether* a tagged asset moved through a portal is insufficient: systems must reliably distinguish **Inbound Arrivals** (`Out-to-In`) from **Outbound Shipments** (`In-to-Out`).

The **Adv.SmartSdk** includes a real-time trajectory classification engine based on **Dual-Zone RSSI Centroid Velocity Analysis**.

---

## 1. Algorithmic Principle

```
        Inside Warehouse (Zone A: Antennas 1 & 3)
   ──────────────────────────────────────────────────
                       ▲
                       │   Trajectory Vector
                       │
   ──────────────────────────────────────────────────
        Outside / Dock (Zone B: Antennas 2 & 4)
```

1. **RSSI Smoothing & Exponential Moving Average (EMA)**:
   For each incoming tag observation on antenna $i$ at timestamp $t$:
   $$RSSI_{smoothed}(i, t) = \alpha \cdot RSSI(t) + (1 - \alpha) \cdot RSSI_{smoothed}(i, t-1)$$
2. **Centroid Position Estimation ($Z(t)$)**:
   Antennas are assigned normalized spatial positions along the portal axis:
   - Inside Antennas: $P_1 = +1.0, P_3 = +1.0$
   - Outside Antennas: $P_2 = -1.0, P_4 = -1.0$
   
   The spatial centroid at time $t$ is:
   $$Z(t) = \frac{\sum_{i} RSSI_{lin}(i, t) \cdot P_i}{\sum_{i} RSSI_{lin}(i, t)}$$
   where $RSSI_{lin} = 10^{\frac{RSSI_{dBm}}{10}}$.

3. **Trajectory Slope (Velocity $\frac{dZ}{dt}$)**:
   Linear regression over sliding window $\Delta T$:
   - Positive Slope ($+m$): **Out-to-In (Arrival)**
   - Negative Slope ($-m$): **In-to-Out (Departure)**
4. **Statistical Confidence ($C$)**:
   Computed based on $R^2$ fit, observation count, and dynamic RSSI peak separation.

---

## 2. Direction Enumeration

| Value | Direction Name | Meaning |
| :---: | :--- | :--- |
| `0` | `Unknown` | Tag stationary or insufficient read count |
| `1` | `InToOut` | Transit from Inside to Outside (Shipment / Departure) |
| `2` | `OutToIn` | Transit from Outside to Inside (Receiving / Arrival) |

---

## 3. Native Integration Example (C++)

```cpp
#include "AdvSmartSdk.h"
#include <iostream>

void ADV_CALL OnDirectionEvent(void* detectorHandle, const char* epcUtf8, int direction, double confidence) {
    const char* dirStr = (direction == 1) ? "IN_TO_OUT (Departure)" : 
                         (direction == 2) ? "OUT_TO_IN (Arrival)" : "UNKNOWN";
    std::cout << "[PORTAL EVENT] Tag: " << epcUtf8 
              << " | Direction: " << dirStr 
              << " | Confidence: " << (confidence * 100.0) << "%" << std::endl;
}

int main() {
    Beetech::Adv::AdvSmartSdk sdk("AdvSmartSdk.dll");
    
    void* detector = nullptr;
    sdk.Sdk_CreateDirectionDetector(0, &detector);
    sdk.Sdk_RegisterDirectionCallback(detector, OnDirectionEvent);

    // As reader tags arrive, feed them into detector:
    // sdk.Sdk_ProcessTagRead(detector, "E280116060000214B001ABCD", 1, -55.0, 1774742400000LL);
    
    sdk.Sdk_FreeDirectionDetector(detector);
}
```
