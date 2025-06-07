package Pharmacy_client;

import javax.swing.*;
import java.awt.*;

public class PharmacistDashboard extends JFrame {
    private String username;
    private JPanel contentPanel;
    private JPanel sidebar, headerPanel, buttonPanel;
    private JLabel welcomeLabel;
    private boolean darkMode = false;

    private final ImageIcon moonIcon;
    private final ImageIcon sunIcon;

    private final Color LIGHT_BG = Color.WHITE;
    private final Color DARK_BG = new Color(36, 36, 36);
    private final Color SIDEBAR_LIGHT = new Color(44, 62, 80);
    private final Color SIDEBAR_DARK = new Color(28, 28, 28);

    public PharmacistDashboard(String username) {
        this.username = username;
        setTitle("Pharmacist Dashboard - " + username);
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        moonIcon = new ImageIcon(getClass().getResource("/images/dark.png"));
        sunIcon = new ImageIcon(getClass().getResource("/images/light.png"));

        // Sidebar
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Top header (Icons only)
        JPanel headerBar = createHeaderBar();
        add(headerBar, BorderLayout.NORTH);

        // Content
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(LIGHT_BG);
        add(contentPanel, BorderLayout.CENTER);

        showPanel(new PharmacistHomePanel(username));
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(SIDEBAR_LIGHT);

        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(SIDEBAR_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        ImageIcon icon = new ImageIcon(getClass().getResource("/images/icon_pharma.png"));
        Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        JLabel profilePic = new JLabel(new ImageIcon(scaledImage));
        profilePic.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        headerPanel.add(profilePic);
        headerPanel.add(welcomeLabel);

        buttonPanel = new JPanel(new GridLayout(8, 1, 10, 10));
        buttonPanel.setBackground(SIDEBAR_LIGHT);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        String[] labels = {
                "Add Medicine", "Sell Medicine", "View Medicine", "View Bill",
                "Update Medicine", "Profile", "Logout", "Exit"
        };

        JButton[] buttons = new JButton[labels.length];
        for (int i = 0; i < labels.length; i++) {
            buttons[i] = new JButton(labels[i]);
            buttons[i].setFocusPainted(false);
            buttons[i].setBackground(new Color(52, 73, 94));
            buttons[i].setForeground(Color.WHITE);
            buttons[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
            buttonPanel.add(buttons[i]);
        }

        buttons[0].addActionListener(e -> showPanel(new AddMedicine()));
        buttons[1].addActionListener(e -> showPanel(new SellMedicine(username)));
        buttons[2].addActionListener(e -> showPanel(new ViewMedicine()));
        buttons[3].addActionListener(e -> showPanel(new ViewBill()));
        buttons[4].addActionListener(e -> showPanel(new UpdateMedicine()));
        buttons[5].addActionListener(e -> showPanel(new ProfilePanel(username)));
        buttons[6].addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                setVisible(false);
                new Login().setVisible(true);
            }
        });
        buttons[7].addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit application?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                System.exit(0);
            }
        });

        sidebar.add(headerPanel, BorderLayout.NORTH);
        sidebar.add(buttonPanel, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Panel vide à gauche (équilibrage)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        // Panel à droite pour les icônes
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        // Icônes de droite dans l'ordre souhaité
        rightPanel.add(createIcon("/images/mail.png"));
        rightPanel.add(createIcon("/images/notif.png"));


        // 🌙 Thème
        JButton themeBtn = new JButton(new ImageIcon(getClass().getResource("/images/dark.png")));
        themeBtn.setContentAreaFilled(false);
        themeBtn.setBorderPainted(false);
        themeBtn.setFocusPainted(false);
        themeBtn.setToolTipText("Toggle Theme");
        themeBtn.addActionListener(e -> toggleDarkMode());
        rightPanel.add(themeBtn);

        // 🏠 Home (placé après dark mode)
        ImageIcon homeIcon = new ImageIcon(getClass().getResource("/images/home.png"));
        Image scaledHome = homeIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JButton homeBtn = new JButton(new ImageIcon(scaledHome));
        homeBtn.setToolTipText("Home");
        homeBtn.setToolTipText("Home");
        homeBtn.setContentAreaFilled(false);
        homeBtn.setBorderPainted(false);
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> showPanel(new PharmacistHomePanel(username)));
        rightPanel.add(homeBtn);

        rightPanel.add(createIcon("/images/icon_pharma.png"));
        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }




    private JButton createIcon(String path) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JButton btn = new JButton(new ImageIcon(scaled));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        return btn;
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        Color bg = darkMode ? DARK_BG : LIGHT_BG;
        Color sidebarBg = darkMode ? SIDEBAR_DARK : SIDEBAR_LIGHT;
        Color fg = darkMode ? Color.WHITE : Color.BLACK;

        contentPanel.setBackground(bg);
        sidebar.setBackground(sidebarBg);
        headerPanel.setBackground(sidebarBg);
        buttonPanel.setBackground(sidebarBg);
        welcomeLabel.setForeground(fg);

        for (Component c : buttonPanel.getComponents()) {
            if (c instanceof JButton btn) {
                btn.setBackground(darkMode ? new Color(70, 70, 70) : new Color(52, 73, 94));
                btn.setForeground(fg);
            }
        }

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PharmacistDashboard("pharmacien").setVisible(true));
    }

}
