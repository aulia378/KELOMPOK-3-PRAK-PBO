import java.util.ArrayList;
import java.util.Scanner;

public class AdminDriver extends Driver {
    private ListBarang listBarang; // menyimpan & mengelola data barang
    private ArrayList<Transaksi> listTransaksi; // menyimpan transaksi yang sudah disetujui admin

    // Lebar kolom untuk tampilan tabel CLI
    private static final int W_ID = 8;
    private static final int W_NAME = 22;
    private static final int W_PRICE = 12;
    private static final int W_STOCK = 6;
    private static final int W_DESC = 30;
    private static final int W_BRAND = 16;

    public AdminDriver(Admin admin, ListBarang listBarang) {
        super(admin);
        this.listBarang = listBarang;
        this.listTransaksi = new ArrayList<>();
    }

    // Menghapus karakter aneh dari text (supaya tabel rapi)
    private static String clean(String s) {
        if (s == null) return "";
        return s.replace("\r", " ").replace("\n", " ").replace("\t", " ").trim();
    }

    // Memotong text agar tidak melewati panjang kolom tabel
    private static String truncate(String s, int max) {
        s = clean(s);
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    // Membuat garis pemisah tabel
    private static void printSeparator() {
        int[] widths = {W_ID, W_NAME, W_PRICE, W_STOCK, W_DESC, W_BRAND};
        StringBuilder sb = new StringBuilder();
        for (int w : widths) {
            sb.append("+");
            for (int i = 0; i < w + 2; i++) sb.append("-");
        }
        sb.append("+");
        System.out.println(sb.toString());
    }

    // Menampilkan header tabel
    private static void printHeader() {
        printSeparator();
        String fmt = "| %-" + W_ID + "s | %-" + W_NAME + "s | %" + W_PRICE + "s | %" + W_STOCK + "s | %-" + W_DESC + "s | %-" + W_BRAND + "s |%n";
        System.out.printf(fmt, "ID", "Nama Barang", "Harga", "Stok", "Deskripsi", "Brand");
        printSeparator();
    }

    // Menampilkan 1 baris data barang
    private static void printRow(String id, String name, double price, int stock, String desc, String brand) {
        String fmt = "| %-" + W_ID + "s | %-" + W_NAME + "s | %" + W_PRICE + ".2f | %" + W_STOCK + "d | %-" + W_DESC + "s | %-" + W_BRAND + "s |%n";
        System.out.printf(fmt, id, truncate(name, W_NAME), price, stock, truncate(desc, W_DESC), truncate(brand, W_BRAND));
    }

    @Override
    public void handleMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            akun.menu(); // tampilkan menu untuk admin
            int choice = InputUtils.readIntInRange(sc, 1, 6, "Pilih: ");
            switch (choice) {

                // ======================= TAMBAH BARANG ==========================
                case 1:
                    System.out.print("ID Barang (kosong untuk auto-generate): ");
                    String idInput = sc.nextLine().trim();
                    String id;

                    // Cek apakah ID sudah dipakai
                    while (true) {
                        if (idInput.isEmpty()) { id = listBarang.generateId(); break; }
                        if (listBarang.getBarang(idInput) != null) {
                            System.out.println("ID sudah ada. Masukkan ID lain atau kosong untuk auto-generate.");
                            System.out.print("ID Barang (kosong untuk auto-generate): ");
                            idInput = sc.nextLine().trim();
                        } else { id = idInput; break; }
                    }

                     // Input data barang
                    System.out.print("Nama Barang: ");
                    String nama = sc.nextLine();
                    double harga = InputUtils.readDouble(sc, "Harga: ");
                    int stok = InputUtils.readPositiveInt(sc, "Stok: ");
                    System.out.print("Deskripsi: ");
                    String deskripsi = sc.nextLine();
                    System.out.print("Brand: ");
                    String brand = sc.nextLine();
                    listBarang.addBarang(new Barang(id, nama, harga, stok, deskripsi, brand));
                    System.out.println("Barang berhasil ditambahkan. ID = " + id);
                    break;

                // ======================= HAPUS BARANG ==========================
                case 2:
                    System.out.print("ID Barang yang akan dihapus: ");
                    String idHapus = sc.nextLine().trim();

                    // Bila daftar disimpan dalam file, removeBarang juga mengupdate file                    
                    if (listBarang.removeBarang(idHapus)) System.out.println("Barang dihapus.");
                    else System.out.println("Barang tidak ditemukan.");
                    break;

                // ======================= EDIT BARANG ==========================                    
                case 3:
                    System.out.print("ID Barang yang akan diedit: ");
                    String idEdit = sc.nextLine().trim();
                    Barang target = listBarang.getBarang(idEdit);
                    if (target == null) {
                        System.out.println("Barang tidak ditemukan.");
                        break;
                    }

                    // Semua field bisa dikosongkan untuk mempertahankan nilai lama                    
                    System.out.println("Biarkan kosong untuk mempertahankan nilai saat ini.");

                    System.out.println("Nama saat ini: " + target.getNama());
                    System.out.print("Nama baru: ");
                    String nNama = sc.nextLine().trim();
                    if (nNama.isEmpty()) nNama = target.getNama();

                    System.out.println("Harga saat ini: " + target.getHarga());
                    System.out.print("Harga baru: ");
                    String sHarga = sc.nextLine().trim();
                    double nHarga = sHarga.isEmpty() ? target.getHarga() : Double.parseDouble(sHarga);

                    System.out.println("Stok saat ini: " + target.getStok());
                    System.out.print("Stok baru: ");
                    String sStok = sc.nextLine().trim();
                    int nStok = sStok.isEmpty() ? target.getStok() : Integer.parseInt(sStok);

                    System.out.println("Deskripsi saat ini: " + target.getDeskripsi());
                    System.out.print("Deskripsi baru: ");
                    String nDesc = sc.nextLine().trim();
                    if (nDesc.isEmpty()) nDesc = target.getDeskripsi();

                    System.out.println("Brand saat ini: " + target.getBrand());
                    System.out.print("Brand baru: ");
                    String nBrand = sc.nextLine().trim();
                    if (nBrand.isEmpty()) nBrand = target.getBrand();

                    boolean edited = listBarang.editBarangFull(idEdit, nNama, nHarga, nStok, nDesc, nBrand);
                    if (edited) System.out.println("Barang berhasil diupdate.");
                    else System.out.println("Gagal mengupdate barang.");
                    break;

                // ======================= TAMPILKAN SEMUA BARANG ==========================
                case 4:
                    System.out.println("Daftar Barang:");
                    printHeader();
                    for (Barang b : listBarang.getBarangList()) {
                        printRow(
                            b.getId(),
                            b.getNama(),
                            b.getHarga(),
                            b.getStok(),
                            b.getDeskripsi(),
                            b.getBrand()
                        );
                    }
                    printSeparator();
                    break;

                // ======================= LIHAT TRANSAKSI ==========================
                case 5:
                    if (listTransaksi.isEmpty()) System.out.println("Belum ada transaksi.");
                    else {
                        for (Transaksi t : listTransaksi) System.out.println(t);
                    }
                    break;

                // ======================= KELUAR ==========================
                case 6:
                    System.out.println("Keluar ke menu utama.");
                    return;

                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }

    // Admin menerima transaksi → transaksi masuk ke riwayat
    public void approveTransaksi(Transaksi transaksi) {
        listTransaksi.add(transaksi);
        System.out.println("Transaksi diterima.");
    }
}
