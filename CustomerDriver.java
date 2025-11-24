import java.util.*;
import java.util.stream.Collectors;

// Driver untuk akun Customer.
// Mengelola tampilan menu, daftar barang, keranjang, checkout, dan riwayat transaksi.

public class CustomerDriver extends Driver {
    private ListBarang listBarang; // sumber data produk
    private AdminDriver adminDriver; // untuk mengirim transaksi agar disetujui admin

    // Kolom lebar untuk tabel (sesuaikan jika perlu)
    private static final int W_ID = 8;
    private static final int W_NAME = 22;
    private static final int W_PRICE = 12;
    private static final int W_STOCK = 6;
    private static final int W_DESC = 30;
    private static final int W_BRAND = 16;

    public CustomerDriver(Customer customer, ListBarang listBarang, AdminDriver adminDriver) {
        super(customer);
        this.listBarang = listBarang;
        this.adminDriver = adminDriver;
    }

    // Membersihkan karakter newline/tab dari teks untuk tampilan tabel
    private static String clean(String s) {
        if (s == null) return "";
        return s.replace("\r", " ").replace("\n", " ").replace("\t", " ").trim();
    }

    // Memotong teks agar sesuai lebar kolom
    private static String truncate(String s, int max) {
        s = clean(s);
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    // Mencetak garis pemisah untuk tabel
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

    // Header tabel daftar barang
    private static void printHeader() {
        printSeparator();
        String fmt = "| %-" + W_ID + "s | %-" + W_NAME + "s | %" + W_PRICE + "s | %" + W_STOCK + "s | %-" + W_DESC + "s | %-" + W_BRAND + "s |%n";
        System.out.printf(fmt, "ID", "Nama Barang", "Harga", "Stok", "Deskripsi", "Brand");
        printSeparator();
    }

    // Mencetak satu baris produk
    private static void printRow(String id, String name, double price, int stock, String desc, String brand) {
        String fmt = "| %-" + W_ID + "s | %-" + W_NAME + "s | %" + W_PRICE + ".2f | %" + W_STOCK + "d | %-" + W_DESC + "s | %-" + W_BRAND + "s |%n";
        System.out.printf(fmt, id, name, price, stock, desc, brand);
    }

    //Menu utama Customer.
    //Mengatur navigasi melihat barang, keranjang, checkout, dan riwayat.
    @Override
    public void handleMenu() {
        Scanner sc = new Scanner(System.in);
        Customer customer = (Customer) akun;

        while (true) {
            customer.menu();
            int choice = InputUtils.readIntInRange(sc, 1, 6, "Pilih: ");

            switch (choice) {

                // =======================
                // 1. TAMPILKAN DAFTAR BARANG
                // =======================
                case 1:
                    System.out.println("Daftar Barang:");
                    printHeader();
                    for (Barang barang : listBarang.getBarangList()) {
                        printRow(
                            truncate(barang.getId(), W_ID),
                            truncate(barang.getNama(), W_NAME),
                            barang.getHarga(),
                            barang.getStok(),
                            truncate(barang.getDeskripsi(), W_DESC),
                            truncate(barang.getBrand(), W_BRAND)
                        );
                    }
                    printSeparator();
                    break;

                case 2:
                    // =======================
                    // 2. MASUKKAN BARANG KE KERANJANG
                    // =======================
                    System.out.print("ID Barang: ");
                    String idBarang = sc.nextLine().trim();
                    Barang barang = listBarang.getBarang(idBarang);
                    if (barang == null) {
                        System.out.println("Barang tidak ditemukan.");
                        break;
                    }
                    int available = barang.getStok();
                    if (available <= 0) {
                        System.out.println("Stok barang habis.");
                        break;
                    }
                    // minta jumlah yang ingin dibeli, batasi oleh stok tersedia
                    int qty = InputUtils.readIntInRange(sc, 1, available, "Jumlah yang ingin dibeli (max " + available + "): ");
                    // panggil Keranjang.addBarang(Barang, qty) yang menggabungkan jika id sama
                    customer.getKeranjang().addBarang(barang, qty);
                    System.out.println(qty + " unit '" + barang.getNama() + "' berhasil ditambahkan ke keranjang.");
                    break;

                case 3:
                    // =======================
                    // 3. LIHAT KERANJANG
                    // =======================
                    System.out.println("Keranjang:");
                    List<CartItem> cartItems = customer.getKeranjang().getItems();
                    if (cartItems == null || cartItems.isEmpty()) {
                        System.out.println("Keranjang kosong.");
                    } else {
                        printHeader();
                        for (CartItem ci : cartItems) {
                            Barang b = ci.getBarang();
                            int q = ci.getQty();
                            // tampilkan qty di kolom "Stok" (sesuai permintaan)
                            printRow(
                                truncate(b.getId(), W_ID),
                                truncate(b.getNama(), W_NAME),
                                b.getHarga(),
                                q,
                                truncate(b.getDeskripsi(), W_DESC),
                                truncate(b.getBrand(), W_BRAND)
                            );
                        }
                        printSeparator();
                    }
                    break;

                case 4:
                    // =======================
                    // 4. CHECKOUT
                    // =======================
                    // Checkout: pilih item(s) dari keranjang (gunakan CartItem list)
                    List<CartItem> keranjangItems = customer.getKeranjang().getItems();
                    if (keranjangItems == null || keranjangItems.isEmpty()) {
                        System.out.println("Keranjang kosong.");
                        break;
                    }

                    System.out.println("Keranjang Anda:");
                    // Tampilkan isi keranjang sebelum checkout
                    printHeader();
                    for (CartItem ci : keranjangItems) {
                        Barang b = ci.getBarang();
                        int q = ci.getQty();
                        printRow(
                            truncate(b.getId(), W_ID),
                            truncate(b.getNama(), W_NAME),
                            b.getHarga(),
                            q,
                            truncate(b.getDeskripsi(), W_DESC),
                            truncate(b.getBrand(), W_BRAND)
                        );
                    }
                    printSeparator();

                    // Pilih ID untuk checkout
                    System.out.println("Masukkan ID barang yang ingin di-checkout (pisah koma), atau ketik 'all' untuk semua:");
                    System.out.print("Pilihan: ");
                    String line = sc.nextLine().trim();
                    Set<String> selectedIds = new HashSet<>();
                    List<CartItem> selectedCartItems = new ArrayList<>();

                    // Pilih semua item
                    if (line.equalsIgnoreCase("all")) {
                        selectedCartItems.addAll(keranjangItems);
                        selectedIds.addAll(keranjangItems.stream().map(ci -> ci.getBarang().getId()).collect(Collectors.toSet()));
                    } else {
                        // Pilih item tertentu
                        String[] parts = line.split(",");
                        for (String p : parts) {
                            String id = p.trim();
                            if (id.isEmpty()) continue;
                            Optional<CartItem> opt = keranjangItems.stream().filter(ci -> id.equals(ci.getBarang().getId())).findFirst();
                            if (opt.isPresent()) {
                                if (!selectedIds.contains(id)) {
                                    selectedIds.add(id);
                                    selectedCartItems.add(opt.get());
                                }
                            } else {
                                System.out.println("ID tidak ditemukan di keranjang: " + id);
                            }
                        }
                    }

                    if (selectedCartItems.isEmpty()) {
                        System.out.println("Tidak ada barang yang dipilih untuk checkout.");
                        break;
                    }

                    // untuk setiap CartItem terpilih, tanyakan berapa qty yang ingin di-checkout (1..availableInCart)
                    Map<String, Integer> qtyToCheckout = new HashMap<>();
                    for (CartItem ci : selectedCartItems) {
                        String id = ci.getBarang().getId();
                        int availableInCart = ci.getQty();
                        int chosen = InputUtils.readIntInRange(sc, 1, availableInCart,
                                "Masukkan jumlah yang ingin di-checkout untuk ID " + id + " (max " + availableInCart + "): ");
                        qtyToCheckout.put(id, chosen);
                    }

                    // metode pembayaran
                    System.out.println("Pilih metode pembayaran: ");
                    System.out.println("1. QRIS");
                    System.out.println("2. Bank");
                    System.out.println("3. COD");
                    int metode = InputUtils.readIntInRange(sc, 1, 3, "Pilihan Anda: ");
                    Pembayaran pembayaran;
                    switch (metode) {
                        case 1: pembayaran = new QRIS(); break;
                        case 2: pembayaran = new Bank(); break;
                        case 3: pembayaran = new COD(); break;
                        default:
                            System.out.println("Pilihan tidak valid.");
                            continue;
                    }

                    // Buat list Barang untuk transaksi sesuai qty yang dipilih
                    List<Barang> itemsForTransaction = new ArrayList<>();
                    for (CartItem ci : selectedCartItems) {
                        String id = ci.getBarang().getId();
                        int q = qtyToCheckout.getOrDefault(id, 0);
                        for (int i = 0; i < q; i++) itemsForTransaction.add(ci.getBarang());
                    }

                    Transaksi transaksi = new Transaksi(customer, itemsForTransaction);
                    Invoice invoice = new Invoice(transaksi, pembayaran);
                    customer.getInvoiceSelesai().add(invoice);
                    adminDriver.approveTransaksi(transaksi);

                    // kurangi stok di inventory sesuai qty yang di-checkout
                    for (Map.Entry<String, Integer> e : qtyToCheckout.entrySet()) {
                        String id = e.getKey();
                        int q = e.getValue();
                        boolean ok = listBarang.reduceStock(id, q);
                        if (!ok) System.out.println("Peringatan: gagal mengurangi stok untuk ID " + id);
                    }

                    // Perbarui keranjang: kurangi qty sesuai yang dibayar atau hapus item jika qty habis
                    for (CartItem ci : selectedCartItems) {
                        String id = ci.getBarang().getId();
                        int q = qtyToCheckout.getOrDefault(id, 0);
                        if (q >= ci.getQty()) {
                            customer.getKeranjang().removeById(id);
                        } else {
                            customer.getKeranjang().removeQuantity(id, q);
                        }
                    }

                    int totalUnits = itemsForTransaction.size();
                    System.out.println("Checkout berhasil untuk " + totalUnits + " unit dari " + selectedCartItems.size() + " item.");
                    break;

                case 5:
                    // =======================
                    // 5. RIWAYAT TRANSAKSI
                    // =======================
                    if (customer.getInvoiceSelesai().isEmpty()) {
                        System.out.println("Belum ada riwayat belanja.");
                    } else {
                        for (Invoice inv : customer.getInvoiceSelesai()) {
                            System.out.println(inv);
                        }
                    }
                    break;

                case 6:
                    // =======================
                    // 6. LOGOUT
                    // =======================
                    System.out.println("Logout...");
                    return;

                default:
                    System.out.println("Pilihan tidak valid.");
            }
        }
    }
}
