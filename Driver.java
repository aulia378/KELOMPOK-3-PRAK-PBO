// Kelas Driver bersifat abstract, digunakan sebagai kelas dasar untuk pengelolaan menu admindriver dan customerdriver.
// Tidak dapat diinstansiasi langsung, tetapi harus diturunkan oleh kelas konkret lainnya.
public abstract class Driver {
    protected Akun akun;

    // Konstruktor untuk menginisialisasi atribut akun.
    public Driver(Akun akun) { 
        this.akun = akun;
    }

    // Method abstract untuk menangani menu.
    // Harus diimplementasikan oleh kelas turunan untuk memberikan logika spesifik pada menu.
    public abstract void handleMenu();
}
