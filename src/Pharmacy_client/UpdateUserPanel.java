package Pharmacy_client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;            // Pour PrintWriter, BufferedReader, InputStreamReader
import java.net.Socket;
import java.sql.*;
public class UpdateUserPanel extends JPanel {
    private JTextField txtUsername, txtName, txtEmail, txtMobile, txtAddress, txtDOB;
    private JComboBox<String> comboUserRole;

    private final String emailPattern = "^[a-zA-Z0-9]+[@]+[a-zA-Z0-9]+[.]+[a-zA-Z0-9]+$";
    private final String mobilePattern = "^[0-9]*$";

    public UpdateUserPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fontLabel = new Font("Segoe UI", Font.PLAIN, 15);
        Font fontField = new Font("Segoe UI", Font.PLAIN, 14);

        // === Champs ===
        JLabel lblSearchUsername = new JLabel("Username:");
        lblSearchUsername.setFont(fontLabel);
        txtUsername = new JTextField();
        txtUsername.setFont(fontField);
        JButton btnSearch = new JButton("🔍 Search");
        btnSearch.setFont(fontField);
        btnSearch.setFocusPainted(false);

        JLabel lblRole = new JLabel("User Role:");
        lblRole.setFont(fontLabel);
        comboUserRole = new JComboBox<>(new String[]{"Admin", "Pharmacist"});
        comboUserRole.setFont(fontField);

        JLabel lblName = new JLabel("Name:");
        lblName.setFont(fontLabel);
        txtName = new JTextField();
        txtName.setFont(fontField);

        JLabel lblDOB = new JLabel("Date of Birth:");
        lblDOB.setFont(fontLabel);
        txtDOB = new JTextField("dd-MM-yyyy");
        txtDOB.setFont(fontField);

        JLabel lblMobile = new JLabel("Mobile Number:");
        lblMobile.setFont(fontLabel);
        txtMobile = new JTextField();
        txtMobile.setFont(fontField);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(fontLabel);
        txtEmail = new JTextField();
        txtEmail.setFont(fontField);

        JLabel lblAddress = new JLabel("Address:");
        lblAddress.setFont(fontLabel);
        txtAddress = new JTextField();
        txtAddress.setFont(fontField);

        JButton btnUpdate = new JButton("✏️ Update User");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnUpdate.setBackground(new Color(70, 130, 180));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // === Placement ===
        int y = 0;
        addRow(lblSearchUsername, txtUsername, btnSearch, gbc, y++);
        addRow(lblRole, comboUserRole, gbc, y++);
        addRow(lblName, txtName, gbc, y++);
        addRow(lblDOB, txtDOB, gbc, y++);
        addRow(lblMobile, txtMobile, gbc, y++);
        addRow(lblEmail, txtEmail, gbc, y++);
        addRow(lblAddress, txtAddress, gbc, y++);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        add(btnUpdate, gbc);

        // === Actions ===
        btnSearch.addActionListener(e -> searchUser());
        btnUpdate.addActionListener(e -> updateUser());
    }

    private void addRow(JLabel label, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        add(label, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(field, gbc);
        gbc.gridwidth = 1;
    }

    private void addRow(JLabel label, JComponent field, JButton btn, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        add(label, gbc);
        gbc.gridx = 1;
        add(field, gbc);
        gbc.gridx = 2;
        add(btn, gbc);
    }

    private void searchUser() {
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please enter a username to search.");
            return;
        }

        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM appuser WHERE username='" + username + "'");
            if (rs.next()) {
                txtUsername.setEditable(false);
                txtName.setText(rs.getString("name"));
                txtEmail.setText(rs.getString("email"));
                txtMobile.setText(rs.getString("mobileNumber"));
                txtAddress.setText(rs.getString("address"));
                txtDOB.setText(rs.getString("dob"));
                comboUserRole.setSelectedItem(rs.getString("userRole"));
            } else {
                JOptionPane.showMessageDialog(this, "❌ User not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e);
        }
    }

    private void updateUser() {
        String username = txtUsername.getText().trim();
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String mobile = txtMobile.getText().trim();
        String address = txtAddress.getText().trim();
        String dob = txtDOB.getText().trim();
        String role = (String) comboUserRole.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || address.isEmpty() || dob.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ All fields are required.");
        } else if (!email.matches(emailPattern)) {
            JOptionPane.showMessageDialog(this, "📧 Invalid email format.");
        } else if (!mobile.matches(mobilePattern) || mobile.length() != 8) {
            JOptionPane.showMessageDialog(this, "📞 Invalid mobile number.");
        } else {
            try {
                // Connexion au serveur
                Socket socket = new Socket("127.0.0.1", 9000);
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Envoyer la requête
                pw.println("updateUser");
                pw.println(username);
                pw.println(name);
                pw.println(dob);
                pw.println(mobile);
                pw.println(email);
                pw.println(address);
                pw.println(role);

                String response = br.readLine();
                if ("success".equals(response)) {
                    JOptionPane.showMessageDialog(this, "✅ User updated successfully.");
                    txtUsername.setEditable(true);
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Failed to update user.");
                }

                socket.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
