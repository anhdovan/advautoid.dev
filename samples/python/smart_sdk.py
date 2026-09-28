"""
Beetech Universal RFID SmartSdk - Python ctypes Binding
Integrates directly with the unmanaged native engine: AdvSmartSdk.dll / libAdvSmartSdk.so
"""

import os
import sys
import ctypes
from ctypes import (
    c_char_p, c_int, c_longlong, c_double, c_void_p,
    POINTER, CFUNCTYPE, create_string_buffer, byref
)
from dataclasses import dataclass
from typing import Callable, Optional, Tuple

# Determine OS library filename
if sys.platform == "win32":
    LIB_NAMES = ["AdvSmartSdk.dll"]
    RID_DIRS = ["win-x64", "win-arm64"]
elif sys.platform == "darwin":
    LIB_NAMES = ["libAdvSmartSdk.dylib", "AdvSmartSdk.dylib"]
    RID_DIRS = ["osx-arm64", "osx-x64"]
else:
    LIB_NAMES = ["libAdvSmartSdk.so", "AdvSmartSdk.so"]
    RID_DIRS = ["linux-x64", "linux-arm64"]

DEFAULT_LIB_PATHS = []
for lib_name in LIB_NAMES:
    DEFAULT_LIB_PATHS.extend([
        os.path.abspath(os.path.join(os.path.dirname(__file__), lib_name)),
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", lib_name)),
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "publish", lib_name))
    ])
    for rid in RID_DIRS:
        DEFAULT_LIB_PATHS.append(
            os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "bin", "Release", "net8.0", rid, "publish", lib_name))
        )

def load_smart_sdk_dll(dll_path: Optional[str] = None) -> ctypes.CDLL:
    if dll_path and os.path.exists(dll_path):
        return ctypes.CDLL(dll_path)
    
    for path in DEFAULT_LIB_PATHS:
        if os.path.exists(path):
            return ctypes.CDLL(path)
            
    expected = LIB_NAMES[0]
    raise FileNotFoundError(
        f"Native library '{expected}' could not be located for platform '{sys.platform}'.\n"
        "Ensure the native project is compiled via Native AOT:\n"
        "  Windows: dotnet publish Src/Windows/Beetech.Adv.SmartSdk.Native -c Release -r win-x64 /p:PublishAot=true\n"
        "  Linux:   dotnet publish Src/Windows/Beetech.Adv.SmartSdk.Native -c Release -r linux-x64 /p:PublishAot=true"
    )

@dataclass
class TagReadEvent:
    reader_handle: int
    epc: str
    rssi: int
    antenna_port: int
    timestamp_ms: int

@dataclass
class DirectionEvent:
    detector_handle: int
    epc: str
    direction: int
    confidence: float

# Native C callback prototypes
TAG_CALLBACK_TYPE = CFUNCTYPE(None, c_void_p, c_char_p, c_int, c_int, c_longlong)
DIRECTION_CALLBACK_TYPE = CFUNCTYPE(None, c_void_p, c_char_p, c_int, c_double)

class SmartSdk:
    def __init__(self, dll_path: Optional[str] = None):
        self._dll = load_smart_sdk_dll(dll_path)
        self._setup_signatures()

    def _setup_signatures(self):
        # 1. Sdk_GetHardwareFingerprint(byte* outFpUtf8, int outLen) -> int
        self._dll.Sdk_GetHardwareFingerprint.argtypes = [c_char_p, c_int]
        self._dll.Sdk_GetHardwareFingerprint.restype = c_int

        # 2. Sdk_ValidateLicense(byte* path, byte* outErr, int errLen) -> int
        self._dll.Sdk_ValidateLicense.argtypes = [c_char_p, c_char_p, c_int]
        self._dll.Sdk_ValidateLicense.restype = c_int

        # 3. Sdk_SetDevLicenseBypass(int enabled) -> void
        self._dll.Sdk_SetDevLicenseBypass.argtypes = [c_int]
        self._dll.Sdk_SetDevLicenseBypass.restype = None

        # 4. Sdk_EncryptEpc(long tagId, byte* type, byte* outEpcHex, int outLen) -> int
        self._dll.Sdk_EncryptEpc.argtypes = [c_longlong, c_char_p, c_char_p, c_int]
        self._dll.Sdk_EncryptEpc.restype = c_int

        # 5. Sdk_DecryptEpc(byte* epcHex, long* outTagId, byte* outType, int typeLen) -> int
        self._dll.Sdk_DecryptEpc.argtypes = [c_char_p, POINTER(c_longlong), c_char_p, c_int]
        self._dll.Sdk_DecryptEpc.restype = c_int

        # 6. Sdk_GetSgtin(byte* gtin, byte* serial, int prefixLen, byte* outHex, int outLen) -> int
        self._dll.Sdk_GetSgtin.argtypes = [c_char_p, c_char_p, c_int, c_char_p, c_int]
        self._dll.Sdk_GetSgtin.restype = c_int

        # 7. Sdk_TranslateEpcToGtin(byte* epcHex, byte* outGtin, int gtinLen, byte* outSerial, int serLen) -> int
        self._dll.Sdk_TranslateEpcToGtin.argtypes = [c_char_p, c_char_p, c_int, c_char_p, c_int]
        self._dll.Sdk_TranslateEpcToGtin.restype = c_int

        # 8. Sdk_CreateReader(byte* driverId, int readerId, byte* address, void** outHandle) -> int
        self._dll.Sdk_CreateReader.argtypes = [c_char_p, c_int, c_char_p, POINTER(c_void_p)]
        self._dll.Sdk_CreateReader.restype = c_int

        # 9. Sdk_RegisterTagCallback(void* handle, TAG_CALLBACK_TYPE callback) -> int
        self._dll.Sdk_RegisterTagCallback.argtypes = [c_void_p, TAG_CALLBACK_TYPE]
        self._dll.Sdk_RegisterTagCallback.restype = c_int

        # 10. Lifecycle operations
        self._dll.Sdk_Connect.argtypes = [c_void_p]
        self._dll.Sdk_Connect.restype = c_int
        self._dll.Sdk_Disconnect.argtypes = [c_void_p]
        self._dll.Sdk_Disconnect.restype = c_int
        self._dll.Sdk_StartInventory.argtypes = [c_void_p]
        self._dll.Sdk_StartInventory.restype = c_int
        self._dll.Sdk_StopInventory.argtypes = [c_void_p]
        self._dll.Sdk_StopInventory.restype = c_int
        self._dll.Sdk_FreeReader.argtypes = [c_void_p]
        self._dll.Sdk_FreeReader.restype = c_int

        # 11. Direction Detector
        self._dll.Sdk_CreateDirectionDetector.argtypes = [c_int, POINTER(c_void_p)]
        self._dll.Sdk_CreateDirectionDetector.restype = c_int
        self._dll.Sdk_RegisterDirectionCallback.argtypes = [c_void_p, DIRECTION_CALLBACK_TYPE]
        self._dll.Sdk_RegisterDirectionCallback.restype = c_int
        self._dll.Sdk_ProcessTagRead.argtypes = [c_void_p, c_char_p, c_int, c_double, c_longlong]
        self._dll.Sdk_ProcessTagRead.restype = c_int
        self._dll.Sdk_FreeDirectionDetector.argtypes = [c_void_p]
        self._dll.Sdk_FreeDirectionDetector.restype = c_int

    def get_hardware_fingerprint(self) -> str:
        buf = create_string_buffer(64)
        length = self._dll.Sdk_GetHardwareFingerprint(buf, 64)
        if length <= 0:
            return ""
        return buf.value[:length].decode('utf-8')

    def set_dev_license_bypass(self, enabled: bool):
        self._dll.Sdk_SetDevLicenseBypass(1 if enabled else 0)

    def validate_license(self, license_path: Optional[str] = None) -> Tuple[bool, str]:
        p_path = license_path.encode('utf-8') if license_path else None
        err_buf = create_string_buffer(256)
        res = self._dll.Sdk_ValidateLicense(p_path, err_buf, 256)
        return (res == 0, err_buf.value.decode('utf-8'))

    def encrypt_epc(self, tag_id: int, category: str) -> str:
        out_buf = create_string_buffer(32)
        res = self._dll.Sdk_EncryptEpc(tag_id, category.encode('utf-8'), out_buf, 32)
        if res <= 0:
            raise RuntimeError(f"Failed to encrypt EPC (code {res})")
        return out_buf.value[:res].decode('ascii')

    def decrypt_epc(self, epc_hex: str) -> Tuple[int, str]:
        tag_id = c_longlong(0)
        type_buf = create_string_buffer(64)
        res = self._dll.Sdk_DecryptEpc(epc_hex.encode('ascii'), byref(tag_id), type_buf, 64)
        if res != 0:
            raise RuntimeError(f"Failed to decrypt EPC '{epc_hex}' (code {res})")
        return (tag_id.value, type_buf.value.decode('utf-8'))

    def get_sgtin(self, gtin: str, serial: str, company_prefix_len: int = 7) -> str:
        out_buf = create_string_buffer(32)
        res = self._dll.Sdk_GetSgtin(gtin.encode('utf-8'), serial.encode('utf-8'), company_prefix_len, out_buf, 32)
        if res <= 0:
            raise RuntimeError(f"Failed to encode SGTIN-96 (code {res})")
        return out_buf.value[:res].decode('ascii')

    def translate_epc_to_gtin(self, epc_hex: str) -> Tuple[str, str]:
        gtin_buf = create_string_buffer(32)
        serial_buf = create_string_buffer(64)
        res = self._dll.Sdk_TranslateEpcToGtin(epc_hex.encode('ascii'), gtin_buf, 32, serial_buf, 64)
        if res != 0:
            raise RuntimeError(f"Failed to translate EPC '{epc_hex}' (code {res})")
        return (gtin_buf.value.decode('ascii'), serial_buf.value.decode('ascii'))

    def create_reader(self, driver_id: str, reader_id: int = 1, address: Optional[str] = None) -> 'SmartSdkReader':
        handle = c_void_p()
        p_addr = address.encode('utf-8') if address else None
        res = self._dll.Sdk_CreateReader(driver_id.encode('utf-8'), reader_id, p_addr, byref(handle))
        if res != 0 or not handle.value:
            raise RuntimeError(f"Failed to create reader driver '{driver_id}' (code {res})")
        return SmartSdkReader(self._dll, handle)

class SmartSdkReader:
    def __init__(self, dll: ctypes.CDLL, handle: c_void_p):
        self._dll = dll
        self._handle = handle
        self._callback_ref = None
        self._tag_read_listeners = []

        # Retain callback reference to prevent garbage collection
        def _native_callback(r_handle, p_epc, rssi, ant, ts):
            epc = p_epc.decode('ascii') if p_epc else ""
            evt = TagReadEvent(r_handle, epc, rssi, ant, ts)
            for listener in self._tag_read_listeners:
                try:
                    listener(evt)
                except Exception as ex:
                    print(f"[ERROR] Tag callback exception: {ex}", file=sys.stderr)

        self._callback_ref = TAG_CALLBACK_TYPE(_native_callback)
        self._dll.Sdk_RegisterTagCallback(self._handle, self._callback_ref)

    def on_tag_read(self, callback: Callable[[TagReadEvent], None]):
        self._tag_read_listeners.append(callback)

    def connect(self) -> bool:
        return self._dll.Sdk_Connect(self._handle) == 0

    def disconnect(self):
        self._dll.Sdk_Disconnect(self._handle)

    def start_inventory(self):
        self._dll.Sdk_StartInventory(self._handle)

    def stop_inventory(self):
        self._dll.Sdk_StopInventory(self._handle)

    def close(self):
        if self._handle:
            self._dll.Sdk_FreeReader(self._handle)
            self._handle = None
            self._callback_ref = None
            self._tag_read_listeners.clear()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
