public class CartItem {
    private Barang barang; // objek barang yang dimasukkan ke keranjang
    private int qty;       // jumlah barang yang dibeli

    // Constructor: memastikan qty minimal 0 (menghindari nilai negatif)
    public CartItem(Barang barang, int qty) {
        this.barang = barang;
        this.qty = Math.max(0, qty);
    }

    public Barang getBarang() { return barang; }
    public int getQty() { return qty; }

    // Setter qty: dicegah agar tidak menjadi negatif
    public void setQty(int qty) { this.qty = Math.max(0, qty); }

    // Menambah jumlah qty (misalnya user memilih tambah 1)
    // Tetap dicegah agar tidak menjadi negatif
    public void addQty(int delta) { this.qty = Math.max(0, this.qty + delta); }

    // Untuk menampilkan item keranjang secara sederhana dalam CLI
    @Override
    public String toString() {
        return barang.getId() + " x" + qty + " (" + barang.getNama() + ")";
    }
}