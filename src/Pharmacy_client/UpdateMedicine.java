package Pharmacy_client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class UpdateMedicine extends JPanel {
    private JTextField txtMedicineId, txtName, txtCompanyName, txtQuantity, txtAddQuantity, txtPricePerUnit;

    public UpdateMedicine() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(30, 40, 30, 40),
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMedicineId = new JTextField();
        txtName = new JTextField();
        txtCompanyName = new JTextField();
        txtQuantity = new JTextField();
        txtQuantity.setEditable(false);
        txtAddQuantity = new JTextField();
        txtPricePerUnit = new JTextField();

        JButton btnSearch = new JButton("🔍 Search");
        JButton btnUpdate = new JButton("💾 Update");

        int y = 0;
        addRow(new JLabel("Medicine ID:"), txtMedicineId, btnSearch, gbc, formPanel, y++);
        addRow(new JLabel("Name:"), txtName, gbc, formPanel, y++);
        addRow(new JLabel("Company Name:"), txtCompanyName, gbc, formPanel, y++);
        addRow(new JLabel("Quantity:"), txtQuantity, gbc, formPanel, y++);
        addRow(new JLabel("Add Quantity:"), txtAddQuantity, gbc, formPanel, y++);
        addRow(new JLabel("Price per Unit:"), txtPricePerUnit, gbc, formPanel, y++);

        gbc.gridx = 1;
        gbc.gridy = y;
        formPanel.add(btnUpdate, gbc);

        add(formPanel, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> searchMedicine());
        btnUpdate.addActionListener(e -> updateMedicine());
    }

    private void addRow(JLabel lbl, JTextField field, GridBagConstraints gbc, JPanel panel, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void addRow(JLabel lbl, JTextField field, JButton btn, GridBagConstraints gbc, JPanel panel, int y) {
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridx = 2;
        panel.add(btn, gbc);
    }

    private void searchMedicine() {
        String medicineId = txtMedicineId.getText().trim();
        if (medicineId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Medicine ID is required");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("searchMedicineById");
            pw.println(medicineId);

            String response = br.readLine();
            if (response.equals("not_found")) {
                JOptionPane.showMessageDialog(this, "Medicine ID does not exist.");
            } else {
                String[] data = response.split("::");
                txtMedicineId.setEditable(false);
                txtName.setText(data[0]);
                txtCompanyName.setText(data[1]);
                txtQuantity.setText(data[2]);
                txtPricePerUnit.setText(data[3]);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de recherche: " + e.getMessage());
        }
    }

    private void updateMedicine() {
        String id = txtMedicineId.getText().trim();
        String name = txtName.getText().trim();
        String company = txtCompanyName.getText().trim();
        String quantity = txtQuantity.getText().trim();
        String addQty = txtAddQuantity.getText().trim();
        String price = txtPricePerUnit.getText().trim();

        int totalQty;
        try {
            totalQty = Integer.parseInt(quantity) + (addQty.equals("") ? 0 : Integer.parseInt(addQty));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity format");
            return;
        }

        if (name.isEmpty() || company.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }

        // 🔥 Nouvelle validation du prix : accepter les nombres décimaux
        try {
            Double.parseDouble(price.replace(',', '.'));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("updateMedicine");
            pw.println(id);
            pw.println(name);
            pw.println(company);
            pw.println(String.valueOf(totalQty));
            pw.println(price.replace(',', '.'));

            String response = br.readLine();
            if ("success".equals(response)) {
                JOptionPane.showMessageDialog(this, "✅ Medicine updated successfully");
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to update medicine.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de mise à jour: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtMedicineId.setEditable(true);
        txtMedicineId.setText("");
        txtName.setText("");
        txtCompanyName.setText("");
        txtQuantity.setText("");
        txtAddQuantity.setText("");
        txtPricePerUnit.setText("");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Update Medicine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new UpdateMedicine());
        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
