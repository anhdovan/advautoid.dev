#include <iostream>
#include <iomanip>
#include <string>
#include <vector>
#include <chrono>
#include <thread>
#include "AdvSmartSdk.h"

using namespace Beetech::Adv;

int main() {
    std::cout << "==============================================================\n";
    std::cout << "         AutoID C++ Native C-ABI Sample Application           \n";
    std::cout << "==============================================================\n\n";

    AdvSmartSdk sdk;
    if (!sdk.Load()) {
        std::cerr << "[ERROR] Could not dynamically load AdvSmartSdk shared library!\n";
        std::cerr << "Ensure AdvSmartSdk.dll (Windows) or libAdvSmartSdk.so (Linux) is present.\n";
        return 1;
    }

    // 1. Hardware Fingerprint
    std::string fp = sdk.GetHardwareFingerprint();
    std::cout << "[1] Machine Hardware Fingerprint: " << fp << "\n";

    // 2. Dev License Bypass
    sdk.SetDevLicenseBypass(true);
    std::cout << "[2] Dev License Bypass: ENABLED\n\n";

    // 3. ChaCha20 EPC Cryptography
    std::cout << "[3] Testing ChaCha20 EPC Cryptography...\n";
    char encEpc[32] = {0};
    long long tagId = 123456789LL;
    int encRes = sdk.Sdk_EncryptEpc(tagId, "BOX", encEpc, sizeof(encEpc));
    if (encRes > 0) {
        std::cout << "    Encrypted EPC: " << encEpc << "\n";
        
        long long decTagId = 0;
        char decType[32] = {0};
        int decRes = sdk.Sdk_DecryptEpc(encEpc, &decTagId, decType, sizeof(decType));
        if (decRes == 0 && decTagId == tagId) {
            std::cout << "    Decrypted Tag ID: " << decTagId << " | Type: " << decType << " (SUCCESS)\n\n";
        }
    }

    // 4. GS1 SGTIN-96 Codec
    std::cout << "[4] Testing GS1 SGTIN-96 Bitwise Codec...\n";
    char sgtinHex[32] = {0};
    const char* gtin = "00860004123451";
    const char* serial = "10203040";
    int sgtinRes = sdk.Sdk_GetSgtin(gtin, serial, 7, sgtinHex, sizeof(sgtinHex));
    if (sgtinRes > 0) {
        std::cout << "    Encoded SGTIN-96 EPC: " << sgtinHex << "\n";
        
        char outGtin[32] = {0};
        char outSerial[32] = {0};
        int trRes = sdk.Sdk_TranslateEpcToGtin(sgtinHex, outGtin, sizeof(outGtin), outSerial, sizeof(outSerial));
        if (trRes == 0) {
            std::cout << "    Decoded GTIN: " << outGtin << " | Serial: " << outSerial << " (SUCCESS)\n\n";
        }
    }

    // 5. Reader Hardware Streaming (Mock Loopback)
    std::cout << "[5] Testing Reader Hardware Streaming (driver='mock')...\n";
    void* readerHandle = nullptr;
    int createRes = sdk.Sdk_CreateReader("mock", 1, "virtual:loopback", &readerHandle);
    if (createRes != 0 || !readerHandle) {
        std::cerr << "    Failed to create reader instance (code " << createRes << ")\n";
        return 1;
    }

    auto reader = std::make_unique<AdvSmartSdkReader>(&sdk, readerHandle);
    int tagCounter = 0;

    reader->OnTagRead([&tagCounter](const TagReadEvent& evt) {
        tagCounter++;
        std::cout << "    [TAG EVENT #" << tagCounter << "] EPC: " << evt.epc
                  << " | RSSI: " << evt.rssi << " dBm"
                  << " | Ant: " << evt.antennaPort << "\n";
    });

    if (reader->Connect()) {
        std::cout << "    Reader connected successfully. Starting inventory stream for 3 seconds...\n";
        reader->StartInventory();

        std::this_thread::sleep_for(std::chrono::seconds(3));

        std::cout << "    Stopping inventory...\n";
        reader->StopInventory();
        reader->Disconnect();
    } else {
        std::cerr << "    Failed to connect reader.\n";
    }

    reader->Close();

    std::cout << "\n==============================================================\n";
    std::cout << "AutoID C++ Demonstration Completed Successfully (" << tagCounter << " tags read)\n";
    std::cout << "==============================================================\n";
    return 0;
}
