package com.beetech.adv.smartsdk;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Modern Java 21+ Foreign Function & Memory (FFM) binding for Beetech Universal RFID SmartSdk.
 * Links directly to the unmanaged native engine: AdvSmartSdk.dll.
 */
public final class SmartSdk implements AutoCloseable {

    public record TagReadEvent(MemorySegment readerHandle, String epc, int rssi, int antennaPort, long timestampMs) {}
    public record DirectionEvent(MemorySegment detectorHandle, String epc, int direction, double confidence) {}

    private final Arena arena;
    private final SymbolLookup lookup;
    private final Linker linker;

    // Method Handles
    private final MethodHandle hGetHardwareFingerprint;
    private final MethodHandle hSetDevLicenseBypass;
    private final MethodHandle hValidateLicense;
    private final MethodHandle hEncryptEpc;
    private final MethodHandle hDecryptEpc;
    private final MethodHandle hGetSgtin;
    private final MethodHandle hTranslateEpcToGtin;

    private final MethodHandle hCreateReader;
    private final MethodHandle hRegisterTagCallback;
    private final MethodHandle hConnect;
    private final MethodHandle hDisconnect;
    private final MethodHandle hStartInventory;
    private final MethodHandle hStopInventory;
    private final MethodHandle hFreeReader;

    private final MethodHandle hCreateDirectionDetector;
    private final MethodHandle hRegisterDirectionCallback;
    private final MethodHandle hProcessTagRead;
    private final MethodHandle hFreeDirectionDetector;

    public SmartSdk() {
        this(resolveDllPath());
    }

    public SmartSdk(Path dllPath) {
        if (!Files.exists(dllPath)) {
            throw new IllegalArgumentException("AdvSmartSdk.dll not found at: " + dllPath);
        }

        this.arena = Arena.ofShared();
        this.lookup = SymbolLookup.libraryLookup(dllPath, arena);
        this.linker = Linker.nativeLinker();

        try {
            // Fingerprint & Licensing
            hGetHardwareFingerprint = downcall("Sdk_GetHardwareFingerprint", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
            hSetDevLicenseBypass = downcall("Sdk_SetDevLicenseBypass", 
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT));
            hValidateLicense = downcall("Sdk_ValidateLicense", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

            // Crypto & SGTIN
            hEncryptEpc = downcall("Sdk_EncryptEpc", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
            hDecryptEpc = downcall("Sdk_DecryptEpc", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
            hGetSgtin = downcall("Sdk_GetSgtin", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
            hTranslateEpcToGtin = downcall("Sdk_TranslateEpcToGtin", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

            // Reader
            hCreateReader = downcall("Sdk_CreateReader", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            hRegisterTagCallback = downcall("Sdk_RegisterTagCallback", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            hConnect = downcall("Sdk_Connect", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            hDisconnect = downcall("Sdk_Disconnect", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            hStartInventory = downcall("Sdk_StartInventory", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            hStopInventory = downcall("Sdk_StopInventory", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            hFreeReader = downcall("Sdk_FreeReader", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

            // Direction
            hCreateDirectionDetector = downcall("Sdk_CreateDirectionDetector", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            hRegisterDirectionCallback = downcall("Sdk_RegisterDirectionCallback", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
            hProcessTagRead = downcall("Sdk_ProcessTagRead", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_LONG));
            hFreeDirectionDetector = downcall("Sdk_FreeDirectionDetector", 
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

        } catch (Exception ex) {
            throw new RuntimeException("Failed to bind AdvSmartSdk symbols", ex);
        }
    }

    private MethodHandle downcall(String name, FunctionDescriptor desc) {
        MemorySegment sym = lookup.find(name).orElseThrow(() -> new IllegalStateException("Symbol not found: " + name));
        return linker.downcallHandle(sym, desc);
    }

    private static Path resolveDllPath() {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<String> libNames;
        List<String> rids;
        if (os.contains("win")) {
            libNames = List.of("AdvSmartSdk.dll");
            rids = List.of("win-x64", "win-arm64");
        } else if (os.contains("mac")) {
            libNames = List.of("libAdvSmartSdk.dylib", "AdvSmartSdk.dylib");
            rids = List.of("osx-arm64", "osx-x64");
        } else {
            libNames = List.of("libAdvSmartSdk.so", "AdvSmartSdk.so");
            rids = List.of("linux-x64", "linux-arm64");
        }

        List<Path> candidates = new java.util.ArrayList<>();
        for (String libName : libNames) {
            candidates.add(Path.of(libName));
            candidates.add(Path.of("Samples/CSharp/06_ReaderApi/bin/Release/net8.0/" + libName));
            candidates.add(Path.of("../CSharp/06_ReaderApi/bin/Release/net8.0/" + libName));
            candidates.add(Path.of("Samples/06_ReaderApi/bin/Release/net8.0/" + libName));
            candidates.add(Path.of("../06_ReaderApi/bin/Release/net8.0/" + libName));
            for (String rid : rids) {
                candidates.add(Path.of("Src/Windows/Beetech.Adv.SmartSdk.Native/bin/Release/net8.0/" + rid + "/publish/" + libName));
                candidates.add(Path.of("../../Src/Windows/Beetech.Adv.SmartSdk.Native/bin/Release/net8.0/" + rid + "/publish/" + libName));
            }
        }

        for (Path p : candidates) {
            if (Files.exists(p)) return p.toAbsolutePath().normalize();
        }
        return candidates.get(0).toAbsolutePath().normalize();
    }

    public String getHardwareFingerprint() {
        try (Arena local = Arena.ofConfined()) {
            MemorySegment buf = local.allocate(64);
            int len = (int) hGetHardwareFingerprint.invokeExact(buf, 64);
            if (len <= 0) return "";
            return buf.getUtf8String(0);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void setDevLicenseBypass(boolean enabled) {
        try {
            hSetDevLicenseBypass.invokeExact(enabled ? 1 : 0);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public String encryptEpc(long tagId, String category) {
        try (Arena local = Arena.ofConfined()) {
            MemorySegment pCat = local.allocateUtf8String(category);
            MemorySegment outBuf = local.allocate(32);
            int len = (int) hEncryptEpc.invokeExact(tagId, pCat, outBuf, 32);
            if (len <= 0) throw new RuntimeException("Failed to encrypt EPC");
            return outBuf.getUtf8String(0);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public String getSgtin(String gtin, String serial, int prefixLen) {
        try (Arena local = Arena.ofConfined()) {
            MemorySegment pGtin = local.allocateUtf8String(gtin);
            MemorySegment pSer = local.allocateUtf8String(serial);
            MemorySegment outBuf = local.allocate(32);
            int len = (int) hGetSgtin.invokeExact(pGtin, pSer, prefixLen, outBuf, 32);
            if (len <= 0) throw new RuntimeException("Failed to encode SGTIN-96");
            return outBuf.getUtf8String(0);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public SmartSdkReader createReader(String driverId, int readerId, String address) {
        try {
            MemorySegment pDriver = arena.allocateUtf8String(driverId);
            MemorySegment pAddr = address != null ? arena.allocateUtf8String(address) : MemorySegment.NULL;
            MemorySegment pOutHandle = arena.allocate(ValueLayout.ADDRESS);

            int res = (int) hCreateReader.invokeExact(pDriver, readerId, pAddr, pOutHandle);
            if (res != 0) {
                throw new RuntimeException("Failed to create reader '" + driverId + "' (code " + res + ")");
            }

            MemorySegment handle = pOutHandle.get(ValueLayout.ADDRESS, 0);
            return new SmartSdkReader(this, handle);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void close() {
        arena.close();
    }

    /**
     * Managed reader wrapper in Java.
     */
    public static final class SmartSdkReader implements AutoCloseable {
        private final SmartSdk sdk;
        private MemorySegment handle;
        private final List<Consumer<TagReadEvent>> listeners = new CopyOnWriteArrayList<>();
        private MemorySegment upcallStub;

        private SmartSdkReader(SmartSdk sdk, MemorySegment handle) throws Throwable {
            this.sdk = sdk;
            this.handle = handle;

            MethodHandle cbTarget = MethodHandles.lookup().findVirtual(
                SmartSdkReader.class, "onNativeTagRead",
                MethodType.methodType(void.class, MemorySegment.class, MemorySegment.class, int.class, int.class, long.class)
            ).bindTo(this);

            FunctionDescriptor cbDesc = FunctionDescriptor.ofVoid(
                ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG
            );
            this.upcallStub = sdk.linker.upcallStub(cbTarget, cbDesc, sdk.arena);

            int res = (int) sdk.hRegisterTagCallback.invokeExact(handle, upcallStub);
            if (res != 0) throw new RuntimeException("Failed to register native tag callback");
        }

        private void onNativeTagRead(MemorySegment rHandle, MemorySegment pEpc, int rssi, int antPort, long ts) {
            String epc = pEpc.reinterpret(128).getUtf8String(0);
            TagReadEvent evt = new TagReadEvent(rHandle, epc, rssi, antPort, ts);
            for (var listener : listeners) {
                try {
                    listener.accept(evt);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void addTagReadListener(Consumer<TagReadEvent> listener) {
            listeners.add(listener);
        }

        public boolean connect() {
            try {
                return ((int) sdk.hConnect.invokeExact(handle)) == 0;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        public void disconnect() {
            try {
                int res = (int) sdk.hDisconnect.invokeExact(handle);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        public void startInventory() {
            try {
                int res = (int) sdk.hStartInventory.invokeExact(handle);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        public void stopInventory() {
            try {
                int res = (int) sdk.hStopInventory.invokeExact(handle);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        @Override
        public void close() {
            if (handle != null && !handle.equals(MemorySegment.NULL)) {
                try {
                    int res = (int) sdk.hFreeReader.invokeExact(handle);
                } catch (Throwable ignored) {}
                handle = MemorySegment.NULL;
                listeners.clear();
            }
        }
    }
}
