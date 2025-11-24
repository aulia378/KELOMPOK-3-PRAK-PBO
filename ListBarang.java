import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Menyimpan dan memproses data Barang + integrasi file
public class ListBarang implements DataStorage {

    private List<Barang> barangList = new ArrayList<>();
    private String filePath = "barang.txt"; // lokasi penyimpanan data barang

    public ListBarang() {}

    // Mengubah lokasi file jika diperlukan (validasi sederhana)
    public void setFilePath(String path) {
        if (path != null && !path.trim().isEmpty()) filePath = path;
    }

    // Mengambil list barang terbaru dari file (read-only)
    public List<Barang> getBarangList() {
        loadData(); 
        return Collections.unmodifiableList(barangList);
    }

    // Mencari satu barang berdasarkan ID
    public Barang getBarang(String id) {
        loadData();
        if (id == null) return null;
        for (Barang b : barangList) 
            if (id.equals(b.getId())) return b;
        return null;
    }

    // Generate ID otomatis berdasarkan angka terbesar di file
    public String generateId() {
        loadData();
        int max = 0;
        for (Barang b : barangList) {
            try {
                String digits = b.getId().replaceAll("\\D+", "");
                if (!digits.isEmpty()) {
                    int v = Integer.parseInt(digits);
                    if (v > max) max = v;
                }
            } catch (Exception ignored) {}
        }
        return String.valueOf(max + 1);
    }

    // ================================
    //      IMPLEMENTASI INTERFACE
    // ================================

    @Override
    public void loadData() {
        barangList.clear();
        File f = new File(filePath);
        if (!f.exists()) return; // jika file belum ada, list tetap kosong

        // Membaca file dengan aman (try-with-resources)
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Parsing fleksibel mendukung CSV lama & format baru
                Barang b = Barang.fromFileString(line);
                if (b != null) barangList.add(b);
            }
        } catch (IOException e) {
            // Penting untuk debugging jika file rusak atau tidak terbaca
            System.err.println("Error saat membaca data barang: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void saveData() {
        File f = new File(filePath);

        // Menulis ulang isi file setiap kali save (overwrite)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
            for (Barang b : barangList) {
                // Format penyimpanan: CSV
                String line = b.getId() + "," + escape(b.getNama()) + "," + b.getHarga()
                        + "," + b.getStok() + "," + escape(b.getDeskripsi()) + "," + escape(b.getBrand());
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saat menyimpan data barang: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================================
    //      OPERASI CRUD BARANG
    // ================================

    public void addBarang(Barang b) {
        if (b == null) return;
        loadData();      
        barangList.add(b);
        saveData();      // langsung simpan ke file
    }

    public boolean removeBarang(String id) {
        loadData();
        Barang target = getBarang(id);
        if (target != null) {
            barangList.remove(target);
            saveData();
            return true;
        }
        return false;
    }

    // Edit lengkap semua atribut barang
    public boolean editBarangFull(String id, String nama, double harga, int stok,
                                  String deskripsi, String brand) {
        loadData();
        Barang target = getBarang(id);
        if (target != null) {
            target.setNama(nama);
            target.setHarga(harga);
            target.setStok(stok);
            target.setDeskripsi(deskripsi);
            target.setBrand(brand);
            saveData();
            return true;
        }
        return false;
    }

    // Mengurangi stok (validasi stok cukup)
    public boolean reduceStock(String id, int qty) {
        if (id == null || qty <= 0) return false;
        loadData();
        for (Barang b : barangList) {
            if (id.equals(b.getId())) {
                if (b.getStok() < qty) return false; // stok tidak cukup
                b.setStok(b.getStok() - qty);
                saveData();
                return true;
            }
        }
        return false;
    }

    // Kompatibilitas: method lama panggil loadData()
    public void loadFromFile() { loadData(); }

    // Membersihkan karakter yang mengganggu format CSV
    private String escape(String s) {
        if (s == null) return "";
        return s.replace(",", " ")
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }
}
