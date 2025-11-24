import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class LoginManager {

    // Menyimpan seluruh akun yang sudah ter-load ke memori
    private ArrayList<Akun> akunList;

    public LoginManager() {
        this.akunList = new ArrayList<>();
    }

    // Menambahkan akun ke list (tidak langsung menulis ke file)
    public void addAkun(Akun akun) {
        akunList.add(akun);
    }

    // Cek apakah username sudah dipakai (mencegah duplikasi)
    public boolean hasUsername(String username) {
        for (Akun a : akunList) {
            if (a.getUsername().equals(username)) return true;
        }
        return false;
    }

    // Autentikasi login (cek username + password)
    public Akun authenticate(String username, String password) {
        for (Akun a : akunList) {
            if (a.getUsername().equals(username) && a.getPassword().equals(password)) {
                return a;
            }
        }
        return null; // login gagal
    }

    // Mengambil admin pertama yang ditemukan (dipakai untuk akses cepat di sistem)
    public Admin getAnyAdmin() {
        for (Akun a : akunList) {
            if (a instanceof Admin) return (Admin) a;
        }
        return null;
    }

    // Mengambil customer pertama yang ditemukan
    public Customer getAnyCustomer() {
        for (Akun a : akunList) {
            if (a instanceof Customer) return (Customer) a;
        }
        return null;
    }

    // ================================
    //  LOAD AKUN DARI FILE
    //  Format: role,id,username,password
    // ================================
    public void loadFromFile(String filePath) {
        File f = new File(filePath);

        // Pastikan file akun ada, jika tidak buat baru
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {
                System.out.println("Gagal membuat file akun: " + e.getMessage());
                return;
            }
        }

        // Membaca file dan memuat akun ke memori
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue; // format tidak valid, skip

                String role = parts[0];
                String id = parts[1];
                String username = parts[2];
                String password = parts[3];

                // Hindari akun duplikat (jika file rusak)
                if (hasUsername(username)) continue;

                // Buat objek sesuai role
                if (role.equalsIgnoreCase("admin")) {
                    addAkun(new Admin(id, username, password));
                } else if (role.equalsIgnoreCase("customer")) {
                    addAkun(new Customer(id, username, password));
                }
            }

        } catch (Exception e) {
            System.out.println("Gagal membaca file akun: " + e.getMessage());
        }
    }

    // ================================
    //  REGISTER AKUN BARU + SIMPAN KE FILE
    // ================================
    public Akun register(String role, String username, String password, String filePath) {

        // Cegah username duplikat
        if (hasUsername(username)) {
            return null;
        }

        String id = generateIdForRole(role);
        Akun akun = null;

        // Buat akun sesuai role
        if (role.equalsIgnoreCase("admin")) {
            akun = new Admin(id, username, password);
        } else if (role.equalsIgnoreCase("customer")) {
            akun = new Customer(id, username, password);
        } else {
            return null;
        }

        // Tambahkan ke list memory
        addAkun(akun);

        // Simpan ke file
        appendToFile(role, akun, filePath);

        return akun;
    }

    // Membuat ID otomatis: A1, A2... atau C1, C2...
    private String generateIdForRole(String role) {
        int count = 0;

        if (role.equalsIgnoreCase("admin")) {
            for (Akun a : akunList) if (a instanceof Admin) count++;
            return "A" + (count + 1);

        } else { // customer
            for (Akun a : akunList) if (a instanceof Customer) count++;
            return "C" + (count + 1);
        }
    }

    // Menulis akun baru ke file secara append
    private void appendToFile(String role, Akun akun, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {

            bw.write(role + "," + akun.getId() + "," + akun.getUsername() + "," + akun.getPassword());
            bw.newLine();

        } catch (Exception e) {
            System.out.println("Gagal menulis ke file akun: " + e.getMessage());
        }
    }
}
