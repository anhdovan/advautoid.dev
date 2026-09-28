# Writing Custom Reader Driver Plugins

The **Adv.SmartSdk** architecture is decoupled from physical hardware via an open **Service Provider Interface (SPI)**. Developers and hardware manufacturers can introduce support for proprietary RFID/barcode readers without modifying the core SDK.

---

## 1. Driver SPI Interface (`IRfidReaderDriver`)

In C# / Native AOT, any driver must implement the lifecycle contract:

```csharp
public interface IRfidReaderDriver : IDisposable
{
    string DriverId { get; }
    int ReaderId { get; }
    ReaderStatus Status { get; }

    event EventHandler<TagReadEventArgs> TagReceived;
    event EventHandler<ReaderStatusChangedEventArgs> StatusChanged;

    Task ConnectAsync(ReaderConnectionConfig config, CancellationToken ct);
    Task DisconnectAsync();
    Task StartInventoryAsync();
    Task StopInventoryAsync();
    Task ApplySettingsAsync(ReaderSetting settings);
}
```

---

## 2. Registering with the AOT Driver Factory

Because Native AOT strips unused reflection metadata, driver registration is explicit:

```csharp
public static class CustomDriverRegistration
{
    public static void Initialize()
    {
        ReaderFactory.RegisterDriver("custom-serial", (id, address) => 
            new MyCustomSerialRfidReader(id, address));
    }
}
```

---

## 3. Emitting Tag Read Events

When raw bytes or packets arrive over UART / TCP, parse the EPC and raise the event:

```csharp
protected void OnPacketParsed(byte[] rawEpc, int rssiDbm, int antenna)
{
    string epcHex = Convert.ToHexString(rawEpc);
    long timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    TagReceived?.Invoke(this, new TagReadEventArgs(
        readerId: this.ReaderId,
        epc: epcHex,
        rssi: rssiDbm,
        antenna: antenna,
        timestamp: timestamp
    ));
}
```

---

## 4. Hardware Simulation with Mock Driver

For unit testing and CI builds without physical readers, use the built-in `"mock"` driver:

```typescript
// Connects to an in-memory loopback generator
await client.createReader({
  driverId: "mock",
  readerId: 99,
  address: "virtual:loopback"
});
```
