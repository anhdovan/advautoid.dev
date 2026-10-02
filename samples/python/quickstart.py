import os
import sys
import time
from smart_sdk import SmartSdk

# 1. Initialize native engine (auto-detect OS library)
lib_file = "AdvSmartSdk.dll" if sys.platform == "win32" else "libAdvSmartSdk.so"
sdk = SmartSdk(lib_file)

# 2. Hardware Fingerprint & Licensing
fp = sdk.get_hardware_fingerprint()
print(f"Machine Hardware Fingerprint: {fp}")

# FOR TESTING: Enable dev license bypass
sdk.set_dev_license_bypass(True)

# FOR PRODUCTION: Validate official RSA-2048 license file:
# is_valid, err_msg = sdk.validate_license("smartsdk.lic")
# if not is_valid:
#     raise RuntimeError(f"License check failed: {err_msg}")

# 3. Create and start a Reader (Mock loopback simulator or physical 'impinj'/'zebra')
def on_tag_read(tag):
    print(f"[NATIVE TAG] EPC: {tag.epc} | RSSI: {tag.rssi} dBm | Antenna: {tag.antenna_port}")

with sdk.create_reader("mock", reader_id=1, address="virtual:loopback") as reader:
    reader.start_inventory(on_tag_read)
    print("Streaming tags from reader #1 for 5 seconds...")
    time.sleep(5)
    reader.stop_inventory()
    reader.disconnect()

print("Quickstart completed successfully.")
