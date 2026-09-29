# Licensing & Hardware Fingerprinting

The **Adv.SmartSdk** incorporates a cryptographic node-locking licensing system to protect enterprise IP while providing seamless activation for system integrators.

---

## 1. Hardware Fingerprinting (`BT-XXXX-XXXX-XXXX-XXXX`)

The SDK generates a hardware fingerprint derived from physical, non-volatile machine identifiers:
- Motherboard UUID and BIOS Serial Number
- CPU Processor ID
- Primary Disk Drive Physical Serial Number

### Querying Hardware Fingerprint

#### Using Python
```python
from smart_sdk import SmartSdk

sdk = SmartSdk("AdvSmartSdk.dll")
fp = sdk.get_hardware_fingerprint()
print(f"Machine Hardware Fingerprint: {fp}")
# Example output: BT-7F3A-89E2-11C0-994B
```

#### Using C++
```cpp
#include "AdvSmartSdk.h"
#include <iostream>

int main() {
    Beetech::Adv::AdvSmartSdk sdk("AdvSmartSdk.dll");
    char fp[64] = {0};
    sdk.Sdk_GetHardwareFingerprint(fp, sizeof(fp));
    std::cout << "Hardware Fingerprint: " << fp << std::endl;
}
```

---

## 2. License File Format (`smartsdk.lic`)

A license file is an encrypted, digitally signed JSON payload containing:
- Licensed Hardware Fingerprint(s)
- Allowed Reader Count (e.g. 4, 8, unlimited)
- Feature Flags (`DIRECTION_DETECTOR`, `CHACHA20_CRYPTO`, `GS1_SGTIN`)
- Expiration Timestamp (UTC)
- 2048-bit RSA / ECDSA Digital Signature

### Validating a License

```python
is_valid, err_msg = sdk.validate_license("path/to/smartsdk.lic")
if not is_valid:
    print(f"License verification failed: {err_msg}")
```

---

## 3. Developer Bypass Mode

During prototyping, testing, or automated CI pipelines, you can bypass license checks without a `.lic` file:

```python
# Enable developer bypass
sdk.set_dev_license_bypass(True)
```

In the Edge Gateway Docker container, set the environment variable:
```bash
-e DEV_LICENSE_BYPASS=true
```

> [!WARNING]
> Developer bypass mode is strictly restricted to development environments and non-production testing. Production builds should always use commercial node-locked licenses.

---

## 4. Instant Trial License Generator

Need an official RSA-2048 digitally signed license for a proof-of-concept or pilot?
1. Obtain your machine hardware fingerprint via `sdk.get_hardware_fingerprint()`.
2. Visit the self-service portal: **[https://advautoid.com/trial](https://advautoid.com/trial)**
3. Generate and download your instant 30-day trial license file (`license.lic`).
4. Place `license.lic` into your application directory or mount it into `/app/license.lic` in Docker.

