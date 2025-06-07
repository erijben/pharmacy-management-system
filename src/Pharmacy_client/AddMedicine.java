package Pharmacy_client;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import java.io.*; //Lecture/écriture via le Socket

public class AddMedicine extends JPanel {
    //declaration des composants
    private JTextField txtId, txtName, txtCompany, txtQty, txtPrice;
    private final String numberPattern = "^[0-9]*$"; //numberPattern utilisée pour vérifier que la quantité est bien numérique

    public AddMedicine() {
        setLayout(new GridBagLayout()); //GridBagLayout pour positionner les éléments
        setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        // Champs
        txtId = createField(fieldFont);
        txtName = createField(fieldFont);
        txtCompany = createField(fieldFont);
        txtQty = createField(fieldFont);
        txtPrice = createField(fieldFont);

        // Bouton d’enregistrement declenche la methode savamedicine()
        JButton btnSave = new JButton("💾 Save Medicine");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnSave.setBackground(new Color(52, 152, 219));
        btnSave.setForeground(Color.WHITE);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveMedicine());

        // Ajout des composants dans la grille
        int y = 0;
        addRow("Medicine ID:", txtId, gbc, y++, labelFont);
        addRow("Name:", txtName, gbc, y++, labelFont);
        addRow("Company Name:", txtCompany, gbc, y++, labelFont);
        addRow("Quantity:", txtQty, gbc, y++, labelFont);
        addRow("Price per Unit:", txtPrice, gbc, y++, labelFont);

        gbc.gridx = 1;
        gbc.gridy = y;
        gbc.gridwidth = 2;
        add(btnSave, gbc);
    }

    //methode pour creer rapidement un champ texte avec une police donnée
    private JTextField createField(Font font) {
        JTextField field = new JTextField();
        field.setFont(font);
        return field;
    }

    //methode addrow
    private void addRow(String labelText, JTextField field, GridBagConstraints gbc, int y, Font font) {
        JLabel label = new JLabel(labelText);
        label.setFont(font);

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        add(label, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(field, gbc);
    }

    private void saveMedicine() {
          // Lire les champs
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String company = txtCompany.getText().trim();
        String qty = txtQty.getText().trim();
        String price = txtPrice.getText().trim();
       // Validation de base : aucun champ vide
        if (id.isEmpty() || name.isEmpty() || company.isEmpty() || qty.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ All fields are required.");
            return;
        }
        // Vérification quantité numérique
        if (!qty.matches(numberPattern)) {
            JOptionPane.showMessageDialog(this, "📦 Quantity must be numeric.");
            return;
        }
// Vérification prix numérique
        String priceNormalized = price.replace(',', '.');
        try {
            Double.parseDouble(priceNormalized);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "💰 Price must be numeric (e.g., 12.5 or 12,5).");
            return;
        }

        //communication avec le serveur via socket
        try (
                //connexion au serveur
                Socket socket = new Socket("127.0.0.1", 9000);
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("addMedicine"); //demande d'ajout
            pw.println(id);
            pw.println(name);
            pw.println(company);
            pw.println(qty);
            pw.println(priceNormalized);

            // Lire la réponse du serveur
            String response = br.readLine();
            //Le serveur répond "success" ou "fail"
            if ("success".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, "✅ Medicine added successfully!");
                clearFields(); //appel du methode pour effacer les champs
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to add medicine.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "❌ Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Réinitialisation du formulaire
    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtCompany.setText("");
        txtQty.setText("");
        txtPrice.setText("");
    }

    //tester cette interface seule
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Add Medicine");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new AddMedicine());
            frame.pack();
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
