import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
    
/* ================================================================
 *  StyleUtils
 *  Utility class untuk menyimpan style warna, font, dan style tombol.
 *  Dipakai di seluruh aplikasi untuk konsistensi tampilan GUI.
 * ================================================================ */
class StyleUtils {
    public static final Color PRIMARY = new Color(255, 182, 193);       // Pink lembut (tema utama)
    public static final Color SECONDARY = new Color(255, 240, 245);     // Background lembut
    public static final Color ACCENT = new Color(219, 112, 147);        // Warna aksen untuk tombol penting
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 14);

    /** Utility untuk styling tombol utama */
    public static void styleButton(JButton btn) {
        btn.setBackground(ACCENT); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /** Utility untuk styling tombol sekunder */
    public static void styleButtonSecondary(JButton btn) {
        btn.setBackground(Color.GRAY); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}

/* ================================================================
 *  ImageUtils
 *  Digunakan untuk mengambil gambar produk berdasarkan ID
 *  dan memastikan folder 'img/' sudah ada.
 * ================================================================ */
class ImageUtils {

    /** Membuat folder img jika belum ada */
    public static void initFolder() {
        File f = new File("img");
        if(!f.exists()) f.mkdir();
    }

    /** Mengambil gambar berdasarkan ID barang (id.jpg/png/jpeg) */
    public static ImageIcon getProductImage(String id, int width, int height) {
        String[] exts = {".jpg", ".png", ".jpeg"};
        for(String ext : exts) {
            File f = new File("img/" + id + ext);
            if(f.exists()) {
                ImageIcon icon = new ImageIcon(f.getPath());
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        }
        return null; // Jika tidak ada gambar
    }
}

/* ================================================================
 *  MainFrame (Frame utama aplikasi)
 *  - Mengatur perpindahan Panel (Login/Admin/Customer) memakai CardLayout
 *  - Menginisialisasi data barang dan akun dari file .txt
 *  - Menyimpan transaksi global
 * ================================================================ */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ListBarang listBarang;         // Penyimpanan produk
    private LoginManager loginManager;     // Penyimpanan akun user
    private final String akunFile = "akun.txt";
    private final String barangFile = "barang.txt";
    private Akun currentUser;
    private List<Transaksi> allTransactions = new ArrayList<>();

    public MainFrame() {

        // Membuat folder img
        ImageUtils.initFolder();

        // ListBarang menerapkan interface DataStorage (load & save otomatis)
        listBarang = new ListBarang();
        listBarang.setFilePath(barangFile);
        listBarang.loadData();

        // Menginisialisasi login manager dan load akun dari file
        loginManager = new LoginManager();
        loginManager.loadFromFile(akunFile);

        // Auto buat admin dan customer default jika file masih kosong
        if (loginManager.getAnyAdmin() == null)
            loginManager.register("admin", "admin", "admin123", akunFile);

        if (loginManager.getAnyCustomer() == null)
            loginManager.register("customer", "customer", "customer123", akunFile);

        // Konfigurasi window
        setTitle("Glowify - Online Store");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Menggunakan tema Nimbus (jika tersedia)
        try { 
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); 
        } catch (Exception e) {
            System.err.println("Tema Nimbus tidak tersedia, menggunakan default.");
        }

        // CardLayout untuk mengatur halaman
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panel awal adalah login
        mainPanel.add(new LoginPanel(this), "LOGIN");
        add(mainPanel);
    }

    /** Mengembalikan user ke menu Login */
    public void showLogin() { 
        currentUser = null;  
        cardLayout.show(mainPanel, "LOGIN"); 
    }

    /** Dipanggil ketika login berhasil */
    public void loginSuccess(Akun akun) {
        this.currentUser = akun;

        // Load panel sesuai role pengguna
        if (akun instanceof Admin) {
            mainPanel.add(new AdminPanel(this, (Admin) akun, listBarang), "ADMIN");
            cardLayout.show(mainPanel, "ADMIN");
        } else if (akun instanceof Customer) {
            mainPanel.add(new CustomerPanel(this, (Customer) akun, listBarang), "CUSTOMER");
            cardLayout.show(mainPanel, "CUSTOMER");
        }
    }

    /** Menyimpan transaksi pembelian (dipakai customer) */
    public void addTransaction(Transaksi trx) { allTransactions.add(trx); }

    public List<Transaksi> getAllTransactions() { return allTransactions; }
    public LoginManager getLoginManager() { return loginManager; }
    public String getAkunFile() { return akunFile; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

/* ================================================================
 *  LoginPanel
 *  Panel untuk login dan registrasi akun baru
 * ================================================================ */
class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginPanel(MainFrame frame) {
        this.mainFrame = frame;
        setLayout(new GridBagLayout());
        setBackground(StyleUtils.SECONDARY);

        // Kartu login dengan border & padding
        JPanel card = new JPanel(new GridLayout(6, 1, 10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(StyleUtils.PRIMARY, 1),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        // Komponen
        JLabel lblTitle = new JLabel("        Glowify Login        ", SwingConstants.CENTER);
        lblTitle.setFont(StyleUtils.TITLE_FONT);
        lblTitle.setForeground(StyleUtils.ACCENT);

        txtUser = new JTextField();
        txtUser.setBorder(BorderFactory.createTitledBorder("Username"));

        txtPass = new JPasswordField();
        txtPass.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton btnLogin = new JButton("MASUK");
        StyleUtils.styleButton(btnLogin);

        // Tombol daftar akun
        JButton btnReg = new JButton("Daftar Akun Baru");
        btnReg.setContentAreaFilled(false);
        btnReg.setBorderPainted(false);
        btnReg.setForeground(StyleUtils.ACCENT);

        // Menambahkan ke panel login
        card.add(lblTitle);
        card.add(new JLabel("Welcome to Glowify!", SwingConstants.CENTER));
        card.add(txtUser);
        card.add(txtPass);
        card.add(btnLogin);
        card.add(btnReg);
        add(card);

        // Event tombol login
        btnLogin.addActionListener(e -> processLogin());

        // Event tombol register
        btnReg.addActionListener(e -> processRegister());
    }

    /** Memproses login user */
    private void processLogin() {
        Akun akun = mainFrame.getLoginManager().authenticate(
                txtUser.getText(),
                new String(txtPass.getPassword())
        );

        if (akun != null) {
            mainFrame.loginSuccess(akun);
            txtUser.setText("");
            txtPass.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Login Gagal! Cek username/password.",
                    "Error Akses",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Membuka form registrasi akun baru */
    private void processRegister() {
        JPanel p = new JPanel(new GridLayout(0,1));
        JTextField u = new JTextField();
        JTextField pass = new JTextField();
        JComboBox<String> role = new JComboBox<>(new String[]{"Customer", "Admin"});

        // Form input
        p.add(new JLabel("Role:"));
        p.add(role);
        p.add(new JLabel("Username:"));
        p.add(u);
        p.add(new JLabel("Password:"));
        p.add(pass);

        if (JOptionPane.showConfirmDialog(this, p, "Register",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

            // Validasi username unik
            if(!mainFrame.getLoginManager().hasUsername(u.getText())) {

                // Simpan akun baru ke file
                mainFrame.getLoginManager().register(
                        role.getSelectedItem().toString().toLowerCase(),
                        u.getText(),
                        pass.getText(),
                        mainFrame.getAkunFile()
                );

                JOptionPane.showMessageDialog(this, "Registrasi Berhasil!");
            } 
            else {
                JOptionPane.showMessageDialog(this,
                        "Username sudah ada!",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}




class CustomerPanel extends JPanel {
    private MainFrame mainFrame;
    private Customer customer;
    private ListBarang listBarang;
    private JPanel gridPanel;
    private JLabel lblCartInfo;

    public CustomerPanel(MainFrame frame, Customer cust, ListBarang lb) {
        this.mainFrame = frame; 
        this.customer = cust; 
        this.listBarang = lb;

        setLayout(new BorderLayout());

        // ============================
        // HEADER AREA (Brand + Greeting + Cart + Logout)
        // ============================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(10,20,10,20));

        JPanel leftHeader = new JPanel(new GridLayout(2, 1));
        leftHeader.setBackground(Color.WHITE);

        JLabel brand = new JLabel("Glowify Store");
        brand.setFont(StyleUtils.TITLE_FONT);
        brand.setForeground(StyleUtils.ACCENT);

        JLabel greeting = new JLabel("Welcome to Glowify, " + cust.getUsername() + ". Happy Shopping!!");
        greeting.setFont(new Font("SansSerif", Font.ITALIC, 12));
        greeting.setForeground(Color.GRAY);

        leftHeader.add(brand);
        leftHeader.add(greeting);

        // ============================
        // CART + HISTORY + LOGOUT
        // ============================
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setBackground(Color.WHITE);

        lblCartInfo = new JLabel("Cart: 0");        // info jumlah item di keranjang
        lblCartInfo.setFont(StyleUtils.HEADER_FONT);

        JButton btnCart = new JButton("🛒");
        StyleUtils.styleButton(btnCart);
        btnCart.addActionListener(e -> showCartDialog());

        JButton btnHistory = new JButton("Status Pesanan");
        StyleUtils.styleButtonSecondary(btnHistory);
        btnHistory.addActionListener(e -> showHistoryDialog());

        JButton btnLogout = new JButton("Keluar");
        btnLogout.setBackground(Color.GRAY);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> mainFrame.showLogin());  // kembali ke login

        right.add(lblCartInfo);
        right.add(Box.createHorizontalStrut(10));
        right.add(btnCart);
        right.add(btnHistory);
        right.add(Box.createHorizontalStrut(10));
        right.add(btnLogout);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ============================
        // GRID CATALOG PRODUK
        // ============================
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(StyleUtils.SECONDARY);
        gridPanel.setBorder(new EmptyBorder(20,20,20,20));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        refreshCatalog();
    }

    // ===========================================
    // Muat ulang katalog dari file barang.txt
    // ===========================================
    private void refreshCatalog() {
        gridPanel.removeAll();
        listBarang.loadData();   // refresh data stok nyata
        for(Barang b : listBarang.getBarangList()) 
            gridPanel.add(createProductCard(b));

        updateCartCount();
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // ===========================================
    // Card tiap produk (gambar + nama + harga + tombol beli)
    // ===========================================
    private JPanel createProductCard(Barang b) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        // -------------------------------
        // Gambar Produk
        // -------------------------------
        JLabel imgLabel = new JLabel();
        ImageIcon icon = ImageUtils.getProductImage(b.getId(), 150, 150);

        if(icon != null) {
            imgLabel.setIcon(icon);
        } else {
            // Jika belum ada gambar → tampil placeholder
            imgLabel.setPreferredSize(new Dimension(150, 150));
            imgLabel.setOpaque(true);
            imgLabel.setBackground(Color.LIGHT_GRAY);
            imgLabel.setText("Klik Detail");
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        // Klik gambar → muncul popup detail
        imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        final ImageIcon finalIcon = icon;

        imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // DETAIL POPUP
                JTextArea textArea = new JTextArea(b.getDeskripsi());
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setEditable(false);

                JScrollPane scrollText = new JScrollPane(textArea);
                scrollText.setPreferredSize(new Dimension(350, 150));

                ImageIcon bigIcon = ImageUtils.getProductImage(b.getId(), 250, 250);

                JOptionPane.showMessageDialog(
                    card, scrollText,
                    "Detail: " + b.getNama(),
                    JOptionPane.INFORMATION_MESSAGE,
                    (bigIcon!=null?bigIcon:finalIcon)
                );
            }
        });

        JLabel name = new JLabel(b.getNama());
        name.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel price = new JLabel("Rp " + String.format("%,.0f", b.getHarga()));
        price.setForeground(StyleUtils.ACCENT);
        price.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel stock = new JLabel("Stok: " + b.getStok());

        // -------------------------------
        // BUTTON BELI & TAMBAH KE KERANJANG
        // -------------------------------
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        actionPanel.setBackground(Color.WHITE);

        JButton btnBuyNow = new JButton("Beli");
        btnBuyNow.setBackground(StyleUtils.ACCENT);
        btnBuyNow.setForeground(Color.WHITE);

        JButton btnAddToCart = new JButton("🛒");
        btnAddToCart.setBackground(StyleUtils.SECONDARY);

        // BELI LANGSUNG
        btnBuyNow.addActionListener(e -> processDirectBuy(b));

        // TAMBAH KE KERANJANG + VALIDASI INPUT ANGKA
        btnAddToCart.addActionListener(e -> {
            if(b.getStok() <= 0) {
                JOptionPane.showMessageDialog(this, "Stok Habis!", "Info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String input = JOptionPane.showInputDialog(this, "Jumlah (Keranjang):", "1");
            if(input == null) return;

            try {
                int qty = Integer.parseInt(input);

                // Validasi jumlah
                if(qty > 0 && qty <= b.getStok()) {
                    customer.getKeranjang().addBarang(b, qty);
                    updateCartCount();
                    showCartDialog();  // langsung buka dialog keranjang
                } else {
                    JOptionPane.showMessageDialog(this, "Jumlah tidak valid (Min 1, Max "+b.getStok()+")");
                }

            } catch(NumberFormatException ex) {
                // Input huruf → error
                JOptionPane.showMessageDialog(this, "Input harus angka!", "Error Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(btnBuyNow);
        actionPanel.add(btnAddToCart);

        // Susun komponen card
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        price.setAlignmentX(Component.CENTER_ALIGNMENT);
        stock.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(imgLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(name);
        card.add(Box.createVerticalStrut(5));
        card.add(price);
        card.add(stock);
        card.add(Box.createVerticalStrut(10));
        card.add(actionPanel);

        return card;
    }

    // ===========================================
    // Update jumlah item di keranjang (UI)
    // ===========================================
    private void updateCartCount() {
        int count = customer.getKeranjang().getItems() != null
                    ? customer.getKeranjang().getItems().size()
                    : 0;
        lblCartInfo.setText("🛒" + count);
    }

    // ===========================================
    // PEMBELIAN LANGSUNG (TANPA KERANJANG)
    // ===========================================
    private void processDirectBuy(Barang b) {
        if(b.getStok() <= 0) {
            JOptionPane.showMessageDialog(this, "Stok Habis!");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Beli Langsung (Jumlah):", "1");
        if(input == null) return;

        try {
            int qty = Integer.parseInt(input);

            // Validasi jumlah
            if(qty <= 0 || qty > b.getStok()) {
                JOptionPane.showMessageDialog(this, "Jumlah tidak valid!");
                return;
            }

            // PILIH METODE PEMBAYARAN
            String[] opts = {"QRIS", "Bank Transfer", "COD"};
            int c = JOptionPane.showOptionDialog(this, "Metode Pembayaran", "Checkout",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opts, opts[0]);

            if(c == -1) return;

            Pembayaran bayar = (c==0) ? new QRIS() : (c==1) ? new Bank() : new COD();

            // Muat ulang stok terbaru sebelum transaksi (mencegah race condition)
            listBarang.loadData();
            Barang realItem = listBarang.getBarang(b.getId());

            // Cek stok terbaru
            if(realItem != null && realItem.getStok() >= qty) {

                listBarang.reduceStock(realItem.getId(), qty);

                // Buat list barang sebanyak qty
                List<Barang> trxItems = new ArrayList<>();
                for(int i=0; i<qty; i++) trxItems.add(realItem);

                // Simpan transaksi
                Transaksi trx = new Transaksi(customer, trxItems);
                trx.setMetodePembayaran(bayar.getClass().getSimpleName());

                mainFrame.addTransaction(trx);

                // Simpan invoice
                Invoice inv = new Invoice(trx, bayar);
                customer.getInvoiceSelesai().add(inv);

                JOptionPane.showMessageDialog(this, "Pembelian Berhasil!\nStatus: Sedang Diproses");
                refreshCatalog();

            } else {
                JOptionPane.showMessageDialog(this, "Stok berubah/habis!", "Gagal", JOptionPane.ERROR_MESSAGE);
                refreshCatalog();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Input harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===========================================
    // DIALOG KERANJANG BELANJA
    // ===========================================
    private void showCartDialog() {
        JDialog d = new JDialog(mainFrame, "Keranjang Belanja", true);
        d.setSize(750, 500);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        // Ambil isi keranjang
        List<CartItem> items = customer.getKeranjang().getItems();

        String[] cols = {"Pilih", "ID", "Produk", "Harga", "Qty", "Subtotal"};

        // Tabel keranjang
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int columnIndex) { 
                return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
            }
            @Override public boolean isCellEditable(int row, int column) { 
                return column == 0;   // hanya kolom checkbox yang bisa diedit
            }
        };

        // Masukkan data keranjang ke tabel
        for(CartItem i : items) {
            model.addRow(new Object[]{
                Boolean.TRUE,
                i.getBarang().getId(),
                i.getBarang().getNama(),
                i.getBarang().getHarga(),
                i.getQty(),
                i.getBarang().getHarga() * i.getQty()
            });
        }

        JTable table = new JTable(model);
        d.add(new JScrollPane(table), BorderLayout.CENTER);

        // ============================
        // AREA TOTAL & CHECKOUT
        // ============================
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel lblTotal = new JLabel("Total Dipilih: Rp 0");

        // Hitung total otomatis ketika checkbox berubah
        Runnable hitungTotal = () -> {
            double tot = 0;
            for(int i=0; i<table.getRowCount(); i++)
                if((Boolean) table.getValueAt(i, 0))
                    tot += (Double) table.getValueAt(i, 5);

            lblTotal.setText("Total Dipilih: Rp " + String.format("%,.0f", tot));
        };

        model.addTableModelListener(e -> hitungTotal.run());
        hitungTotal.run();

        // Tombol bayar
        JButton btnCheckout = new JButton("Bayar");
        StyleUtils.styleButton(btnCheckout);

        btnCheckout.addActionListener(e -> {
            // Ambil item yang dicentang
            List<CartItem> selectedItems = new ArrayList<>();
            for(int i=0; i<table.getRowCount(); i++)
                if((Boolean) table.getValueAt(i, 0)) {
                    String id = (String) table.getValueAt(i, 1);
                    for(CartItem item : items)
                        if(item.getBarang().getId().equals(id))
                            selectedItems.add(item);
                }

            if(selectedItems.isEmpty()) {
                JOptionPane.showMessageDialog(d, "Pilih barang dulu!");
                return;
            }

            String[] opts = {"QRIS", "Bank", "COD"};
            int c = JOptionPane.showOptionDialog(d, "Pembayaran", "Checkout",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, opts, opts[0]);

            if(c != -1) {
                Pembayaran bayar = (c==0)?new QRIS():(c==1)?new Bank():new COD();

                listBarang.loadData();   // refresh stok terbaru
                
                boolean ok = true;
                List<Barang> trxItems = new ArrayList<>();

                // Cek stok untuk tiap item
                for(CartItem ci : selectedItems) {
                    Barang real = listBarang.getBarang(ci.getBarang().getId());

                    if(real == null || real.getStok() < ci.getQty()) {
                        ok = false; 
                    } else {
                        for(int k=0; k<ci.getQty(); k++) trxItems.add(real);
                    }
                }

                if(!ok) {
                    JOptionPane.showMessageDialog(d, "Stok tidak cukup!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Kurangi stok & hapus dari keranjang
                for(CartItem ci : selectedItems) {
                    listBarang.reduceStock(ci.getBarang().getId(), ci.getQty());
                    customer.getKeranjang().removeById(ci.getBarang().getId());
                }

                // Simpan transaksi
                Transaksi trx = new Transaksi(customer, trxItems);
                trx.setMetodePembayaran(bayar.getClass().getSimpleName());
                mainFrame.addTransaction(trx);

                // Simpan invoice
                Invoice inv = new Invoice(trx, bayar);
                customer.getInvoiceSelesai().add(inv);

                JOptionPane.showMessageDialog(d, "Sukses!\nInvoice disimpan.");

                d.dispose();
                refreshCatalog();
            }
        });

        bottom.add(lblTotal);
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(btnCheckout);
        d.add(bottom, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // ===========================================
    // RIWAYAT TRANSAKSI
    // ===========================================
    private void showHistoryDialog() {
        JDialog d = new JDialog(mainFrame, "Status Pesanan & Riwayat", true);
        d.setSize(750, 500);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        String[] cols = {"Tgl", "ID Transaksi", "Total", "Metode Bayar", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        // Masukkan data invoice ke tabel
        for(Invoice inv : customer.getInvoiceSelesai()) {
            Transaksi t = inv.getTransaksi();
            model.addRow(new Object[]{
                df.format(new Date(t.getTimestamp())),
                t.getId(),
                "Rp " + String.format("%,.0f", t.getTotal()),
                t.getMetodePembayaran(),
                t.getStatus()
            });
        }

        d.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        d.setVisible(true);
    }
}

class AdminPanel extends JPanel {
    private MainFrame mainFrame;
    private ListBarang listBarang;
    private JTabbedPane tabbedPane;

    public AdminPanel(MainFrame frame, Admin admin, ListBarang lb) {
        this.mainFrame = frame;
        this.listBarang = lb;

        setLayout(new BorderLayout());

        // === HEADER PANEL ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(StyleUtils.ACCENT);
        header.setBorder(new EmptyBorder(10,20,10,20));

        JLabel title = new JLabel("ADMIN DASHBOARD");
        title.setForeground(Color.WHITE);
        title.setFont(StyleUtils.TITLE_FONT);

        JButton btnLogout = new JButton("Keluar");
        // Logout mengembalikan ke panel login
        btnLogout.addActionListener(e -> mainFrame.showLogin());

        header.add(title, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Tab utama Admin (Barang & Pesanan)
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Kelola Barang", createInventoryPanel());
        tabbedPane.addTab("Kelola Pesanan", createOrderPanel());
        add(tabbedPane, BorderLayout.CENTER);
    }

    // =======================
    // PANEL KELOLA INVENTORI
    // =======================
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID", "Nama", "Harga", "Stok", "Brand", "Desc"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        // **refresh** → memuat ulang data dari file.txt (menggunakan interface DataStorage)
        Runnable refresh = () -> {
            model.setRowCount(0);
            listBarang.loadData(); // load ulang file barang.txt
            for (Barang b : listBarang.getBarangList()) {
                model.addRow(new Object[]{
                        b.getId(), b.getNama(), b.getHarga(),
                        b.getStok(), b.getBrand(), b.getDeskripsi()
                });
            }
        };
        refresh.run(); // initial load

        // Input field
        JTextField txtNama = new JTextField();
        JTextField txtHarga = new JTextField();
        JTextField txtStok = new JTextField();
        JTextField txtBrand = new JTextField();
        JTextField txtDesc = new JTextField();
        JLabel lblId = new JLabel("-");

        // Ketika user klik baris tabel → data ditampilkan ke form untuk di-edit
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int r = table.getSelectedRow();
                lblId.setText(model.getValueAt(r, 0).toString());
                txtNama.setText(model.getValueAt(r, 1).toString());
                txtHarga.setText(model.getValueAt(r, 2).toString());
                txtStok.setText(model.getValueAt(r, 3).toString());
                txtBrand.setText(model.getValueAt(r, 4).toString());
                txtDesc.setText(model.getValueAt(r, 5).toString());
            }
        });

        JPanel form = new JPanel(new GridLayout(7, 2, 5, 5));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("ID:")); form.add(lblId);
        form.add(new JLabel("Nama:")); form.add(txtNama);
        form.add(new JLabel("Harga:")); form.add(txtHarga);
        form.add(new JLabel("Stok:")); form.add(txtStok);
        form.add(new JLabel("Brand:")); form.add(txtBrand);
        form.add(new JLabel("Desc:")); form.add(txtDesc);
        form.add(new JLabel("Foto:"));

        JPanel btns = new JPanel();
        JButton btnAdd = new JButton("Tambah");
        JButton btnUpd = new JButton("Update");
        JButton btnDel = new JButton("Hapus");
        JButton btnClr = new JButton("Clear");
        JButton btnUpload = new JButton("Upload Foto");
        StyleUtils.styleButtonSecondary(btnUpload);

        // ===========================
        //   BUTTON TAMBAH BARANG
        // ===========================
        // Validasi numeric + error handling agar program tidak crash
        btnAdd.addActionListener(e -> {
            try {
                String id = listBarang.generateId(); // ID otomatis increment
                listBarang.addBarang(new Barang(
                        id,
                        txtNama.getText(),
                        Double.parseDouble(txtHarga.getText()),
                        Integer.parseInt(txtStok.getText()),
                        txtDesc.getText(),
                        txtBrand.getText()
                ));
                refresh.run();
                JOptionPane.showMessageDialog(panel, "Sukses menambah barang!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Harga dan stok harus berupa angka!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Terjadi Error: " + ex.getMessage());
            }
        });

        // ===========================
        //   BUTTON UPDATE BARANG
        // ===========================
        btnUpd.addActionListener(e -> {
            if (lblId.getText().equals("-")) {
                JOptionPane.showMessageDialog(panel, "Pilih barang terlebih dahulu!");
                return;
            }
            listBarang.editBarangFull(
                    lblId.getText(),
                    txtNama.getText(),
                    Double.parseDouble(txtHarga.getText()),
                    Integer.parseInt(txtStok.getText()),
                    txtDesc.getText(),
                    txtBrand.getText()
            );
            refresh.run();
            JOptionPane.showMessageDialog(panel, "Barang berhasil diupdate!");
        });

        // ===========================
        //   BUTTON HAPUS BARANG
        // ===========================
        btnDel.addActionListener(e -> {
            if (lblId.getText().equals("-")) {
                JOptionPane.showMessageDialog(panel, "Pilih barang terlebih dahulu!");
                return;
            }
            listBarang.removeBarang(lblId.getText());
            refresh.run();
            lblId.setText("-");
        });

        // ===========================
        //   UPLOAD FOTO PRODUK
        // ===========================
        // File disimpan menjadi img/<id>.jpg
        btnUpload.addActionListener(e -> {
            if (lblId.getText().equals("-")) {
                JOptionPane.showMessageDialog(panel, "Pilih barang terlebih dahulu!");
                return;
            }
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png"));
            if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.copy(
                            fc.getSelectedFile().toPath(),
                            new File("img/" + lblId.getText() + ".jpg").toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    JOptionPane.showMessageDialog(panel, "Foto berhasil diupload!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Gagal mengupload gambar!");
                }
            }
        });

        // Bersihkan form
        btnClr.addActionListener(e -> {
            lblId.setText("-");
            txtNama.setText("");
            txtHarga.setText("");
            txtStok.setText("");
            txtBrand.setText("");
            txtDesc.setText("");
        });

        form.add(btnUpload);
        btns.add(btnAdd); btns.add(btnUpd); btns.add(btnDel); btns.add(btnClr);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout());
        bot.add(form, BorderLayout.CENTER);
        bot.add(btns, BorderLayout.SOUTH);

        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    // ===========================
    // PANEL KELOLA PESANAN
    // ===========================
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] cols = {"ID Trans", "Customer", "Tgl", "Total", "Bayar", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        Runnable loadData = () -> {
            model.setRowCount(0);
            // Ambil semua transaksi dari MainFrame
            for (Transaksi t : mainFrame.getAllTransactions()) {
                model.addRow(new Object[]{
                        t.getId(),
                        t.getCustomer().getUsername(),
                        df.format(new Date(t.getTimestamp())),
                        "Rp " + String.format("%,.0f", t.getTotal()),
                        t.getMetodePembayaran(),
                        t.getStatus()
                });
            }
        };

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadData.run());

        JPanel actionPanel = new JPanel();
        JButton btn1 = new JButton("Proses");
        JButton btn2 = new JButton("Kirim");
        JButton btn3 = new JButton("Selesai");

        // Listener generik untuk ubah status pesanan
        java.awt.event.ActionListener l = e -> {
            int r = table.getSelectedRow();
            if (r == -1) return;

            String id = (String) model.getValueAt(r, 0);

            // Update status transaksi
            for (Transaksi t : mainFrame.getAllTransactions()) {
                if (t.getId().equals(id)) {
                    t.setStatus(e.getActionCommand());
                    break;
                }
            }
            loadData.run();
        };

        btn1.setActionCommand("Sedang Diproses");
        btn2.setActionCommand("Dikirim");
        btn3.setActionCommand("Selesai");

        btn1.addActionListener(l);
        btn2.addActionListener(l);
        btn3.addActionListener(l);

        actionPanel.add(btn1); actionPanel.add(btn2); actionPanel.add(btn3);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Order: "));
        top.add(btnRefresh);

        panel.add(top, BorderLayout.NORTH);
        panel.add(actionPanel, BorderLayout.SOUTH);

        loadData.run();

        // Reload otomatis ketika tab "Kelola Pesanan" dibuka
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadData.run();
            }
        });

        return panel;
    }
}
