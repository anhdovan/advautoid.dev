using System;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading;

namespace AutoId.Samples.NativePInvoke;

public class Program
{
    private const string DllName = "AdvSmartSdk.dll";

    [UnmanagedFunctionPointer(CallingConvention.Cdecl)]
    public delegate void TagReadCallback(IntPtr readerHandle, [MarshalAs(UnmanagedType.LPUTF8Str)] string epc, int rssi, int antPort, long timestampMs);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_GetHardwareFingerprint(byte[] outFp, int outLen);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern void Sdk_SetDevLicenseBypass(int enabled);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_EncryptEpc(long tagId, [MarshalAs(UnmanagedType.LPUTF8Str)] string type, byte[] outEpcHex, int outLen);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_GetSgtin([MarshalAs(UnmanagedType.LPUTF8Str)] string gtin, [MarshalAs(UnmanagedType.LPUTF8Str)] string serial, int prefixLen, byte[] outEpcHex, int outLen);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_CreateReader([MarshalAs(UnmanagedType.LPUTF8Str)] string driverId, int readerId, [MarshalAs(UnmanagedType.LPUTF8Str)] string address, out IntPtr outHandle);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_RegisterTagCallback(IntPtr readerHandle, TagReadCallback callback);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_Connect(IntPtr readerHandle);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_StartInventory(IntPtr readerHandle);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_StopInventory(IntPtr readerHandle);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_Disconnect(IntPtr readerHandle);

    [DllImport(DllName, CallingConvention = CallingConvention.Cdecl)]
    public static extern int Sdk_FreeReader(IntPtr readerHandle);

    private static TagReadCallback? _callbackRef; // Prevent GC collection

    public static void Main()
    {
        Console.WriteLine("==============================================================");
        Console.WriteLine("       AutoID C# Low-Level P/Invoke Native C-ABI Sample        ");
        Console.WriteLine("==============================================================\n");

        try
        {
            // 1. Hardware Fingerprint
            byte[] fpBuf = new byte[64];
            int fpLen = Sdk_GetHardwareFingerprint(fpBuf, fpBuf.Length);
            string fp = Encoding.UTF8.GetString(fpBuf, 0, Math.Max(0, fpLen));
            Console.WriteLine($"[1] Hardware Fingerprint: {fp}");

            // 2. Dev License Bypass
            Sdk_SetDevLicenseBypass(1);
            Console.WriteLine("[2] Dev License Bypass: ENABLED");

            // 3. ChaCha20 EPC Encrypt
            byte[] encBuf = new byte[32];
            int encLen = Sdk_EncryptEpc(100200300400L, "0001", encBuf, encBuf.Length);
            string encEpc = Encoding.ASCII.GetString(encBuf, 0, Math.Max(0, encLen));
            Console.WriteLine($"[3] Encrypted ChaCha20 EPC: {encEpc}");

            // 4. GS1 SGTIN-96
            byte[] sgtinBuf = new byte[32];
            int sgtinLen = Sdk_GetSgtin("00860004123451", "55443322", 7, sgtinBuf, sgtinBuf.Length);
            string sgtinHex = Encoding.ASCII.GetString(sgtinBuf, 0, Math.Max(0, sgtinLen));
            Console.WriteLine($"[4] GS1 SGTIN-96 EPC: {sgtinHex}");

            // 5. Connect and stream from Mock reader
            Console.WriteLine("\n[5] Initializing Mock Reader via P/Invoke...");
            if (Sdk_CreateReader("mock", 1, "virtual:loopback", out IntPtr readerHandle) == 0 && readerHandle != IntPtr.Zero)
            {
                int count = 0;
                _callbackRef = (handle, epc, rssi, ant, ts) =>
                {
                    count++;
                    Console.WriteLine($"    [P/INVOKE TAG #{count:03d}] EPC: {epc} | RSSI: {rssi} dBm | Ant: {ant}");
                };

                Sdk_RegisterTagCallback(readerHandle, _callbackRef);
                Sdk_Connect(readerHandle);
                Sdk_StartInventory(readerHandle);

                Console.WriteLine("    Streaming tags for 3 seconds...");
                Thread.Sleep(3000);

                Sdk_StopInventory(readerHandle);
                Sdk_Disconnect(readerHandle);
                Sdk_FreeReader(readerHandle);
                Console.WriteLine($"\nFinished streaming. Read {count} tags.");
            }
        }
        catch (DllNotFoundException)
        {
            Console.WriteLine("[Note] AdvSmartSdk.dll was not found in the current working directory or PATH.");
            Console.WriteLine("Place AdvSmartSdk.dll adjacent to the executable to run native P/Invoke tests.");
        }
    }
}
