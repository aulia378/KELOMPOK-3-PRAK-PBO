// Kelas abstrak ini merepresentasikan metode pembayaran secara umum.
public abstract class Pembayaran {
    protected String id;

    public Pembayaran(String id) {
        this.id = id;
    }

    // Metode toString() untuk mengembalikan representasi string dari objek Pembayaran,
    // termasuk nama kelas dan ID metode pembayaran.
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " - ID: " + id;
    }
}

// Kelas QRIS merupakan subclass dari Pembayaran, yang merepresentasikan metode pembayaran QRIS.
class QRIS extends Pembayaran {
    public QRIS() {
        super("QRIS001");
    }
}

// Kelas Bank merupakan subclass dari Pembayaran, yang merepresentasikan metode pembayaran melalui bank.
class Bank extends Pembayaran {
    public Bank() {
        super("BANK001");
    }
}

// Kelas COD merupakan subclass dari Pembayaran, yang merepresentasikan metode pembayaran tunai saat pengiriman.
class COD extends Pembayaran {
    public COD() {
        super("COD001");
    }
}

