package Pharmacy_client;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class AddUserPanel extends JPanel {
    //attributs
    private JTextField txtName, txtMobile, txtEmail, txtUsername, txtAddress;
    private JPasswordField txtPassword;
    private JDateChooser dateChooser;
    private JComboBox<String> comboRole;
    private JLabel iconLabel;
    private int checkUsername = 0; //verifier si le user name existe deja

    public AddUserPanel() {
        //Configuration du panneau
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font fontLabel = new Font("Segoe UI", Font.PLAIN, 15);
        Font fontField = new Font("Segoe UI", Font.PLAIN, 14);

        // Création des champs
        //Champs et labels pour tous les éléments du formulaire.
        JLabel lblRole = new JLabel("User Role:");
        lblRole.setFont(fontLabel);
        comboRole = new JComboBox<>(new String[]{"Admin", "Pharmacist"});
        comboRole.setFont(fontField);

        JLabel lblName = new JLabel("Name:");
        lblName.setFont(fontLabel);
        txtName = new JTextField();
        txtName.setFont(fontField);

        JLabel lblDOB = new JLabel("Date of Birth:");
        lblDOB.setFont(fontLabel);
        //Fournit un sélecteur de date graphique(calendrier)
        dateChooser = new JDateChooser(); //sélectionne sa Date de naissance
        dateChooser.setDateFormatString("dd-MM-yyyy");
        dateChooser.setFont(fontField);

        JLabel lblMobile = new JLabel("Mobile Number:");
        lblMobile.setFont(fontLabel);
        txtMobile = new JTextField();
        txtMobile.setFont(fontField);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(fontLabel);
        txtEmail = new JTextField();
        txtEmail.setFont(fontField);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(fontLabel);
        txtUsername = new JTextField();
        txtUsername.setFont(fontField);

        iconLabel = new JLabel();
        iconLabel.setVisible(false);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(fontLabel);
        txtPassword = new JPasswordField();
        txtPassword.setFont(fontField);

        JLabel lblAddress = new JLabel("Address:");
        lblAddress.setFont(fontLabel);
        txtAddress = new JTextField();
        txtAddress.setFont(fontField);

        //bouton save user
        JButton btnSave = new JButton("➕ Save User");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setBackground(new Color(70, 130, 180));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveUser());

        // === Ajout des champs dans le panneau avec bonne organisation
        //avec addrow
        int y = 0;
        addRow(lblRole, comboRole, gbc, y++);
        addRow(lblName, txtName, gbc, y++);
        addRow(lblDOB, dateChooser, gbc, y++);
        addRow(lblMobile, txtMobile, gbc, y++);
        addRow(lblEmail, txtEmail, gbc, y++);
        addRow(lblUsername, txtUsername, iconLabel, gbc, y++);
        addRow(lblPassword, txtPassword, gbc, y++);
        addRow(lblAddress, txtAddress, gbc, y++);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        add(btnSave, gbc);

        //Vérification automatique du Username
        //ça vérifie en temps réel s’il est disponible
        txtUsername.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                checkUsernameAvailable();
            }
        });
    }

    private void addRow(JLabel label, JComponent field, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        add(label, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(field, gbc);
        gbc.gridwidth = 1;
    }

    private void addRow(JLabel label, JComponent field, JLabel icon, GridBagConstraints gbc, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        add(label, gbc);
        gbc.gridx = 1;
        add(field, gbc);
        gbc.gridx = 2;
        add(icon, gbc);
    }

    //Méthode
    //Vérifie via socket (serveur) si le Username existe déjà dans la base
    private void checkUsernameAvailable() {
        String username = txtUsername.getText();
        iconLabel.setVisible(false);
        checkUsername = 0;

        if (!username.isEmpty()) {
            try (Socket socket = new Socket("127.0.0.1", 9000);
                 PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                pw.println("checkUsername");
                pw.println(username);

                String response = br.readLine();
                if ("exists".equals(response)) {
                    checkUsername = 1;
                    iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/no.png")));
                } else if ("available".equals(response)) {
                    iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/yes.png")));
                }
                iconLabel.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "⚠️ Error checking username: " + e.getMessage());
            }
        }
    }

    //methode saveUser()
    private void saveUser() {
        String userRole = (String) comboRole.getSelectedItem();
        String name = txtName.getText();
        String mobile = txtMobile.getText();
        String email = txtEmail.getText();
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        String address = txtAddress.getText();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String dob = (dateChooser.getDate() != null) ? sdf.format(dateChooser.getDate()) : "";

        String emailPattern = "^[a-zA-Z0-9]+[@]+[a-zA-Z0-9]+[.]+[a-zA-Z0-9]+$";
        String mobilePattern = "^[0-9]*$";

        if (name.isEmpty() || dob.isEmpty() || mobile.isEmpty() || email.isEmpty() ||
                username.isEmpty() || password.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        } else if (!mobile.matches(mobilePattern) || mobile.length() != 8) {
            JOptionPane.showMessageDialog(this, "Invalid mobile number");
            return;
        } else if (!email.matches(emailPattern)) {
            JOptionPane.showMessageDialog(this, "Invalid email address");
            return;
        } else if (checkUsername == 1) {
            JOptionPane.showMessageDialog(this, "Username already exists");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            //Envoie toutes les informations au serveur via socket
            pw.println("addUser");
            pw.println(userRole);
            pw.println(name);
            pw.println(dob);
            pw.println(mobile);
            pw.println(email);
            pw.println(username);
            pw.println(password);
            pw.println(address);

            String response = br.readLine();
            if ("success".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, "✅ User added successfully");
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to add user");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Add User Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Add User");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new AddUserPanel());
            frame.pack();
            frame.setSize(500, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
