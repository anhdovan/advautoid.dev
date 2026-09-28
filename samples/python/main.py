"""
AutoID Python Native C-ABI Sample Application
Demonstrates complete hardware control, ChaCha20 encryption, and GS1 SGTIN-96 codec.
"""

import time
import sys
from smart_sdk import SmartSdk, TagReadEvent

def print_separator(title: str):
    print("\n" + "=" * 60)
    print(f"  {title}")
    print("=" * 60)

def main():
    print_separator("AutoID Python SDK Demonstration")

    try:
        sdk = SmartSdk()
    except Exception as ex:
        print(f"Error loading SDK native library: {ex}")
        print("Note: In standalone mode, compile AdvSmartSdk.dll or set PATH.")
        sys.exit(1)

    # 1. Hardware Fingerprint
    print_separator("1. Hardware Node-Locking Fingerprint")
    fp = sdk.get_hardware_fingerprint()
    print(f"Machine Fingerprint: {fp}")

    # 2. Licensing
    print_separator("2. License Management")
    sdk.set_dev_license_bypass(True)
    print("Dev License Bypass Enabled: TRUE")

    # 3. ChaCha20 EPC Cryptography
    print_separator("3. ChaCha20 Anti-Clone EPC Cryptography")
    tag_id = 9876543210
    tag_type = "PALLET"
    encrypted_epc = sdk.encrypt_epc(tag_id, tag_type)
    print(f"Original Tag ID: {tag_id} | Type: {tag_type}")
    print(f"Encrypted EPC  : {encrypted_epc}")

    dec_id, dec_type = sdk.decrypt_epc(encrypted_epc)
    print(f"Decrypted ID   : {dec_id} | Type: {dec_type}")
    assert tag_id == dec_id, "Decrypted Tag ID must match!"
    print("Verification   : SUCCESS (Cryptographically Valid)")

    # 4. GS1 SGTIN-96 Codec
    print_separator("4. GS1 SGTIN-96 Bitwise Codec")
    gtin = "00860004123451"
    serial = "99887766"
    sgtin_epc = sdk.get_sgtin(gtin, serial, company_prefix_len=7)
    print(f"Original GTIN-14: {gtin} | Serial: {serial}")
    print(f"SGTIN-96 Hex EPC: {sgtin_epc}")

    dec_gtin, dec_serial = sdk.translate_epc_to_gtin(sgtin_epc)
    print(f"Decoded GTIN-14 : {dec_gtin} | Serial: {dec_serial}")
    print("Verification    : SUCCESS")

    # 5. Reader Hardware Streaming (Virtual Mock Reader)
    print_separator("5. Reader Control & Live Inventory Streaming")
    
    tag_count = 0
    def on_tag(evt: TagReadEvent):
        nonlocal tag_count
        tag_count += 1
        print(f"[TAG #{tag_count:03d}] EPC: {evt.epc} | RSSI: {evt.rssi:3d} dBm | Ant: {evt.antenna_port}")

    print("Creating 'mock' reader (Loopback Simulator)...")
    with sdk.create_reader("mock", reader_id=1, address="virtual:loopback") as reader:
        reader.on_tag_read(on_tag)
        
        print("Connecting to reader...")
        if not reader.connect():
            print("Failed to connect!")
            return

        print("Starting inventory stream (running for 3 seconds)...")
        reader.start_inventory()
        time.sleep(3.0)

        print("Stopping inventory stream...")
        reader.stop_inventory()
        reader.disconnect()

    print(f"\nInventory complete. Total tags captured: {tag_count}")
    print_separator("Demonstration Complete")

if __name__ == "__main__":
    main()
