package coursework;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
/**
 * Simple Swing GUI to exercise the coursework features:
 * - Add/Delete product
 * - Display all products
 * - Add/Remove stock
 * - Show last 4 activities (sorted by quantity)
 *
 * Notes:
 * - Auto-loads saved data on startup, and auto-saves after each successful change.
 * - Keeps the UI straightforward with CardLayout for different panels.
 */

public class DashboardGUI extends JFrame {
    // Manager holds all product logic, search/sort, and persistence
    private ProductManager manager = new ProductManager();

    // Left navigation
    private JButton navAdd, navDelete, navDisplay, navAddStock, navRemoveStock, navActivities;

    // Card layout center
    private JPanel contentPanel;
    private CardLayout cards;

    // Add product fields
    private JTextField txtID_add, txtName_add, txtQty_add;

    // Delete product fields
    private JTextField txtID_delete;
    private JCheckBox chkDeleteConfirm;

    // Stock fields (shared)
    private JTextField txtStockID, txtStockQty;

    // Activity lookup
    private JTextField txtActLookupID;

    // Table and model
    private JTable table;
    private DefaultTableModel model;

    // Search and sort
    private JTextField txtSearch;
    private JButton btnSearch, btnSortName, btnSortQty;

    // Clock
    private JLabel lblClock;

    public DashboardGUI() {
        setTitle("Supermarket Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8,8));

        // TOP bar with clock , sort and search
        JPanel topBar = new JPanel(new BorderLayout(10,10));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(30);
        btnSearch = new JButton("Search");
        btnSortName = new JButton("Sort: Name");
        btnSortQty = new JButton("Sort: Quantity");
        searchPanel.add(new JLabel("Search (ID or Name):"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnSortName);
        searchPanel.add(btnSortQty);

        lblClock = new JLabel();
        lblClock.setHorizontalAlignment(SwingConstants.RIGHT);
        lblClock.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateClock();
        new Timer(1000, e -> updateClock()).start();

        topBar.add(searchPanel, BorderLayout.CENTER);
        topBar.add(lblClock, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // LEFT navigation 
        JPanel left = new JPanel(new GridLayout(8, 1, 8, 8));
        left.setPreferredSize(new Dimension(200, 0));
        navAdd = new JButton("Add Product");
        navDelete = new JButton("Delete Product");
        navDisplay = new JButton("Display Products");
        navAddStock = new JButton("Add Stock");
        navRemoveStock = new JButton("Remove Stock");
        navActivities = new JButton("Show Activities");
        left.add(navAdd); left.add(navDelete); left.add(navDisplay);
        left.add(navAddStock); left.add(navRemoveStock); left.add(navActivities);
        add(left, BorderLayout.WEST);

        // CENTER cardlayout 
        cards = new CardLayout();
        contentPanel = new JPanel(cards);
        contentPanel.add(buildAddPanel(), "add");
        contentPanel.add(buildDeletePanel(), "delete");
        contentPanel.add(buildAddStockPanel(), "addStock");
        contentPanel.add(buildRemoveStockPanel(), "removeStock");
        contentPanel.add(buildActivityPanel(), "activities");
        contentPanel.add(buildTablePanel(), "display");
        add(contentPanel, BorderLayout.CENTER);

        // NAVIGATION listeners
        navAdd.addActionListener(e -> showCard("add"));
        navDelete.addActionListener(e -> showCard("delete"));
        navAddStock.addActionListener(e -> showCard("addStock"));
        navRemoveStock.addActionListener(e -> showCard("removeStock"));
        navActivities.addActionListener(e -> showCard("activities"));
        navDisplay.addActionListener(e -> {
            refreshTable();
            showCard("display");
        });

        // search & sort listeners
        btnSearch.addActionListener(e -> refreshTableWithSearch());
        btnSortName.addActionListener(e -> {
            manager.sortByNameAsc();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Sorted by name (A → Z).");
        });
        btnSortQty.addActionListener(e -> {
            manager.sortByQuantityAsc();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Sorted by quantity (low → high).");
        });

        // auto load previously saved data (if have), then refresh the table
        manager.loadFromFile("data.dat");
        refreshTable();

        setVisible(true);
    }

    private void updateClock() {
        lblClock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    private void showCard(String name) {
        cards.show(contentPanel, name);
    }

    // ---------------- panels ----------------

    private JPanel buildAddPanel() {
        JPanel p = new JPanel(new GridLayout(5,2,10,10));
        p.setBorder(BorderFactory.createTitledBorder("Add Product"));
       // textfield.setPreferredSize(new Dimension(20, 20));

        txtID_add = new JTextField();
        txtName_add = new JTextField();
        txtQty_add = new JTextField();
        JButton btnAdd = new JButton("Add Product");

        p.add(new JLabel("Product ID:")); p.add(txtID_add);
        p.add(new JLabel("Name:")); p.add(txtName_add);
        p.add(new JLabel("Initial Quantity:")); p.add(txtQty_add);
        p.add(new JLabel("")); p.add(btnAdd);

        
        btnAdd.addActionListener(e -> {
            String id = txtID_add.getText().trim();
            String name = txtName_add.getText().trim();
            String qtys = txtQty_add.getText().trim();

            if (id.isEmpty() || name.isEmpty() || qtys.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int q = Integer.parseInt(qtys);
                if (q < 0) {
                    JOptionPane.showMessageDialog(this, "Initial quantity cannot be negative.",
                            "Invalid input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Product pNew = new Product(id, name, q);
                if (!manager.addProduct(pNew)) {
                    JOptionPane.showMessageDialog(this, "Product ID already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                manager.saveToFile("data.dat");  // <--- AUTO-SAVE
                JOptionPane.showMessageDialog(this, "Product added successfully.");
                refreshTable();
                txtID_add.setText(""); txtName_add.setText(""); txtQty_add.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Invalid input", JOptionPane.ERROR_MESSAGE);
            }
        });


        return p;
    }

    private JPanel buildDeletePanel() {
        JPanel p = new JPanel(new GridLayout(4,2,10,10));
        p.setBorder(BorderFactory.createTitledBorder("Delete Product"));

        txtID_delete = new JTextField();
        chkDeleteConfirm = new JCheckBox("Confirm Delete");
        JButton btnDel = new JButton("Delete");

        p.add(new JLabel("Product ID:")); p.add(txtID_delete);
        p.add(chkDeleteConfirm); p.add(new JLabel());
        p.add(btnDel); p.add(new JLabel());

        btnDel.addActionListener(e -> {
            if (!chkDeleteConfirm.isSelected()) {
                JOptionPane.showMessageDialog(this, "Tick 'Confirm Delete' checkbox first.", "Confirm required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = txtID_delete.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Product ID.", "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!manager.deleteProduct(id)) {
                JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                 manager.saveToFile("data.dat");  // <--- AUTO-SAVE
                JOptionPane.showMessageDialog(this, "Product deleted.");
                refreshTable();
                txtID_delete.setText(""); chkDeleteConfirm.setSelected(false);
            }
        });

        return p;
    }

    private JPanel buildAddStockPanel() {
        JPanel p = new JPanel(new GridLayout(4,2,10,10));
        p.setBorder(BorderFactory.createTitledBorder("Add To Stock"));

        txtStockID = new JTextField();
        txtStockQty = new JTextField();
        JButton btn = new JButton("Add");

        p.add(new JLabel("Product ID:")); p.add(txtStockID);
        p.add(new JLabel("Quantity to add:")); p.add(txtStockQty);
        p.add(btn); p.add(new JLabel());

        btn.addActionListener(e -> {
            String id = txtStockID.getText().trim();
            String sqty = txtStockQty.getText().trim();
            if (id.isEmpty() || sqty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill both fields.", "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int q = Integer.parseInt(sqty);
                if (!manager.addToStock(id, q)) {
                    JOptionPane.showMessageDialog(this, "Failed to add stock. Check ID or quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    manager.saveToFile("data.dat");  // AUTO-SAVE
                    JOptionPane.showMessageDialog(this, "Stock added.");
                    refreshTable();
                    txtStockID.setText(""); txtStockQty.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Invalid input", JOptionPane.ERROR_MESSAGE);
            }
        });

        return p;
    }

    private JPanel buildRemoveStockPanel() {
        JPanel p = new JPanel(new GridLayout(4,2,10,10));
        p.setBorder(BorderFactory.createTitledBorder("Remove From Stock"));

        JTextField txtRemID = new JTextField();
        JTextField txtRemQty = new JTextField();
        JButton btn = new JButton("Remove");

        p.add(new JLabel("Product ID:")); p.add(txtRemID);
        p.add(new JLabel("Quantity to remove:")); p.add(txtRemQty);
        p.add(btn); p.add(new JLabel());

        btn.addActionListener(e -> {
            String id = txtRemID.getText().trim();
            String sqty = txtRemQty.getText().trim();
            if (id.isEmpty() || sqty.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill both fields.", "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                int q = Integer.parseInt(sqty);
                if (!manager.removeFromStock(id, q)) {
                    JOptionPane.showMessageDialog(this, "Failed to remove stock. Check ID, quantity or available stock.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    manager.saveToFile("data.dat");  // AUTO-SAVE
                    JOptionPane.showMessageDialog(this, "Stock removed.");
                    refreshTable();
                    txtRemID.setText(""); txtRemQty.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Invalid input", JOptionPane.ERROR_MESSAGE);
            }
        });

        return p;
    }

    private JPanel buildActivityPanel() {
        JPanel p = new JPanel(new GridLayout(3,2,10,10));
        p.setBorder(BorderFactory.createTitledBorder("Show Last 4 Activities (Sorted by Quantity)"));

        txtActLookupID = new JTextField();
        JButton btn = new JButton("Show Activities");

        p.add(new JLabel("Product ID:")); p.add(txtActLookupID);
        p.add(btn); p.add(new JLabel());

        btn.addActionListener(e -> {
            String id = txtActLookupID.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Product ID.", "Input required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Activity[] arr = manager.getSortedActivities(id);
            if (arr == null) {
                JOptionPane.showMessageDialog(this, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (Activity a : arr) sb.append(a.toString()).append("\n");
            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Last 4 Activities", JOptionPane.INFORMATION_MESSAGE);
        });

        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(10,10));
        p.setBorder(BorderFactory.createTitledBorder("Products"));

        String[] cols = {"ID", "Name", "Quantity", "Last Updated", "Low Stock"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);

        // row renderer for low stock highlight
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                try {
                    int qty = Integer.parseInt(t.getValueAt(row, 2).toString());
                    if (!isSelected && qty < 5) {
                        c.setBackground(new Color(255, 230, 230)); // light red
                    } else {
                        c.setBackground(isSelected ? t.getSelectionBackground() : t.getBackground());
                    }
                } catch (Exception ex) {
                    c.setBackground(isSelected ? t.getSelectionBackground() : t.getBackground());
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    // refresh table with all products
    private void refreshTable() {
   
        model.setRowCount(0);
        for (Product p : manager.getAllProducts()) {
            model.addRow(new Object[]{
                p.getProductID(),
                p.getProductName(),
                p.getProductQuantity(),
                p.getEntryDate(),                         // <-- fill "Last Updated"
                p.getProductQuantity() < 5 ? "⚠ LOW" : ""
            });
        }

    }

    // refresh table with search results (id or name)
    private void refreshTableWithSearch() {
        String q = txtSearch.getText();
        ArrayList<Product> list = manager.searchByIdOrName(q);
        model.setRowCount(0);
        for (Product p : list) {
            model.addRow(new Object[]{p.getProductID(), p.getProductName(), p.getProductQuantity(),  p.getProductQuantity() < 5 ? "⚠ LOW" : ""});
        }
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching products found.", "Search", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // main
    public static void main(String[] args) {
        // Ensure GUI runs on EDT
        SwingUtilities.invokeLater(() -> new DashboardGUI());
    }
}
