import com.beetech.adv.smartsdk.SmartSdk;
import com.beetech.adv.smartsdk.SmartSdk.SmartSdkReader;
import com.beetech.adv.smartsdk.SmartSdk.TagReadEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ReaderApiSample {

    record ReaderOption(String key, String driverId, String displayName, String defaultAddress, String description) {}

    static class TagRecord {
        final int index;
        final String epc;
        volatile int antennaPort;
        volatile int peakRssi;
        volatile int readCount;
        final Instant firstSeen;
        volatile Instant lastSeen;

        TagRecord(int index, String epc, int antennaPort, int peakRssi, Instant now) {
            this.index = index;
            this.epc = epc;
            this.antennaPort = antennaPort;
            this.peakRssi = peakRssi;
            this.readCount = 1;
            this.firstSeen = now;
            this.lastSeen = now;
        }
    }

    private static final List<ReaderOption> CATALOG = List.of(
        new ReaderOption("1", "impinj-r700", "Impinj R700", "192.168.10.101", "High-performance IoT / REST & LLRP (Default port 443/5084)"),
        new ReaderOption("2", "impinj-r420", "Impinj Speedway R420", "192.168.10.101", "Enterprise LLRP fixed reader (Default port 5084)"),
        new ReaderOption("3", "impinj-r420-ext", "Impinj Speedway R420 Extended", "192.168.10.101", "Extended LLRP driver with advanced antenna telemetry"),
        new ReaderOption("4", "urovo-fr2000", "Urovo FR2000 / RF2000", "192.168.10.101:20058", "Desktop compact reader (Default TCP 20058 or USB)"),
        new ReaderOption("5", "zebra-fx9600", "Zebra FX9600", "192.168.10.101", "Industrial 4-port / 8-port LLRP fixed reader"),
        new ReaderOption("6", "unitech-rs804", "Unitech RS804", "192.168.10.101", "Industrial 4-port LLRP fixed reader"),
        new ReaderOption("7", "caen", "Caen Desktop Reader", "USB", "Caen desktop reader (USB / Virtual COM)"),
        new ReaderOption("8", "chainway-r3", "Chainway R3", "COM1", "Chainway desktop/fixed reader (Serial COM port)"),
        new ReaderOption("9", "mock-reader", "Virtual / Mock Reader", "127.0.0.1", "Simulated tag loopback generator for offline testing")
    );

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    public static void main(String[] args) throws Exception {
        System.out.println("=======================================================================================");
        System.out.println("         Beetech Adv.SmartSdk - Universal RFID Reader Interactive API (Java 21)        ");
        System.out.println("=======================================================================================");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try (SmartSdk sdk = new SmartSdk()) {
            sdk.setDevLicenseBypass(true);

            String fp = sdk.getHardwareFingerprint();
            System.out.println("Hardware Fingerprint: " + fp);

            System.out.println("\nAvailable RFID Reader Drivers:");
            for (var opt : CATALOG) {
                System.out.printf("  [%s] %-30s | %s%n", opt.key(), opt.displayName(), opt.description());
            }

            // Reader selection
            ReaderOption selected = null;
            while (selected == null) {
                System.out.print("\nSelect reader [1-9] (Default: 9 - Virtual / Mock Reader): ");
                String line = reader.readLine();
                String choice = (line == null || line.trim().isEmpty()) ? "9" : line.trim();
                for (var opt : CATALOG) {
                    if (opt.key().equals(choice)) {
                        selected = opt;
                        break;
                    }
                }
                if (selected == null) {
                    System.out.println("  Invalid selection. Please choose a number between 1 and 9.");
                }
            }

            System.out.println("Selected: " + selected.displayName() + " (" + selected.driverId() + ")");

            // IP input
            System.out.printf("%nEnter Reader IP / Address [Default: %s]: ", selected.defaultAddress());
            String addrLine = reader.readLine();
            String address = (addrLine == null || addrLine.trim().isEmpty()) ? selected.defaultAddress() : addrLine.trim();
            System.out.println("Configured Endpoint: " + address);

            // Connect
            System.out.printf("%n[INFO] Initializing native driver for %s...%n", selected.displayName());
            SmartSdkReader rfidReader = null;
            try {
                rfidReader = sdk.createReader(selected.driverId(), 101, address);
            } catch (Exception ex) {
                System.out.println("[ERROR] Failed to initialize reader: " + ex.getMessage());
                return;
            }

            System.out.printf("[INFO] Connecting to %s at %s... ", selected.displayName(), address);
            boolean connected = rfidReader.connect();
            if (!connected) {
                System.out.println("FAILED (Device offline or unreachable)");
                if (!selected.driverId().equals("mock-reader")) {
                    System.out.print("Would you like to fallback to Virtual Mock Reader for live test? (Y/n): ");
                    String fallback = reader.readLine();
                    if (fallback == null || fallback.trim().isEmpty() || fallback.trim().equalsIgnoreCase("y")) {
                        rfidReader.close();
                        rfidReader = sdk.createReader("mock-reader", 101, "127.0.0.1");
                        if (rfidReader.connect()) {
                            System.out.println("[INFO] Switched to Virtual Mock Reader successfully.\n");
                        } else {
                            System.out.println("[ERROR] Could not connect to mock reader.");
                            return;
                        }
                    } else {
                        rfidReader.close();
                        return;
                    }
                } else {
                    rfidReader.close();
                    return;
                }
            } else {
                System.out.println("SUCCESS!");
            }

            // Tracking
            Map<String, TagRecord> inventory = new ConcurrentHashMap<>();
            AtomicLong totalReads = new AtomicLong(0);
            Instant startTime = Instant.now();

            rfidReader.addTagReadListener(evt -> {
                long count = totalReads.incrementAndGet();
                Instant now = Instant.now();

                TagRecord rec = inventory.computeIfAbsent(evt.epc(), epc -> 
                    new TagRecord(inventory.size() + 1, epc, evt.antennaPort(), evt.rssi(), now)
                );

                synchronized (rec) {
                    rec.readCount++;
                    rec.lastSeen = now;
                    rec.peakRssi = Math.max(rec.peakRssi, evt.rssi());
                    rec.antennaPort = evt.antennaPort();
                }

                System.out.printf("  [%s] [TAG DETECTED] EPC=%s | Ant=%d | RSSI=%4d dBm | Total Reads=%d%n",
                    TIME_FMT.format(now), evt.epc(), evt.antennaPort(), evt.rssi(), count);
            });

            System.out.println("\n[INFO] Starting live inventory scan...");
            rfidReader.startInventory();

            System.out.println("\n>>> Scanning active. Live tags streaming below.");
            System.out.println(">>> PRESS ENTER OR ANY INPUT TO STOP INVENTORY AND SHOW INVENTORY LIST <<<\n");

            // Wait for key press or auto-timeout if piped
            if (System.console() == null) {
                // Non-interactive / piped input: wait 5s
                Thread.sleep(5000);
            } else {
                reader.readLine();
            }

            // Stop
            System.out.println("\n[INFO] Stopping inventory and disconnecting reader...");
            rfidReader.stopInventory();
            rfidReader.disconnect();
            rfidReader.close();

            long durationMillis = System.currentTimeMillis() - startTime.toEpochMilli();
            double durationSec = durationMillis / 1000.0;

            // Display Inventory Summary Table
            System.out.println();
            System.out.println("=".repeat(120));
            String title = String.format("INVENTORY LIST - %d UNIQUE TAGS FOUND", inventory.size());
            int pad = (120 - title.length()) / 2;
            System.out.println(" ".repeat(Math.max(0, pad)) + title);
            System.out.println("=".repeat(120));
            System.out.printf("%-4s | %-32s | %-4s | %-9s | %-6s | %-12s | %-12s%n",
                "#", "EPC (Electronic Product Code)", "Ant", "Peak RSSI", "Reads", "First Seen", "Last Seen");
            System.out.println("-".repeat(120));

            if (inventory.isEmpty()) {
                System.out.println("  No tags were detected during this session.");
            } else {
                List<TagRecord> list = new ArrayList<>(inventory.values());
                list.sort(Comparator.comparingInt(r -> r.index));
                for (var r : list) {
                    System.out.printf("%-4d | %-32s | Ant%-1d | %5d dBm | %6d | %-12s | %-12s%n",
                        r.index, r.epc, r.antennaPort, r.peakRssi, r.readCount, 
                        TIME_FMT.format(r.firstSeen), TIME_FMT.format(r.lastSeen));
                }
            }

            System.out.println("=".repeat(120));
            double rate = durationSec > 0 ? (totalReads.get() / durationSec) : 0;
            System.out.printf("Session Summary: Total Reads: %d | Unique Tags: %d | Duration: %.2fs | Rate: %.1f tags/s%n",
                totalReads.get(), inventory.size(), durationSec, rate);
            System.out.println("=".repeat(120));
        }
    }
}
