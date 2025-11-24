import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Transaksi {
    private String id;
    private Customer customer;
    private List<Barang> barangList;
    private double total;
    private long timestamp;
    
    // Field Status & Pembayaran
    private String status; 
    private String metodePembayaran; // [BARU] Menyimpan nama metode bayar (QRIS/COD/dll)

    public Transaksi(Customer customer, List<Barang> items) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase(); 
        this.customer = customer;
        this.barangList = (items == null) ? new ArrayList<>() : new ArrayList<>(items);
        this.timestamp = System.currentTimeMillis();
        this.total = calculateTotal();
        this.status = "Sedang Diproses"; 
        this.metodePembayaran = "-"; // Default
    }

    private double calculateTotal() {
        double sum = 0.0;
        for (Barang b : barangList) {
            if (b != null) sum += b.getHarga();
        }
        return sum;
    }

    // [BARU] Getter & Setter Metode Pembayaran
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metode) { this.metodePembayaran = metode; }

    // Getter & Setter Status
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Getters Lainnya
    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public List<Barang> getBarangList() { return new ArrayList<>(barangList); }
    public double getTotal() { return total; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "ID: " + id + " | Status: " + status + " | Total: " + total;
    }
}