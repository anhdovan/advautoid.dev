using System;
using System.Net.Http;
using System.Net.Http.Json;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace AutoId.Samples.Quickstart;

public class Program
{
    public static async Task Main(string[] args)
    {
        Console.WriteLine("==============================================================");
        Console.WriteLine("           AutoID C# .NET 8 Quickstart Client                 ");
        Console.WriteLine("==============================================================\n");

        var baseUrl = "http://127.0.0.1:18080";
        using var http = new HttpClient { BaseAddress = new Uri(baseUrl) };

        try
        {
            // 1. Health Check
            Console.WriteLine("[1] Checking AutoID Edge Gateway health...");
            var health = await http.GetFromJsonAsync<JsonElement>("/api/health");
            Console.WriteLine($"    Gateway Health: {health.GetProperty("status").GetString()}");

            // 2. Start WebSocket Streaming
            var wsUri = new Uri("ws://127.0.0.1:18080/ws/events");
            using var ws = new ClientWebSocket();
            Console.WriteLine($"[2] Connecting WebSocket to {wsUri}...");
            await ws.ConnectAsync(wsUri, CancellationToken.None);
            Console.WriteLine("    Connected! Listening for live tag reads for 10 seconds...\n");

            var cts = new CancellationTokenSource(TimeSpan.FromSeconds(10));
            var buffer = new byte[4096];

            while (!cts.IsCancellationRequested && ws.State == WebSocketState.Open)
            {
                var result = await ws.ReceiveAsync(new ArraySegment<byte>(buffer), cts.Token);
                if (result.MessageType == WebSocketMessageType.Close) break;

                var jsonStr = Encoding.UTF8.GetString(buffer, 0, result.Count);
                using var doc = JsonDocument.Parse(jsonStr);
                var root = doc.RootElement;
                var eventType = root.GetProperty("type").GetString();

                if (eventType == "tag")
                {
                    var p = root.GetProperty("payload");
                    Console.WriteLine($"    [TAG] EPC: {p.GetProperty("epc").GetString()} | RSSI: {p.GetProperty("rssi").GetInt32()} dBm | Ant: {p.GetProperty("antenna").GetInt32()}");
                }
                else if (eventType == "direction")
                {
                    var p = root.GetProperty("payload");
                    Console.WriteLine($"    ⭐ [TRANSIT] EPC: {p.GetProperty("epc").GetString()} | Direction: {p.GetProperty("direction").GetString()} | Conf: {p.GetProperty("confidence").GetDouble():P0}");
                }
            }
        }
        catch (OperationCanceledException)
        {
            Console.WriteLine("\n[Completed] 10-second streaming session finished.");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"\n[Note] Could not reach gateway at {baseUrl}: {ex.Message}");
            Console.WriteLine("Start the AutoID Edge Gateway container to enable live streaming.");
        }
    }
}
