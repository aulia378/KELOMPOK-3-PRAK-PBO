// Kelas Barang merepresentasikan barang dengan atribut ID, nama, harga, stok, deskripsi, brand.
public class Barang {
    private String id;
    private String nama;
    private double harga;
    private int stok;
    private String deskripsi;
    private String brand;

    // Konstruktor lengkap untuk seluruh atribut
    public Barang(String id, String nama, double harga, int stok, String deskripsi, String brand) {
        this.id = id == null ? "" : id;
        this.nama = nama == null ? "" : nama;
        this.harga = harga;
        this.stok = stok;
        this.deskripsi = deskripsi == null ? "" : deskripsi;
        this.brand = brand == null ? "" : brand;
    }

    // Konstruktor ringkas (kompatibel dengan format file lama: hanya id, nama, harga)
    public Barang(String id, String nama, double harga) {
        this(id, nama, harga, 0, "", "");
    }

    // Getter untuk mendapatkan ID barang
    public String getId() {
        return id;
    }

    // Getter untuk mendapatkan nama barang
    public String getNama() {
        return nama;
    }

    // Getter untuk mendapatkan harga barang
    public double getHarga() {
        return harga;
    }

    // Getter deskripsi
    public String getDeskripsi() {
        return deskripsi;
    }

    // Getter brand (dua nama untuk kompatibilitas)
    public String getBrand() {
        return brand;
    }
    public String getbrand() {
        return brand;
    }

    // Getter stok
    public int getStok() {
        return stok;
    }

    // Setter stok
    public void setStok(int stok) {
        this.stok = stok;
    }

    // Setter nama
    public void setNama(String nama) {
        this.nama = nama;
    }

    // Setter deskripsi
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    // Setter brand (dua nama untuk kompatibilitas)
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setbrand(String brand) {
        this.brand = brand;
    }

    // Setter harga
    public void setHarga(double harga) {
        this.harga = harga;
    }

    // Representasi barang untuk debugging / tampilan CLI
    @Override
    public String toString() {
        return "ID: " + id + ", Nama: " + nama + ", Harga: Rp " + harga + ", Stok: " + stok + ", Deskripsi: " + deskripsi + ", Brand: " + brand;
    }

    // Parse satu baris dari file (mendukung format CSV lama dan format lengkap)
    public static Barang fromFileString(String line) {
        if (line == null) return null;
        String l = line.trim();
        if (l.isEmpty()) return null;

        // coba parse sebagai CSV (koma) terlebih dahulu, lalu tab, lalu pipe
        String[] parts = l.split(",", -1);
        if (parts.length < 3) {
            if (l.contains("\t")) parts = l.split("\t", -1);
            else if (l.contains("|")) parts = l.split("\\|", -1);
        }

        // trim semua bagian
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        try {
            if (parts.length >= 6) {
                String id = unescape(parts[0]);
                String nama = unescape(parts[1]);
                double harga = Double.parseDouble(parts[2]);
                int stok = Integer.parseInt(parts[3]);
                String deskripsi = unescape(parts[4]);
                String brand = unescape(parts[5]);
                return new Barang(id, nama, harga, stok, deskripsi, brand);
            } else if (parts.length >= 3) {
                String id = unescape(parts[0]);
                String nama = unescape(parts[1]);
                double harga = Double.parseDouble(parts[2]);
                return new Barang(id, nama, harga); // stok/deskripsi/brand default
            } else {
                return null;
            }
        } catch (Exception e) {
            // parsing gagal -> kembalikan null
            return null;
        }
    }

    // Membersihkan karakter aneh dari file (newline, tab)
    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\t", " ").replace("\r", " ").replace("\n", " ").trim();
    }
}
