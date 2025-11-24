Class Diagram di atas menggambarkan struktur aplikasi GLOWIFY, yaitu sebuah platform online shop untuk produk kosmetik dan skincare. Pada bagian utama terdapat kelas Main yang berfungsi sebagai titik awal aplikasi dan menghubungkan proses login maupun navigasi menu. Sistem memiliki dua jenis pengguna, yaitu Admin dan Customer, yang diwariskan dari kelas Akun. Setelah login, setiap pengguna diarahkan ke kelas Driver, yang kemudian diturunkan menjadi AdminDriver untuk admin dan CustomerDriver untuk customer. AdminDriver digunakan untuk mengelola data produk melalui kelas ListBarang, seperti menambah, menghapus, mengedit, dan melihat stok produk. Sementara itu, CustomerDriver mengatur proses pembelian mulai dari melihat produk, mengelola keranjang belanja melalui kelas Keranjang, hingga melakukan checkout dan transaksi.

Produk dalam aplikasi direpresentasikan oleh kelas Barang, yang menyimpan informasi seperti nama produk, harga, stok, dan deskripsi. Semua barang tersimpan dalam ListBarang menggunakan struktur list. Saat customer melakukan pembelian, barang yang dipilih akan dimasukkan ke dalam Keranjang, yang dapat menghitung total harga dan memperbarui jumlah item. Ketika checkout, sistem akan membuat objek Transaksi yang menyimpan detail pembelian termasuk daftar barang, total biaya, waktu transaksi, status, dan metode pembayaran. Hasil transaksi kemudian diubah menjadi Invoice sebagai bukti pembayaran. Untuk proses pembayaran, digunakan kelas Pembayaran yang memiliki beberapa turunan yaitu QRIS, Bank, dan COD sebagai pilihan metode pembayaran yang dapat dipilih pengguna.

Secara keseluruhan, class diagram ini menerapkan konsep Object-Oriented Programming seperti inheritance, composition, aggregation, dan polymorphism, serta menggambarkan alur lengkap mulai dari pengelolaan produk oleh admin hingga proses belanja dan pembayaran oleh customer.

Konsep OOP Utama yang kami gunakan yaitu:
1. Encapsulation 
2. Inheritance (Pewarisan)
3. Polymorphism (Polimorfisme)
4. Abstraction (Abstraksi)

Fitur Teknis yang kami gunakan yaitu:
1. Collection Framework
2. FIle Handling (I/O)
3. GUI (Java Swing)
4. Exception Handling (Try-Catch)


Nama Anggota Kelompok
1. Muhammad Isra' Aulia     (2408107010006)
2. Ulfa Khairina            (2408107010013)
3. Dara Ramadhani           (2408107010028)
