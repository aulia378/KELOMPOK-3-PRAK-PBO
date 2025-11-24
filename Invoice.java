import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Invoice {
    private Transaksi transaksi;
    private Pembayaran pembayaran;

    // Lebar kolom
    private static final int W_ID = 8;
    private static final int W_NAME = 25; 
    private static final int W_PRICE = 15;
    private static final int W_QTY = 6;
    private static final int W_SUB = 15;
    private static final int W_BRAND = 15;

    public Invoice(Transaksi transaksi, Pembayaran pembayaran) {
        this.transaksi = transaksi;
        this.pembayaran = pembayaran;
    }

    // Dibutuhkan GUI untuk membaca status transaksi
    public Transaksi getTransaksi() {
        return transaksi;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Header invoice
        sb.append("========== INVOICE ==========\n");
        sb.append("Trans ID     : ").append(transaksi.getId()).append('\n');
        sb.append("Status       : ").append(transaksi.getStatus()).append('\n'); // Tampilkan Status
        sb.append("Customer     : ").append(transaksi.getCustomer() != null ? transaksi.getCustomer().getUsername() : "unknown").append('\n');
        sb.append("Waktu        : ").append(df.format(new Date(transaksi.getTimestamp()))).append("\n\n");

        // Format tabel
        String headerFmt = "%-" + W_ID + "s  %-"+ W_NAME + "s  %" + W_PRICE + "s  %" + W_QTY + "s  %" + W_SUB + "s  %-"+ W_BRAND + "s%n";
        String rowFmt    = "%-" + W_ID + "s  %-"+ W_NAME + "s  %" + W_PRICE + ".2f  %" + W_QTY + "d  %" + W_SUB + ".2f  %-"+ W_BRAND + "s%n";

        int lineLen = W_ID + 2 + W_NAME + 2 + W_PRICE + 2 + W_QTY + 2 + W_SUB + 2 + W_BRAND;
        
        sb.append(String.format(headerFmt, "ID", "Nama Barang", "Harga", "Qty", "Subtotal", "Brand"));
        sb.append(String.join("", Collections.nCopies(lineLen/1, "-"))).append("\n");

        // Hitung qty per barang
        List<Barang> items = transaksi.getBarangList();
        Map<String, Integer> qtyMap = new LinkedHashMap<>();
        Map<String, Barang> rep = new HashMap<>();
        for (Barang b : items) {
            if (b == null) continue;
            qtyMap.put(b.getId(), qtyMap.getOrDefault(b.getId(), 0) + 1);
            rep.putIfAbsent(b.getId(), b);
        }

        // Hitung total
        double total = 0.0;
        for (Map.Entry<String,Integer> e : qtyMap.entrySet()) {
            String id = e.getKey();
            int qty = e.getValue();
            Barang b = rep.get(id);
            double price = b != null ? b.getHarga() : 0.0;
            double sub = price * qty;
            total += sub;
            sb.append(String.format(rowFmt,
                    id,
                    truncate(b != null ? b.getNama() : "", W_NAME),
                    price,
                    qty,
                    sub,
                    truncate(b != null ? b.getBrand() : "", W_BRAND)
            ));
        }

        sb.append(String.join("", Collections.nCopies(lineLen/1, "-"))).append("\n");

        // Total pembayaran
        sb.append(String.format("%-" + (W_ID+W_NAME+2) + "s  %" + (W_PRICE+W_QTY+W_SUB) + ".2f%n", "TOTAL:", total));

         // Metode pembayaran
        sb.append("\nMetode Bayar : ").append(pembayaran != null ? pembayaran.toString() : "null").append("\n");
        sb.append("=============================\n");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("Invoice.txt", true))) {
            bw.write(sb.toString());
            bw.newLine();
        } catch (IOException ex) {}

        return sb.toString();
    }

    // Memotong nama apabila melebihi lebar kolom
    private static String truncate(String s, int max) {
        if (s == null) return "";
        String t = s.replace("\r", " ").replace("\n", " ").trim();
        if (t.length() <= max) return t;
        return t.substring(0, Math.max(0, max - 3)) + "...";
    }
}