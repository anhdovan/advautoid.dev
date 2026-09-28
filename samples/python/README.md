# Python Samples

This directory provides Python samples for interfacing directly with the native C-ABI (`AdvSmartSdk.dll` / `libAdvSmartSdk.so`) and consuming CloudEvents webhooks.

---

## Files

- `smart_sdk.py`: Complete `ctypes` wrapper for all 18 native C-ABI exports, supporting RAII context managers and unmanaged callback marshaling.
- `main.py`: Interactive CLI demonstrating hardware fingerprinting, ChaCha20 EPC encryption/decryption, GS1 SGTIN-96 encoding, and mock reader streaming.
- `webhook_receiver.py`: Flask webhook microservice receiving CNCF CloudEvents with HMAC-SHA256 signature verification.

---

## Quick Start

### 1. Install Requirements
```bash
pip install -r requirements.txt
```

### 2. Run Native C-ABI Demo
```bash
python main.py
```

### 3. Run Webhook Receiver
```bash
python webhook_receiver.py
```
