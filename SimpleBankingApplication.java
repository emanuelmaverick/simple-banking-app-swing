import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class SimpleBankingApplication extends JFrame {

    private static final Color BG = new Color(0x0F172A);
    private static final Color PANEL = new Color(0x111827);
    private static final Color CARD = new Color(0x111C33);
    private static final Color BORDER = new Color(0x22304A);
    private static final Color TEXT = new Color(0xE5E7EB);
    private static final Color MUTED = new Color(0x9CA3AF);
    private static final Color ACCENT = new Color(0x38BDF8);
    private static final Color GOOD = new Color(0x22C55E);
    private static final Color WARN = new Color(0xF59E0B);
    private static final Color BAD = new Color(0xEF4444);

    static class BankAccount {
        private final String accNo;
        private final String name;
        private double balance;

        BankAccount(String accNo, String name, double initialBalance) {
            this.accNo = accNo;
            this.name = name;
            this.balance = initialBalance;
        }

        String getAccNo() { return accNo; }
        String getName() { return name; }
        double getBalance() { return balance; }

        void deposit(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Deposit must be greater than 0.");
            balance += amount;
        }

        void withdraw(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Withdraw must be greater than 0.");
            if (amount > balance) throw new IllegalArgumentException("Insufficient balance.");
            balance -= amount;
        }
    }

    private final Map<String, BankAccount> accounts = new HashMap<>();

    private final JComboBox<String> accountCombo = new JComboBox<>();
    private final JLabel statusLabel = new JLabel("Ready.");
    private final JLabel statusPill = new JLabel(" OK ");
    private final JLabel nameValue = new JLabel("—");
    private final JLabel accValue = new JLabel("—");
    private final JLabel balValue = new JLabel("₱0.00");

    private DefaultTableModel tableModel;
    private JTable table;

    public SimpleBankingApplication() {
        super("Simple Banking Application (Pro GUI)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        applySwingLook();
        buildUI();
        refreshAccountsUI();
    }

    private void applySwingLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("OptionPane.background", PANEL);
        UIManager.put("Panel.background", PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("OptionPane.foreground", TEXT);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        setContentPane(root);

        root.add(new GradientHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(14, 14));
        body.setBorder(new EmptyBorder(14, 14, 14, 14));
        body.setBackground(BG);
        root.add(body, BorderLayout.CENTER);

        JPanel sidebar = cardPanel();
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel sideTitle = label("ACTIONS", 12, MUTED, true);
        sideTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sideTitle);
        sidebar.add(Box.createVerticalStrut(12));

        RoundedButton btnCreate = new RoundedButton("Create Account", ACCENT);
        RoundedButton btnDeposit = new RoundedButton("Deposit", new Color(0x60A5FA));
        RoundedButton btnWithdraw = new RoundedButton("Withdraw", WARN);
        RoundedButton btnBalance = new RoundedButton("Check Balance", new Color(0xA78BFA));
        RoundedButton btnAll = new RoundedButton("View All", new Color(0x34D399));
        RoundedButton btnExit = new RoundedButton("Exit", BAD);

        for (RoundedButton b : List.of(btnCreate, btnDeposit, btnWithdraw, btnBalance, btnAll, btnExit)) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            sidebar.add(b);
            sidebar.add(Box.createVerticalStrut(10));
        }

        body.add(sidebar, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBackground(BG);

        JPanel topBar = cardPanel();
        topBar.setLayout(new BorderLayout(10, 10));
        topBar.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        selector.setOpaque(false);
        selector.add(label("Select Account:", 13, MUTED, false));

        accountCombo.setPreferredSize(new Dimension(220, 30));
        styleCombo(accountCombo);

        RoundedButton refreshBtn = new RoundedButton("Refresh", new Color(0x64748B));
        refreshBtn.setPreferredSize(new Dimension(110, 34));

        selector.add(accountCombo);
        selector.add(refreshBtn);

        topBar.add(selector, BorderLayout.WEST);

        JPanel quickInfo = new JPanel(new GridLayout(1, 3, 10, 0));
        quickInfo.setOpaque(false);
        quickInfo.add(infoTile("Account No.", accValue));
        quickInfo.add(infoTile("Name", nameValue));
        quickInfo.add(infoTile("Balance", balValue));

        topBar.add(quickInfo, BorderLayout.CENTER);

        content.add(topBar, BorderLayout.NORTH);

        JPanel tableCard = cardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel tableTitle = label("ACCOUNTS", 12, MUTED, true);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Account No.", "Name", "Balance"}, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(PANEL);
        tableCard.add(sp, BorderLayout.CENTER);

        content.add(tableCard, BorderLayout.CENTER);

        JPanel footer = cardPanel();
        footer.setBorder(new EmptyBorder(10, 12, 10, 12));
        footer.setLayout(new BorderLayout(10, 0));

        statusLabel.setForeground(TEXT);

        statusPill.setOpaque(true);
        statusPill.setBackground(GOOD);
        statusPill.setForeground(Color.WHITE);
        statusPill.setBorder(new EmptyBorder(4, 10, 4, 10));

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(statusPill, BorderLayout.EAST);

        content.add(footer, BorderLayout.SOUTH);

        body.add(content, BorderLayout.CENTER);

        btnCreate.addActionListener(e -> createAccountDialog());
        btnDeposit.addActionListener(e -> depositDialog());
        btnWithdraw.addActionListener(e -> withdrawDialog());
        btnBalance.addActionListener(e -> checkBalance());
        btnAll.addActionListener(e -> showAllAccountsDialog());
        btnExit.addActionListener(e -> dispose());

        refreshBtn.addActionListener(e -> refreshAccountsUI());

        accountCombo.addActionListener(e -> updateSelectedAccountDetails());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                String accNo = String.valueOf(tableModel.getValueAt(table.getSelectedRow(), 0));
                accountCombo.setSelectedItem(accNo);
                updateSelectedAccountDetails();
            }
        });
    }

    private JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        p.setBorder(new LineBorder(BORDER, 1, true));
        return p;
    }

    private JLabel label(String text, int size, Color color, boolean bold) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        return l;
    }

    private JPanel infoTile(String title, JLabel valueLabel) {
        JPanel tile = new JPanel(new BorderLayout(6, 6));
        tile.setOpaque(false);

        JLabel t = label(title, 12, MUTED, true);

        valueLabel.setForeground(TEXT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tile.add(t, BorderLayout.NORTH);
        tile.add(valueLabel, BorderLayout.CENTER);
        return tile;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(PANEL);
        combo.setForeground(TEXT);
        combo.setBorder(new LineBorder(BORDER, 1, true));
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void styleTable(JTable t) {
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setForeground(TEXT);
        t.setBackground(PANEL);
        t.setGridColor(BORDER);
        t.setSelectionBackground(new Color(0x1D4ED8));
        t.setSelectionForeground(Color.WHITE);

        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(new Color(0x0B1220));
        t.getTableHeader().setForeground(TEXT);
        t.getTableHeader().setBorder(new LineBorder(BORDER, 1, true));
    }

    private void setStatus(String msg, Color pillColor, String pillText) {
        statusLabel.setText(msg);
        statusPill.setText(" " + pillText + " ");
        statusPill.setBackground(pillColor);
    }

    private void refreshAccountsUI() {
        String prev = (String) accountCombo.getSelectedItem();
        accountCombo.removeAllItems();

        List<String> accNos = accounts.keySet().stream().sorted().toList();
        for (String a : accNos) accountCombo.addItem(a);

        tableModel.setRowCount(0);
        for (String a : accNos) {
            BankAccount acc = accounts.get(a);
            tableModel.addRow(new Object[]{
                    acc.getAccNo(),
                    acc.getName(),
                    String.format("₱%,.2f", acc.getBalance())
            });
        }

        if (!accNos.isEmpty()) {
            if (prev != null && accounts.containsKey(prev)) accountCombo.setSelectedItem(prev);
            else accountCombo.setSelectedIndex(0);
            updateSelectedAccountDetails();
            setStatus("Loaded " + accNos.size() + " account(s).", GOOD, "OK");
        } else {
            accValue.setText("—");
            nameValue.setText("—");
            balValue.setText("₱0.00");
            setStatus("No accounts yet. Create an account to begin.", WARN, "INFO");
        }
    }

    private void updateSelectedAccountDetails() {
        String accNo = (String) accountCombo.getSelectedItem();
        if (accNo == null || accNo.isBlank() || !accounts.containsKey(accNo)) {
            accValue.setText("—");
            nameValue.setText("—");
            balValue.setText("₱0.00");
            return;
        }

        BankAccount acc = accounts.get(accNo);
        accValue.setText(acc.getAccNo());
        nameValue.setText(acc.getName());
        balValue.setText(String.format("₱%,.2f", acc.getBalance()));

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (String.valueOf(tableModel.getValueAt(i, 0)).equals(accNo)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                break;
            }
        }
    }

    private BankAccount getSelectedAccountOrWarn() {
        String accNo = (String) accountCombo.getSelectedItem();
        if (accNo == null || accNo.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select an account first.",
                    "No Account Selected", JOptionPane.WARNING_MESSAGE);
            setStatus("Select an account before doing transactions.", WARN, "WARN");
            return null;
        }
        BankAccount acc = accounts.get(accNo);
        if (acc == null) {
            JOptionPane.showMessageDialog(this, "Selected account not found.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            setStatus("Account not found. Refreshing.", BAD, "ERR");
            refreshAccountsUI();
            return null;
        }
        return acc;
    }

    private void createAccountDialog() {
        JTextField accNoField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField balField = new JTextField("0");

        JPanel panel = formPanel();
        panel.add(formLabel("Account No.:")); panel.add(accNoField);
        panel.add(formLabel("Name:")); panel.add(nameField);
        panel.add(formLabel("Initial Balance:")); panel.add(balField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String accNo = accNoField.getText().trim();
        String name = nameField.getText().trim();
        String balStr = balField.getText().trim();

        if (accNo.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account number and name are required.",
                    "Missing", JOptionPane.WARNING_MESSAGE);
            setStatus("Create failed: missing fields.", WARN, "WARN");
            return;
        }
        if (accounts.containsKey(accNo)) {
            JOptionPane.showMessageDialog(this, "Account number already exists.",
                    "Duplicate", JOptionPane.ERROR_MESSAGE);
            setStatus("Create failed: duplicate account no.", BAD, "ERR");
            return;
        }

        double bal;
        try {
            bal = Double.parseDouble(balStr);
            if (bal < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Initial balance must be a non-negative number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            setStatus("Create failed: invalid balance.", BAD, "ERR");
            return;
        }

        accounts.put(accNo, new BankAccount(accNo, name, bal));
        setStatus("Account created: " + accNo, GOOD, "OK");
        refreshAccountsUI();
        accountCombo.setSelectedItem(accNo);
        updateSelectedAccountDetails();
    }

    private void depositDialog() {
        BankAccount acc = getSelectedAccountOrWarn();
        if (acc == null) return;

        String amountStr = JOptionPane.showInputDialog(this,
                "Deposit amount for " + acc.getAccNo() + " (" + acc.getName() + "):",
                "Deposit", JOptionPane.PLAIN_MESSAGE);
        if (amountStr == null) return;

        try {
            double amount = Double.parseDouble(amountStr.trim());
            acc.deposit(amount);
            JOptionPane.showMessageDialog(this,
                    String.format("Deposited ₱%,.2f", amount),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Deposit successful.", GOOD, "OK");
            refreshAccountsUI();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            setStatus("Deposit failed: invalid input.", BAD, "ERR");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            setStatus("Deposit failed: " + ex.getMessage(), BAD, "ERR");
        }
    }

    private void withdrawDialog() {
        BankAccount acc = getSelectedAccountOrWarn();
        if (acc == null) return;

        String amountStr = JOptionPane.showInputDialog(this,
                "Withdraw amount for " + acc.getAccNo() + " (" + acc.getName() + "):",
                "Withdraw", JOptionPane.PLAIN_MESSAGE);
        if (amountStr == null) return;

        try {
            double amount = Double.parseDouble(amountStr.trim());
            acc.withdraw(amount);
            JOptionPane.showMessageDialog(this,
                    String.format("Withdrew ₱%,.2f", amount),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            setStatus("Withdraw successful.", GOOD, "OK");
            refreshAccountsUI();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            setStatus("Withdraw failed: invalid input.", BAD, "ERR");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            setStatus("Withdraw failed: " + ex.getMessage(), BAD, "ERR");
        }
    }

    private void checkBalance() {
        BankAccount acc = getSelectedAccountOrWarn();
        if (acc == null) return;

        JOptionPane.showMessageDialog(this,
                "Account: " + acc.getAccNo() + "\n" +
                        "Name: " + acc.getName() + "\n" +
                        String.format("Balance: ₱%,.2f", acc.getBalance()),
                "Balance", JOptionPane.INFORMATION_MESSAGE);

        setStatus("Balance checked.", new Color(0x60A5FA), "INFO");
        updateSelectedAccountDetails();
    }

    private void showAllAccountsDialog() {
        if (accounts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No accounts to show.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            setStatus("No accounts to show.", WARN, "INFO");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ALL ACCOUNTS\n");
        sb.append("-----------\n");
        accounts.keySet().stream().sorted().forEach(accNo -> {
            BankAccount a = accounts.get(accNo);
            sb.append(a.getAccNo()).append(" | ")
                    .append(a.getName()).append(" | ")
                    .append(String.format("₱%,.2f", a.getBalance()))
                    .append("\n");
        });

        JTextArea area = new JTextArea(sb.toString(), 16, 46);
        area.setEditable(false);
        area.setBackground(PANEL);
        area.setForeground(TEXT);
        area.setCaretPosition(0);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "All Accounts", JOptionPane.INFORMATION_MESSAGE);

        setStatus("Displayed all accounts.", new Color(0x34D399), "OK");
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.setBackground(PANEL);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    static class RoundedButton extends JButton {
        private final Color fill;

        RoundedButton(String text, Color fill) {
            super(text);
            this.fill = fill;
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 16, 10, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color c = fill;
            if (getModel().isPressed()) c = c.darker();
            else if (getModel().isRollover()) c = c.brighter();

            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(getForeground());
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    class GradientHeader extends JPanel {
        GradientHeader() {
            setPreferredSize(new Dimension(0, 78));
            setBorder(new EmptyBorder(14, 16, 14, 16));
            setLayout(new BorderLayout());
            setOpaque(false);

            JLabel title = new JLabel("Simple Banking Application");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Segoe UI", Font.BOLD, 20));

            JLabel subtitle = new JLabel("Create accounts, deposit, withdraw, and view balances");
            subtitle.setForeground(new Color(255, 255, 255, 200));
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(title);
            left.add(Box.createVerticalStrut(4));
            left.add(subtitle);

            add(left, BorderLayout.WEST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0x2563EB), // blue-600
                    getWidth(), getHeight(), new Color(0x06B6D4) // cyan-500
            );
            g2.setPaint(gp);
            g2.fillRoundRect(10, 10, getWidth() - 20, getHeight() - 20, 20, 20);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleBankingApplication().setVisible(true));
    }
}