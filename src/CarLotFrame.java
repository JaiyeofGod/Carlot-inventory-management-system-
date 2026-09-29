import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

/**
 * Main window for CarLot inventory management.
 */
public class CarLotFrame extends JFrame {

    private static final Color BG = new Color(245, 246, 248);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color ACCENT = new Color(20, 90, 150);
    private static final Color ACCENT_DARK = new Color(14, 68, 115);
    private static final Color SUCCESS = new Color(22, 122, 74);
    private static final Color MUTED = new Color(100, 110, 120);
    private static final Color SOLD_BG = new Color(248, 248, 250);
    private static final Color BORDER = new Color(220, 224, 230);

    private final CarLot carLot = new CarLot();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
    private final InventoryTableModel tableModel = new InventoryTableModel();
    private final TableRowSorter<InventoryTableModel> sorter = new TableRowSorter<>(tableModel);

    private JTable table;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<String> sortMode;
    private JLabel avgMpgLabel;
    private JLabel bestMpgLabel;
    private JLabel highestMileageLabel;
    private JLabel totalProfitLabel;
    private JLabel countsLabel;
    private JLabel detailLabel;
    private JButton sellButton;
    private boolean dirty;

    public CarLotFrame() {
        super("CarLot");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 680));
        setPreferredSize(new Dimension(1200, 740));
        setLocationRelativeTo(null);

        buildMenuBar();
        setContentPane(buildRoot());
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptQuit();
            }
        });

        boolean loaded = carLot.loadFromDiskIfPresent();
        refreshAll();
        if (loaded) {
            setStatusMessage("Loaded inventory from " + CarLot.CARLOT_INVENTORY_LOCATION);
        } else {
            setStatusMessage("No inventory file found — starting empty. Add a car to begin.");
        }
        dirty = false;
    }

    private void buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Save");
        save.setAccelerator(KeyStroke.getKeyStroke("meta S"));
        save.addActionListener(e -> saveInventory());
        JMenuItem load = new JMenuItem("Load…");
        load.setAccelerator(KeyStroke.getKeyStroke("meta O"));
        load.addActionListener(e -> loadInventory());
        JMenuItem quit = new JMenuItem("Quit");
        quit.setAccelerator(KeyStroke.getKeyStroke("meta Q"));
        quit.addActionListener(e -> attemptQuit());
        file.add(save);
        file.add(load);
        file.addSeparator();
        file.add(quit);

        JMenu inventory = new JMenu("Inventory");
        JMenuItem add = new JMenuItem("Add Car…");
        add.setAccelerator(KeyStroke.getKeyStroke("meta N"));
        add.addActionListener(e -> showAddCarDialog());
        JMenuItem sell = new JMenuItem("Sell Selected…");
        sell.addActionListener(e -> showSellDialog());
        inventory.add(add);
        inventory.add(sell);

        bar.add(file);
        bar.add(inventory);
        setJMenuBar(bar);
    }

    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        root.add(buildToolbar(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setBorder(new EmptyBorder(16, 16, 16, 16));
        center.setOpaque(false);
        center.add(buildInsightsPanel(), BorderLayout.WEST);
        center.add(buildMainPanel(), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(PANEL_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel brand = new JLabel("CarLot");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 22f));
        brand.setForeground(ACCENT_DARK);

        JLabel subtitle = new JLabel("Inventory");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(13f));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(brand);
        left.add(subtitle);

        searchField = new JTextField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Search by ID");
        searchField.getDocument().addDocumentListener(simpleDocListener(this::applyFilters));

        statusFilter = new JComboBox<>(new String[]{"All", "Available", "Sold"});
        statusFilter.addActionListener(e -> applyFilters());

        sortMode = new JComboBox<>(new String[]{
                "Order of entry",
                "MPG (high → low)",
                "Mileage (high → low)",
                "Asking price (high → low)"
        });
        sortMode.addActionListener(e -> applySort());

        JButton addBtn = primaryButton("Add car");
        addBtn.addActionListener(e -> showAddCarDialog());

        JButton saveBtn = secondaryButton("Save");
        saveBtn.addActionListener(e -> saveInventory());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(labeled("Search", searchField));
        right.add(labeled("Status", statusFilter));
        right.add(labeled("Sort", sortMode));
        right.add(saveBtn);
        right.add(addBtn);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildInsightsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        panel.setPreferredSize(new Dimension(250, 0));

        JLabel title = new JLabel("Insights");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        countsLabel = mutedLabel("0 cars · 0 available");
        countsLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(countsLabel);
        panel.add(Box.createVerticalStrut(12));
        panel.add(separator());
        panel.add(Box.createVerticalStrut(12));

        avgMpgLabel = insightValue("—");
        bestMpgLabel = insightValue("—");
        highestMileageLabel = insightValue("—");
        totalProfitLabel = insightValue("—");

        panel.add(insightBlock("Average MPG", avgMpgLabel));
        panel.add(Box.createVerticalStrut(14));
        panel.add(insightBlock("Best MPG", bestMpgLabel));
        panel.add(Box.createVerticalStrut(14));
        panel.add(insightBlock("Highest mileage", highestMileageLabel));
        panel.add(Box.createVerticalStrut(14));
        panel.add(insightBlock("Total profit", totalProfitLabel));
        panel.add(Box.createVerticalStrut(16));
        panel.add(separator());
        panel.add(Box.createVerticalStrut(12));
        JLabel dataTitle = mutedLabel("Saved to");
        dataTitle.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(dataTitle);
        JLabel dataPath = new JLabel("<html><body style='width:200px'>"
                + CarLot.CARLOT_INVENTORY_LOCATION.replace(System.getProperty("user.home"), "~")
                + "</body></html>");
        dataPath.setFont(dataPath.getFont().deriveFont(11f));
        dataPath.setForeground(MUTED);
        dataPath.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(4));
        panel.add(dataPath);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);

        table = new JTable(tableModel);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getTableHeader().setDefaultRenderer(new InventoryHeaderRenderer());
        table.setDefaultRenderer(Object.class, new InventoryCellRenderer());
        configureColumnWidths(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetail();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(PANEL_BG);

        JPanel detail = new JPanel(new BorderLayout(12, 0));
        detail.setBackground(PANEL_BG);
        detail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(12, 14, 12, 14)));

        detailLabel = new JLabel("Select a car to see details");
        detailLabel.setForeground(MUTED);
        sellButton = primaryButton("Sell car…");
        sellButton.setEnabled(false);
        sellButton.addActionListener(e -> showSellDialog());

        detail.add(detailLabel, BorderLayout.CENTER);
        detail.add(sellButton, BorderLayout.EAST);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(detail, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshAll() {
        tableModel.setCars(getDisplayList());
        updateInsights();
        applyFilters();
        applySort();
        updateDetail();
    }

    private ArrayList<Car> getDisplayList() {
        String mode = (String) sortMode.getSelectedItem();
        if ("MPG (high → low)".equals(mode)) {
            return carLot.getCardsSortedByMPG();
        }
        if ("Mileage (high → low)".equals(mode)) {
            ArrayList<Car> list = carLot.getCarsInOrderOfEntry();
            list.sort((a, b) -> Integer.compare(b.getMileage(), a.getMileage()));
            return list;
        }
        if ("Asking price (high → low)".equals(mode)) {
            ArrayList<Car> list = carLot.getCarsInOrderOfEntry();
            list.sort((a, b) -> Double.compare(b.getSalesPrice(), a.getSalesPrice()));
            return list;
        }
        return carLot.getCarsInOrderOfEntry();
    }

    private void applySort() {
        tableModel.setCars(getDisplayList());
        applyFilters();
        updateDetail();
    }

    private void applyFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.US);
        String status = (String) statusFilter.getSelectedItem();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends InventoryTableModel, ? extends Integer> entry) {
                InventoryTableModel model = entry.getModel();
                Car car = model.getCarAt(entry.getIdentifier());
                if (car == null) {
                    return false;
                }
                if (!query.isEmpty() && !car.getId().toLowerCase(Locale.US).contains(query)) {
                    return false;
                }
                if ("Available".equals(status) && car.isSold()) {
                    return false;
                }
                if ("Sold".equals(status) && !car.isSold()) {
                    return false;
                }
                return true;
            }
        });
    }

    private void updateInsights() {
        int total = carLot.getInventory().size();
        int available = carLot.getAvailableCount();
        countsLabel.setText(total + " cars · " + available + " available");

        avgMpgLabel.setText(total == 0 ? "—" : String.format(Locale.US, "%.1f", carLot.getAverageMpg()));

        Car best = carLot.getCarWithBestMPG();
        bestMpgLabel.setText(best == null ? "—" : best.getId() + " · " + best.getMpg() + " mpg");

        Car highest = carLot.getCarWithHighestMileage();
        highestMileageLabel.setText(highest == null ? "—"
                : highest.getId() + " · " + NumberFormat.getIntegerInstance().format(highest.getMileage()) + " mi");

        totalProfitLabel.setText(currency.format(carLot.getTotalProfit()));
        totalProfitLabel.setForeground(carLot.getTotalProfit() >= 0 ? SUCCESS : new Color(170, 40, 40));
    }

    private void updateDetail() {
        Car car = getSelectedCar();
        if (car == null) {
            detailLabel.setText("Select a car to see details");
            detailLabel.setForeground(MUTED);
            sellButton.setEnabled(false);
            return;
        }
        String text = String.format(Locale.US,
                "%s  ·  %s mi  ·  %d mpg  ·  cost %s  ·  asking %s%s",
                car.getId(),
                NumberFormat.getIntegerInstance().format(car.getMileage()),
                car.getMpg(),
                currency.format(car.getCost()),
                currency.format(car.getSalesPrice()),
                car.isSold()
                        ? "  ·  SOLD for " + currency.format(car.getPriceSold())
                        + "  ·  profit " + currency.format(car.getProfit())
                        : "  ·  Available");
        detailLabel.setText(text);
        detailLabel.setForeground(Color.DARK_GRAY);
        sellButton.setEnabled(!car.isSold());
    }

    private Car getSelectedCar() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getCarAt(modelRow);
    }

    private void showAddCarDialog() {
        JDialog dialog = new JDialog(this, "Add car", true);
        dialog.setLayout(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        form.setBackground(PANEL_BG);

        JTextField idField = new JTextField(18);
        JTextField mileageField = new JTextField(18);
        JTextField mpgField = new JTextField(18);
        JTextField costField = new JTextField(18);
        JTextField priceField = new JTextField(18);
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(170, 40, 40));

        int row = 0;
        row = addFormRow(form, row, "ID (no spaces)", idField);
        row = addFormRow(form, row, "Mileage", mileageField);
        row = addFormRow(form, row, "MPG", mpgField);
        row = addFormRow(form, row, "Cost", costField);
        row = addFormRow(form, row, "Asking price", priceField);

        GridBagConstraints ec = new GridBagConstraints();
        ec.gridx = 0;
        ec.gridy = row;
        ec.gridwidth = 2;
        ec.anchor = GridBagConstraints.WEST;
        ec.insets = new Insets(8, 0, 0, 0);
        form.add(errorLabel, ec);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBorder(new EmptyBorder(8, 16, 16, 16));
        actions.setBackground(PANEL_BG);
        JButton cancel = secondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        JButton save = primaryButton("Add to inventory");
        save.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                if (id.isEmpty()) {
                    throw new IllegalArgumentException("ID is required.");
                }
                if (id.contains(" ")) {
                    throw new IllegalArgumentException("ID must not contain spaces.");
                }
                if (carLot.findCarByIdentifier(id) != null) {
                    throw new IllegalArgumentException("A car with this ID already exists.");
                }
                int mileage = parseNonNegativeInt(mileageField.getText(), "Mileage");
                int mpg = parsePositiveInt(mpgField.getText(), "MPG");
                double cost = parseNonNegativeDouble(costField.getText(), "Cost");
                double salesPrice = parseNonNegativeDouble(priceField.getText(), "Asking price");

                carLot.addCar(id, mileage, mpg, cost, salesPrice);
                markDirty();
                refreshAll();
                selectCarById(id);
                dialog.dispose();
                setStatusMessage("Added " + id + " to inventory.");
            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });
        actions.add(cancel);
        actions.add(save);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void showSellDialog() {
        Car car = getSelectedCar();
        if (car == null) {
            JOptionPane.showMessageDialog(this, "Select an available car first.", "Sell car",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (car.isSold()) {
            JOptionPane.showMessageDialog(this, "That car is already sold.", "Sell car",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Sell " + car.getId(), true);
        dialog.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(16, 16, 8, 16));
        body.setBackground(PANEL_BG);

        JLabel summary = new JLabel("<html><b>" + car.getId() + "</b><br>"
                + "Asking " + currency.format(car.getSalesPrice())
                + " · Cost " + currency.format(car.getCost()) + "</html>");
        summary.setAlignmentX(LEFT_ALIGNMENT);
        body.add(summary);
        body.add(Box.createVerticalStrut(12));

        JTextField priceField = new JTextField(currency.format(car.getSalesPrice()).replace("$", "").replace(",", "").trim(), 14);
        priceField.setMaximumSize(new Dimension(280, 28));
        priceField.setAlignmentX(LEFT_ALIGNMENT);
        JLabel priceLabel = new JLabel("Price sold");
        priceLabel.setAlignmentX(LEFT_ALIGNMENT);
        body.add(priceLabel);
        body.add(Box.createVerticalStrut(4));
        body.add(priceField);

        JLabel preview = new JLabel(" ");
        preview.setAlignmentX(LEFT_ALIGNMENT);
        preview.setForeground(MUTED);
        body.add(Box.createVerticalStrut(8));
        body.add(preview);

        Runnable updatePreview = () -> {
            try {
                double price = parseNonNegativeDouble(priceField.getText(), "Price sold");
                double profit = price - car.getCost();
                preview.setText("Profit preview: " + currency.format(profit));
                preview.setForeground(profit >= 0 ? SUCCESS : new Color(170, 40, 40));
            } catch (IllegalArgumentException ex) {
                preview.setText(ex.getMessage());
                preview.setForeground(new Color(170, 40, 40));
            }
        };
        priceField.getDocument().addDocumentListener(simpleDocListener(updatePreview));
        updatePreview.run();

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBorder(new EmptyBorder(8, 16, 16, 16));
        actions.setBackground(PANEL_BG);
        JButton cancel = secondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        JButton confirm = primaryButton("Confirm sale");
        confirm.addActionListener(e -> {
            try {
                double priceSold = parseNonNegativeDouble(priceField.getText(), "Price sold");
                int confirmResult = JOptionPane.showConfirmDialog(dialog,
                        "Mark " + car.getId() + " as sold for " + currency.format(priceSold) + "?\nThis cannot be undone.",
                        "Confirm sale",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirmResult != JOptionPane.OK_OPTION) {
                    return;
                }
                carLot.sellCar(car.getId(), priceSold);
                markDirty();
                refreshAll();
                selectCarById(car.getId());
                dialog.dispose();
                setStatusMessage("Sold " + car.getId() + " for " + currency.format(priceSold) + ".");
            } catch (IllegalArgumentException ex) {
                preview.setText(ex.getMessage());
                preview.setForeground(new Color(170, 40, 40));
            }
        });
        actions.add(cancel);
        actions.add(confirm);

        dialog.add(body, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }

    private void saveInventory() {
        try {
            carLot.saveToDisk();
            dirty = false;
            setTitle("CarLot");
            setStatusMessage("Saved inventory to " + CarLot.CARLOT_INVENTORY_LOCATION + ".");
            JOptionPane.showMessageDialog(this,
                    "Inventory saved to " + CarLot.CARLOT_INVENTORY_LOCATION + ".",
                    "Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save to " + CarLot.CARLOT_INVENTORY_LOCATION + ".\n" + ex.getMessage(),
                    "Save failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInventory() {
        if (dirty) {
            int result = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Load from disk anyway?",
                    "Load inventory",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
        }
        try {
            carLot.loadFromDisk();
            dirty = false;
            setTitle("CarLot");
            refreshAll();
            setStatusMessage("Loaded inventory from " + CarLot.CARLOT_INVENTORY_LOCATION + ".");
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not open " + CarLot.CARLOT_INVENTORY_LOCATION + ".\n" + ex.getMessage(),
                    "Load failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptQuit() {
        if (dirty) {
            Object[] options = {"Save and Quit", "Quit without Saving", "Cancel"};
            int result = JOptionPane.showOptionDialog(this,
                    "You have unsaved changes.",
                    "Quit CarLot",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (result == 0) {
                try {
                    carLot.saveToDisk();
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Could not save before quitting.\n" + ex.getMessage(),
                            "Save failed",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dispose();
                System.exit(0);
            } else if (result == 1) {
                dispose();
                System.exit(0);
            }
            return;
        }
        dispose();
        System.exit(0);
    }

    private void selectCarById(String id) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Car car = tableModel.getCarAt(i);
            if (car != null && car.getId().equals(id)) {
                int viewRow = table.convertRowIndexToView(i);
                if (viewRow >= 0) {
                    table.setRowSelectionInterval(viewRow, viewRow);
                    table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                }
                break;
            }
        }
    }

    private void setStatusMessage(String message) {
        setTitle(dirty ? "CarLot — unsaved changes" : "CarLot");
        getRootPane().putClientProperty("carlot.status", message);
    }

    private void markDirty() {
        dirty = true;
        setTitle("CarLot — unsaved changes");
    }

    private static int addFormRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints lc = new GridBagConstraints();
        lc.gridx = 0;
        lc.gridy = row;
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(0, 0, 10, 12);
        form.add(new JLabel(label), lc);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 1;
        fc.gridy = row;
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1;
        fc.insets = new Insets(0, 0, 10, 0);
        form.add(field, fc);
        return row + 1;
    }

    private static JPanel labeled(String text, JComponent field) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        panel.add(label);
        panel.add(field);
        return panel;
    }

    private static JPanel insightBlock(String title, JLabel value) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(LEFT_ALIGNMENT);
        JLabel t = mutedLabel(title);
        t.setAlignmentX(LEFT_ALIGNMENT);
        value.setAlignmentX(LEFT_ALIGNMENT);
        block.add(t);
        block.add(Box.createVerticalStrut(2));
        block.add(value);
        return block;
    }

    private static JLabel insightValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }

    private static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(label.getFont().deriveFont(12f));
        return label;
    }

    private static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(BORDER);
        return sep;
    }

    private static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        return button;
    }

    private static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        return button;
    }

    private static int parseNonNegativeInt(String raw, String field) {
        try {
            int value = Integer.parseInt(raw.trim().replace(",", ""));
            if (value < 0) {
                throw new IllegalArgumentException(field + " must be 0 or greater.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " must be a whole number.");
        }
    }

    private static int parsePositiveInt(String raw, String field) {
        int value = parseNonNegativeInt(raw, field);
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0.");
        }
        return value;
    }

    private static double parseNonNegativeDouble(String raw, String field) {
        try {
            String cleaned = raw.trim().replace("$", "").replace(",", "");
            double value = Double.parseDouble(cleaned);
            if (value < 0) {
                throw new IllegalArgumentException(field + " must be 0 or greater.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " must be a number.");
        }
    }

    private static DocumentListener simpleDocListener(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    private static final class InventoryTableModel extends AbstractTableModel {
        private final String[] columns = {
                "ID", "Mileage", "MPG", "Cost", "Asking", "Status", "Sold for", "Profit"
        };
        private ArrayList<Car> cars = new ArrayList<>();

        void setCars(ArrayList<Car> cars) {
            this.cars = cars;
            fireTableDataChanged();
        }

        Car getCarAt(int row) {
            if (row < 0 || row >= cars.size()) {
                return null;
            }
            return cars.get(row);
        }

        @Override
        public int getRowCount() {
            return cars.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Car car = cars.get(rowIndex);
            NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
            return switch (columnIndex) {
                case 0 -> car.getId();
                case 1 -> NumberFormat.getIntegerInstance().format(car.getMileage());
                case 2 -> car.getMpg();
                case 3 -> currency.format(car.getCost());
                case 4 -> currency.format(car.getSalesPrice());
                case 5 -> car.isSold() ? "Sold" : "Available";
                case 6 -> car.isSold() ? currency.format(car.getPriceSold()) : "—";
                case 7 -> car.isSold() ? currency.format(car.getProfit()) : "—";
                default -> "";
            };
        }
    }

    private static int columnAlignment(int column) {
        // ID (0) and Status (5) left; numeric money/mileage columns right
        return (column == 0 || column == 5) ? SwingConstants.LEFT : SwingConstants.RIGHT;
    }

    private static void configureColumnWidths(JTable table) {
        TableColumnModel columns = table.getColumnModel();
        int[] widths = {160, 90, 60, 100, 100, 90, 100, 100};
        for (int i = 0; i < widths.length && i < columns.getColumnCount(); i++) {
            TableColumn column = columns.getColumn(i);
            column.setPreferredWidth(widths[i]);
        }
    }

    private static final class InventoryHeaderRenderer extends DefaultTableCellRenderer {
        InventoryHeaderRenderer() {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                    new EmptyBorder(8, 10, 8, 10)));
            setFont(getFont().deriveFont(Font.BOLD));
            setBackground(new Color(236, 239, 243));
            setForeground(new Color(55, 65, 75));
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(columnAlignment(column));
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    private final class InventoryCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(columnAlignment(column));
            if (!isSelected) {
                int modelRow = table.convertRowIndexToModel(row);
                Car car = tableModel.getCarAt(modelRow);
                if (car != null && car.isSold()) {
                    c.setBackground(SOLD_BG);
                    c.setForeground(MUTED);
                } else {
                    c.setBackground(PANEL_BG);
                    c.setForeground(Color.DARK_GRAY);
                }
                if (column == 5 && car != null && !car.isSold()) {
                    c.setForeground(SUCCESS);
                }
            }
            setBorder(new EmptyBorder(0, 10, 0, 10));
            return c;
        }
    }
}
