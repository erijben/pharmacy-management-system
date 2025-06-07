package Pharmacy_client;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {
    private String username;
    private JPanel contentPanel;
    private JPanel sidebar, headerPanel, buttonPanel;
    private JLabel welcomeLabel;

    public AdminDashboard(String username) {
        this.username = username;
        setTitle("Admin Dashboard - " + username);
        setSize(1200, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === Sidebar ===
        sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // === TopBar modernisée ===
        JPanel headerBar = createHeaderBar();
        add(headerBar, BorderLayout.NORTH);

        // === Content Panel ===
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        add(contentPanel, BorderLayout.CENTER);

        showPanel(new AdminHomePanel(username));
    }

    //side bar
    private JPanel createSidebar() {
        Color sidebarColor = new Color(44, 62, 80);
        Color buttonColor = new Color(52, 73, 94);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(sidebarColor);

        // Header
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(sidebarColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        ImageIcon icon = new ImageIcon(getClass().getResource("/images/icon_admin.png"));
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

        // Boutons
        buttonPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        buttonPanel.setBackground(sidebarColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        String[] labels = { "Add User", "View User", "Update User", "Profile", "Logout", "Exit" };
        JButton[] buttons = new JButton[labels.length];

        for (int i = 0; i < labels.length; i++) {
            buttons[i] = new JButton(labels[i]);
            buttons[i].setFocusPainted(false);
            buttons[i].setBackground(buttonColor);
            buttons[i].setForeground(Color.WHITE);
            buttons[i].setFont(new Font("Segoe UI", Font.BOLD, 13));
            buttonPanel.add(buttons[i]);
        }

        buttons[0].addActionListener(e -> showPanel(new AddUserPanel()));
        buttons[1].addActionListener(e -> showPanel(new ViewUserPanel(username)));
        buttons[2].addActionListener(e -> showPanel(new UpdateUserPanel()));
        buttons[3].addActionListener(e -> showPanel(new ProfilePanel(username)));
        buttons[4].addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                setVisible(false);
                new Login().setVisible(true);
            }
        });
        buttons[5].addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Exit application?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                System.exit(0);
            }
        });

        sidebar.add(headerPanel, BorderLayout.NORTH);
        sidebar.add(buttonPanel, BorderLayout.CENTER);

        return sidebar;
    }

    //navbar
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        rightPanel.add(createIcon("/images/mail.png"));
        rightPanel.add(createIcon("/images/notif.png"));

        JButton themeBtn = new JButton(new ImageIcon(getClass().getResource("/images/dark.png")));
        themeBtn.setContentAreaFilled(false);
        themeBtn.setBorderPainted(false);
        themeBtn.setFocusPainted(false);
        themeBtn.setToolTipText("Toggle Theme");
        rightPanel.add(themeBtn);

        ImageIcon homeIcon = new ImageIcon(getClass().getResource("/images/home.png"));
        Image scaledHome = homeIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        JButton homeBtn = new JButton(new ImageIcon(scaledHome));
        homeBtn.setToolTipText("Home");
        homeBtn.setContentAreaFilled(false);
        homeBtn.setBorderPainted(false);
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> showPanel(new AdminHomePanel(username)));
        rightPanel.add(homeBtn);

        rightPanel.add(createIcon("/images/icon_admin.png"));

        header.add(new JPanel(), BorderLayout.WEST);  // vide à gauche
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

    private void showPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard("admin").setVisible(true));
    }
}
