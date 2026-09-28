#pragma once

#ifndef ADV_SMART_SDK_H
#define ADV_SMART_SDK_H

#ifdef _WIN32
#include <windows.h>
typedef HMODULE NativeModuleHandle;
#define ADV_CALL __cdecl
#define ADV_LOAD_LIB(path) LoadLibraryA(path)
#define ADV_GET_SYM(mod, sym) (void*)GetProcAddress((HMODULE)(mod), (sym))
#define ADV_FREE_LIB(mod) FreeLibrary((HMODULE)(mod))
#else
#include <dlfcn.h>
#include <unistd.h>
typedef void* NativeModuleHandle;
#define ADV_CALL
#define ADV_LOAD_LIB(path) dlopen((path), RTLD_NOW)
#define ADV_GET_SYM(mod, sym) dlsym((mod), (sym))
#define ADV_FREE_LIB(mod) dlclose((mod))
#endif

#include <string>
#include <vector>
#include <functional>
#include <stdexcept>
#include <iostream>

#ifdef __cplusplus
extern "C" {
#endif

// ============================================================================
// Native C-ABI Callback Types
// ============================================================================
typedef void (ADV_CALL *FnTagReadCallback)(void* readerHandle, const char* epcUtf8, int rssi, int antPort, long long timestampMs);
typedef void (ADV_CALL *FnDirectionCallback)(void* detectorHandle, const char* epcUtf8, int direction, double confidence);

// ============================================================================
// Function Pointer Types
// ============================================================================
typedef int  (ADV_CALL *FnSdk_GetHardwareFingerprint)(char* outFpUtf8, int outLen);
typedef int  (ADV_CALL *FnSdk_ValidateLicense)(const char* licensePathUtf8, char* outErrorUtf8, int errorLen);
typedef void (ADV_CALL *FnSdk_SetDevLicenseBypass)(int enabled);

typedef int  (ADV_CALL *FnSdk_EncryptEpc)(long long tagId, const char* typeUtf8, char* outEpcHex, int outLen);
typedef int  (ADV_CALL *FnSdk_DecryptEpc)(const char* epcHexUtf8, long long* outTagId, char* outTypeUtf8, int typeLen);
typedef int  (ADV_CALL *FnSdk_GetSgtin)(const char* gtinUtf8, const char* serialUtf8, int companyPrefixLength, char* outEpcHex, int outLen);
typedef int  (ADV_CALL *FnSdk_TranslateEpcToGtin)(const char* epcHexUtf8, char* outGtinUtf8, int gtinLen, char* outSerialUtf8, int serialLen);

typedef int  (ADV_CALL *FnSdk_CreateReader)(const char* driverIdUtf8, int readerId, const char* addressUtf8, void** outHandle);
typedef int  (ADV_CALL *FnSdk_RegisterTagCallback)(void* readerHandle, FnTagReadCallback callback);
typedef int  (ADV_CALL *FnSdk_Connect)(void* readerHandle);
typedef int  (ADV_CALL *FnSdk_Disconnect)(void* readerHandle);
typedef int  (ADV_CALL *FnSdk_StartInventory)(void* readerHandle);
typedef int  (ADV_CALL *FnSdk_StopInventory)(void* readerHandle);
typedef int  (ADV_CALL *FnSdk_FreeReader)(void* readerHandle);

typedef int  (ADV_CALL *FnSdk_CreateDirectionDetector)(int setupMode, void** outHandle);
typedef int  (ADV_CALL *FnSdk_RegisterDirectionCallback)(void* detectorHandle, FnDirectionCallback callback);
typedef int  (ADV_CALL *FnSdk_ProcessTagRead)(void* detectorHandle, const char* epcUtf8, int antPort, double rssi, long long timestampMs);
typedef int  (ADV_CALL *FnSdk_FreeDirectionDetector)(void* detectorHandle);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus
namespace Beetech {
namespace Adv {

struct TagReadEvent {
    void* readerHandle;
    std::string epc;
    int rssi;
    int antennaPort;
    long long timestampMs;
};

class AdvSmartSdk {
private:
    NativeModuleHandle m_hModule;

public:
    FnSdk_GetHardwareFingerprint Sdk_GetHardwareFingerprint;
    FnSdk_ValidateLicense Sdk_ValidateLicense;
    FnSdk_SetDevLicenseBypass Sdk_SetDevLicenseBypass;

    FnSdk_EncryptEpc Sdk_EncryptEpc;
    FnSdk_DecryptEpc Sdk_DecryptEpc;
    FnSdk_GetSgtin Sdk_GetSgtin;
    FnSdk_TranslateEpcToGtin Sdk_TranslateEpcToGtin;

    FnSdk_CreateReader Sdk_CreateReader;
    FnSdk_RegisterTagCallback Sdk_RegisterTagCallback;
    FnSdk_Connect Sdk_Connect;
    FnSdk_Disconnect Sdk_Disconnect;
    FnSdk_StartInventory Sdk_StartInventory;
    FnSdk_StopInventory Sdk_StopInventory;
    FnSdk_FreeReader Sdk_FreeReader;

    FnSdk_CreateDirectionDetector Sdk_CreateDirectionDetector;
    FnSdk_RegisterDirectionCallback Sdk_RegisterDirectionCallback;
    FnSdk_ProcessTagRead Sdk_ProcessTagRead;
    FnSdk_FreeDirectionDetector Sdk_FreeDirectionDetector;

    AdvSmartSdk() : m_hModule(NULL) {}

    ~AdvSmartSdk() {
        Unload();
    }

    bool Load(const std::string& customPath = "") {
        std::vector<std::string> candidates;
        if (!customPath.empty()) candidates.push_back(customPath);

#ifdef _WIN32
        const std::vector<std::string> libNames = { "AdvSmartSdk.dll" };
        const std::vector<std::string> rids = { "win-x64", "win-arm64" };
#elif defined(__APPLE__)
        const std::vector<std::string> libNames = { "libAdvSmartSdk.dylib", "AdvSmartSdk.dylib" };
        const std::vector<std::string> rids = { "osx-arm64", "osx-x64" };
#else
        const std::vector<std::string> libNames = { "libAdvSmartSdk.so", "AdvSmartSdk.so" };
        const std::vector<std::string> rids = { "linux-x64", "linux-arm64" };
#endif

        for (const auto& name : libNames) {
            candidates.push_back(name);
            candidates.push_back("../../runtimes/win-x64/" + name);
            candidates.push_back("../../runtimes/linux-x64/" + name);
            candidates.push_back("../runtimes/win-x64/" + name);
            candidates.push_back("../runtimes/linux-x64/" + name);
            candidates.push_back("../../publish/" + name);
            candidates.push_back("../" + name);
            for (const auto& rid : rids) {
                candidates.push_back("../../runtimes/" + rid + "/" + name);
                candidates.push_back("../../bin/Release/net8.0/" + rid + "/publish/" + name);
            }
        }

        for (const auto& path : candidates) {
            if (path.empty()) continue;
            m_hModule = (NativeModuleHandle)ADV_LOAD_LIB(path.c_str());
            if (m_hModule) break;
        }

        if (!m_hModule) {
            return false;
        }

        #define BIND_SYM(sym) sym = (Fn##sym)ADV_GET_SYM(m_hModule, #sym); if (!sym) return false;

        BIND_SYM(Sdk_GetHardwareFingerprint);
        BIND_SYM(Sdk_ValidateLicense);
        BIND_SYM(Sdk_SetDevLicenseBypass);

        BIND_SYM(Sdk_EncryptEpc);
        BIND_SYM(Sdk_DecryptEpc);
        BIND_SYM(Sdk_GetSgtin);
        BIND_SYM(Sdk_TranslateEpcToGtin);

        BIND_SYM(Sdk_CreateReader);
        BIND_SYM(Sdk_RegisterTagCallback);
        BIND_SYM(Sdk_Connect);
        BIND_SYM(Sdk_Disconnect);
        BIND_SYM(Sdk_StartInventory);
        BIND_SYM(Sdk_StopInventory);
        BIND_SYM(Sdk_FreeReader);

        BIND_SYM(Sdk_CreateDirectionDetector);
        BIND_SYM(Sdk_RegisterDirectionCallback);
        BIND_SYM(Sdk_ProcessTagRead);
        BIND_SYM(Sdk_FreeDirectionDetector);

        #undef BIND_SYM
        return true;
    }

    void Unload() {
        if (m_hModule) {
            ADV_FREE_LIB(m_hModule);
            m_hModule = NULL;
        }
    }

    std::string GetHardwareFingerprint() {
        char buf[64] = {0};
        int len = Sdk_GetHardwareFingerprint(buf, 64);
        return len > 0 ? std::string(buf, len) : "";
    }

    void SetDevLicenseBypass(bool enabled) {
        Sdk_SetDevLicenseBypass(enabled ? 1 : 0);
    }
};

class AdvSmartSdkReader {
private:
    AdvSmartSdk* m_sdk;
    void* m_handle;
    std::function<void(const TagReadEvent&)> m_userCallback;

    static void ADV_CALL StaticTagCallback(void* readerHandle, const char* epcUtf8, int rssi, int antPort, long long timestampMs) {
        if (s_currentInstance && s_currentInstance->m_userCallback) {
            TagReadEvent evt;
            evt.readerHandle = readerHandle;
            evt.epc = epcUtf8 ? epcUtf8 : "";
            evt.rssi = rssi;
            evt.antennaPort = antPort;
            evt.timestampMs = timestampMs;
            s_currentInstance->m_userCallback(evt);
        }
    }

    static AdvSmartSdkReader* s_currentInstance;

public:
    AdvSmartSdkReader(AdvSmartSdk* sdk, void* handle) : m_sdk(sdk), m_handle(handle) {
        s_currentInstance = this;
        m_sdk->Sdk_RegisterTagCallback(m_handle, StaticTagCallback);
    }

    ~AdvSmartSdkReader() {
        Close();
    }

    void OnTagRead(std::function<void(const TagReadEvent&)> callback) {
        m_userCallback = callback;
    }

    bool Connect() {
        return m_sdk->Sdk_Connect(m_handle) == 0;
    }

    void Disconnect() {
        m_sdk->Sdk_Disconnect(m_handle);
    }

    void StartInventory() {
        m_sdk->Sdk_StartInventory(m_handle);
    }

    void StopInventory() {
        m_sdk->Sdk_StopInventory(m_handle);
    }

    void Close() {
        if (m_handle) {
            m_sdk->Sdk_FreeReader(m_handle);
            m_handle = NULL;
            if (s_currentInstance == this) {
                s_currentInstance = nullptr;
            }
        }
    }
};

#ifdef _WIN32
__declspec(selectany) AdvSmartSdkReader* AdvSmartSdkReader::s_currentInstance = nullptr;
#else
inline AdvSmartSdkReader* AdvSmartSdkReader::s_currentInstance = nullptr;
#endif

}} // namespace Beetech::Adv
#endif // __cplusplus

#endif // ADV_SMART_SDK_H
