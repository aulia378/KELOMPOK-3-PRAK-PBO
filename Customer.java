import java.util.ArrayList;

// Kelas Customer adalah subclass dari kelas Akun.
// Kelas ini merepresentasikan akun dengan peran sebagai customer
public class Customer extends Akun {
    private Keranjang keranjang;
    private ArrayList<Invoice> invoiceSelesai;

    // Konstruktor Customer yang memanggil konstruktor superclass (Akun)
    public Customer(String id, String username, String password) {
        super(id, username, password);
        this.keranjang = new Keranjang(); // Membuat objek Keranjang baru untuk pelanggan.
        this.invoiceSelesai = new ArrayList<>(); // Inisialisasi ArrayList untuk invoice selesai.
    }

    // Method ini digunakan untuk mengakses keranjang customer.
    public Keranjang getKeranjang() {
        return keranjang;
    }

    // Method ini digunakan untuk mengakses daftar transaksi yang sudah selesai.
    public ArrayList<Invoice> getInvoiceSelesai() {
        return invoiceSelesai;
    }

    // Implementasi method abstract menu() dari kelas Akun.
    // Method ini digunakan untuk menampilkan daftar menu yang tersedia untuk pelanggan.
    @Override
    public void menu() {
        System.out.println("\nMenu Customer:");
        System.out.println("1. Lihat Barang");
        System.out.println("2. Tambah ke Keranjang");
        System.out.println("3. Lihat Keranjang");
        System.out.println("4. Checkout");
        System.out.println("5. Lihat Riwayat Belanja");
        System.out.println("6. Keluar");
    }
}
