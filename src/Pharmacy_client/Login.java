package Pharmacy_client;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.io.*;
import java.net.Socket; // ← Ce qu’il manque !
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Login extends JFrame {
    //2 champs de formulaire : username et password
    private JTextField txtUsername;
    private JPasswordField txtPassword; //caché avec points

    //constructeur
    // configuration fenetre principalement
    public Login() {
        setTitle("Login - Pharmacy System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setResizable(true);

        // creation de mainpanel : conteneur vertical ( box layout)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(250, 250, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // ajouter image de Logo
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo_login.png"));
        Image scaled = icon.getImage().getScaledInstance(110, 110, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaled));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logoLabel);

        // ajouter un Titre
        JLabel title = new JLabel("Pharmacy Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(44, 62, 80));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        mainPanel.add(title);

        // ajouter le Formulaire avec les 2 champs et un bouton
        //utilisation de gridbaglayout pour l alignement
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(new Color(250, 250, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // espacement entre les elements de 10 pixels
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(labelFont);
        txtUsername = new JTextField(15);
        txtUsername.setFont(fieldFont);

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(labelFont);
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(fieldFont);

        JButton btnLogin = new JButton("🔐 Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setBackground(new Color(52, 152, 219));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(140, 40));
        btnLogin.addActionListener(e -> loginAction()); //il declenche la methode loginAction

        // Ajout champs
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(lblUsername, gbc);
        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(lblPassword, gbc);
        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        // Ajout bouton centré
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(btnLogin, gbc);

        mainPanel.add(formPanel);
        add(mainPanel); // au lieu de setContentPane (plus simple et flexible)
    }


    private void loginAction() {
//recuperer les valeurs de formulaire
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
//verifier si les champs ne sont pas vide
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password."); //si oui, il bloque l'action avec un message d'erreur.
            return;
        }

        // se connecter au serveur via un socket sur localhost :9000
        try {
            // Connexion socket vers le serveur (envoie 3 lignes au serveur
            Socket socket = new Socket("127.0.0.1", 9000);
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); //canal d'écriture pour envoyer des msgs au serveur
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); //canal de lecture pour recevoir les réponses du serveur

            //Envoyer 3 lignes au serveur
            pw.println("login"); // informe le serveur que tu fais demande de cnx
            pw.println(username); //envoyer le texte saisi
            pw.println(password);

            //attendre reponse de serveur (soit suces soit echec
            String response = br.readLine();
            if (response.equals("success")) {
                //l'utilisateur est reconnu.
                String role = br.readLine(); // Lire le rôle de l'utilisateur( le serveur envoie le role")
                setVisible(false);

                //redirection selon le role
                if (role.equalsIgnoreCase("Admin")) {
                    new AdminDashboard(username).setVisible(true);
                } else if (role.equalsIgnoreCase("Pharmacist")) {
                    new PharmacistDashboard(username).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Login failed: Incorrect username or password.");
            }

            socket.close(); //Toujours fermer proprement la connexion réseau après utilisation !

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
