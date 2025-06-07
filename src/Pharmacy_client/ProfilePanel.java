package Pharmacy_client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ProfilePanel extends JPanel {
    private String username;
    private JTextField txtName, txtMobile, txtEmail, txtAddress;
    private JLabel lblUsername;

    private final String emailPattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$";

    private final String mobilePattern = "^[0-9]*$";

    public ProfilePanel(String tempUsername) {
        this.username = tempUsername;

        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel icon = new JLabel(new ImageIcon(getClass().getResource("/images/icon_users.png")));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(icon, gbc);

        lblUsername = new JLabel("Logged in as: " + username);
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblUsername.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        add(lblUsername, gbc);
        gbc.gridwidth = 1;

        txtName = createField(fieldFont);
        txtMobile = createField(fieldFont);
        txtEmail = createField(fieldFont);
        txtAddress = createField(fieldFont);

        int y = 2;
        addRow("Name:", txtName, gbc, y++, labelFont);
        addRow("Mobile Number:", txtMobile, gbc, y++, labelFont);
        addRow("Email:", txtEmail, gbc, y++, labelFont);
        addRow("Address:", txtAddress, gbc, y++, labelFont);

        JButton btnUpdate = new JButton("🔄 Update Profile");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUpdate.setFocusPainted(false);
        btnUpdate.addActionListener(e -> updateProfile());

        gbc.gridx = 1;
        gbc.gridy = y;
        add(btnUpdate, gbc);

        // 🚀 Charger les infos via serveur
        loadUserData();
    }

    private JTextField createField(Font font) {
        JTextField field = new JTextField();
        field.setFont(font);
        return field;
    }

    private void addRow(String labelText, JTextField field, GridBagConstraints gbc, int y, Font labelFont) {
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);

        gbc.gridx = 0;
        gbc.gridy = y;
        add(label, gbc);

        gbc.gridx = 1;
        add(field, gbc);
    }

    // lit les données (getProfile) depuis le serveur.
    private void loadUserData() {
        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("getProfile");
            pw.println(username);

            String response = br.readLine();
            if (!response.equals("not_found")) {
                String[] data = response.split("::");
                txtName.setText(data[0]);
                txtMobile.setText(data[1]);
                txtEmail.setText(data[2]);
                txtAddress.setText(data[3]);
            } else {
                JOptionPane.showMessageDialog(this, "User not found");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
    }

    private void updateProfile() {
        String name = txtName.getText().trim();
        String mobile = txtMobile.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ All fields are required.");
            return;
        }
        if (!mobile.matches(mobilePattern) || mobile.length() != 8) {
            JOptionPane.showMessageDialog(this, "📞 Invalid mobile number.");
            return;
        }
        if (!email.matches(emailPattern)) {
            JOptionPane.showMessageDialog(this, "📧 Invalid email format.");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("updateProfile");
            pw.println(username);
            pw.println(name);
            pw.println(mobile);
            pw.println(email);
            pw.println(address);

            String response = br.readLine();
            if ("success".equals(response)) {
                JOptionPane.showMessageDialog(this, "✅ Profile updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to update profile.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
        }
    }
}