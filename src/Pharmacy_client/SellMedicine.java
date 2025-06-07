package Pharmacy_client;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import Pharmacy_client.PharmacyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class SellMedicine extends JPanel {
    private final String numberPattern = "^[0-9]+$";
    private double finalTotalPrice = 0; // 🛠️ Changer en double pour gérer les prix décimaux
    private final String username;

    private JTable medicinesTable;
    private JTable cartTable;
    private JTextField txtSearch, txtUniqueId, txtName, txtCompanyName, txtPricePerUnit, txtNoOfUnits, txtTotalPrice;
    private JLabel lblFinalTotalPrice;
    private JButton btnAddToCart, btnConfirmSale;

    public SellMedicine(String username) { //Stocke le nom du pharmacien connecté
        this.username = username;
        initComponents();
    }

 //Appelle initComponents() pour tout construire graphiquement
    //ici tout l interface graphqiue est crée
    private void initComponents() {
        setLayout(null);
        setBackground(Color.WHITE);

        //barre de recherche
        JLabel lblSearch = new JLabel("Search");
        lblSearch.setBounds(20, 20, 80, 25);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(100, 20, 200, 25);
        add(txtSearch);

        //table des médicaments
        medicinesTable = new JTable(new DefaultTableModel(new Object[]{"Medicine"}, 0));
        JScrollPane scrollMed = new JScrollPane(medicinesTable);
        scrollMed.setBounds(20, 60, 280, 400);
        add(scrollMed);

        //formulaire avec details
        JLabel lblId = new JLabel("Medicine ID");
        lblId.setBounds(320, 20, 100, 25);
        add(lblId);

        txtUniqueId = new JTextField();
        txtUniqueId.setBounds(450, 20, 150, 25);
        txtUniqueId.setEditable(false);
        add(txtUniqueId);

        JLabel lblName = new JLabel("Name");
        lblName.setBounds(320, 60, 100, 25);
        add(lblName);

        txtName = new JTextField();
        txtName.setBounds(450, 60, 150, 25);
        txtName.setEditable(false);
        add(txtName);

        JLabel lblCompany = new JLabel("Company");
        lblCompany.setBounds(320, 100, 100, 25);
        add(lblCompany);

        txtCompanyName = new JTextField();
        txtCompanyName.setBounds(450, 100, 150, 25);
        txtCompanyName.setEditable(false);
        add(txtCompanyName);

        JLabel lblPrice = new JLabel("Price/Unit");
        lblPrice.setBounds(650, 20, 100, 25);
        add(lblPrice);

        txtPricePerUnit = new JTextField();
        txtPricePerUnit.setBounds(750, 20, 150, 25);
        txtPricePerUnit.setEditable(false);
        add(txtPricePerUnit);

        JLabel lblNoOfUnits = new JLabel("No. of Units");
        lblNoOfUnits.setBounds(650, 60, 100, 25);
        add(lblNoOfUnits);

        txtNoOfUnits = new JTextField();
        txtNoOfUnits.setBounds(750, 60, 150, 25);
        add(txtNoOfUnits);

        JLabel lblTotal = new JLabel("Total");
        lblTotal.setBounds(650, 100, 100, 25);
        add(lblTotal);

        txtTotalPrice = new JTextField();
        txtTotalPrice.setBounds(750, 100, 150, 25);
        txtTotalPrice.setEditable(false);
        add(txtTotalPrice);


        btnAddToCart = new JButton("➕ Add to Cart");
        btnAddToCart.setBounds(450, 150, 150, 30);
        add(btnAddToCart);

        btnConfirmSale = new JButton("✅ Confirm Sale");
        btnConfirmSale.setBounds(620, 150, 150, 30);
        add(btnConfirmSale);

        cartTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Company", "Unit Price", "Qty", "Total"}, 0));
        JScrollPane scrollCart = new JScrollPane(cartTable);
        scrollCart.setBounds(320, 200, 600, 200);
        add(scrollCart);

        lblFinalTotalPrice = new JLabel("Total: Rs 0.00");
        lblFinalTotalPrice.setBounds(750, 420, 200, 30);
        lblFinalTotalPrice.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblFinalTotalPrice);

        // Events
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                medicineName(txtSearch.getText());
            }
        });

        medicinesTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = medicinesTable.getSelectedRow();
                if (index != -1) {
                    String idName = medicinesTable.getValueAt(index, 0).toString();
                    String[] parts = idName.split(" - ");
                    loadMedicineDetails(parts[0]);
                }
            }
        });


        txtNoOfUnits.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                calculateTotal();
            }
        });

        btnAddToCart.addActionListener(e -> addToCart());
        btnConfirmSale.addActionListener(e -> confirmSale());
    }

    //contacte le serveur pour chercher des médicaments correpondants
    private void medicineName(String keyword) {
        DefaultTableModel model = (DefaultTableModel) medicinesTable.getModel();
        model.setRowCount(0);

        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            pw.println("searchMedicine");
            pw.println(keyword);

            String line;
            while (!(line = br.readLine()).equals("end")) {
                String[] parts = line.split("::");
                if (parts.length >= 2) {
                    model.addRow(new Object[]{parts[0] + " - " + parts[1]});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Search Error: " + ex.getMessage());
        }
    }

    //charge les details du médicament séléctionné
    private void loadMedicineDetails(String id) {
        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            pw.println("searchMedicineById");
            pw.println(id);

            String response = br.readLine();
            if (response != null && !response.equals("not_found")) {
                String[] data = response.split("::");
                txtUniqueId.setText(id);
                txtName.setText(data[0]);
                txtCompanyName.setText(data[1]);
                txtPricePerUnit.setText(data[3]);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loading Error: " + ex.getMessage());
        }
    }

    //multiplie le prix par la quantité saisie
    private void calculateTotal() {
        try {
            String qtyStr = txtNoOfUnits.getText().trim();
            String priceStr = txtPricePerUnit.getText().trim().replace(',', '.');
            if (!qtyStr.isEmpty() && !priceStr.isEmpty() && qtyStr.matches(numberPattern)) {
                int qty = Integer.parseInt(qtyStr);
                double price = Double.parseDouble(priceStr);
                double total = qty * price;
                txtTotalPrice.setText(String.format("%.2f", total));
            } else {
                txtTotalPrice.setText("");
            }
        } catch (Exception ignored) {}
    }

    //ajoute le médicament sélectionné dans le tableau du panier
    private void addToCart() {
        String qtyStr = txtNoOfUnits.getText().trim();
        String totalStr = txtTotalPrice.getText().trim().replace(',', '.');
        String id = txtUniqueId.getText().trim();
        String name = txtName.getText().trim();
        String company = txtCompanyName.getText().trim();
        String priceStr = txtPricePerUnit.getText().trim().replace(',', '.');

        if (qtyStr.isEmpty() || id.isEmpty() || priceStr.isEmpty() || totalStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(id)) {
                JOptionPane.showMessageDialog(this, "Already in cart.");
                return;
            }
        }

        model.addRow(new Object[]{id, name, company, priceStr, qtyStr, totalStr});
        finalTotalPrice += Double.parseDouble(totalStr);
        lblFinalTotalPrice.setText(String.format("Total: Rs %.2f", finalTotalPrice));
        clearForm();
    }

    private void clearForm() {
        txtUniqueId.setText("");
        txtName.setText("");
        txtCompanyName.setText("");
        txtPricePerUnit.setText("");
        txtNoOfUnits.setText("");
        txtTotalPrice.setText("");
    }

    //envoie la vente au serveur(addbill) et génére un pdf de la facture localement
    private void confirmSale() {
        if (cartTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty! Add medicines first.");
            return;
        }

        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("addBill");

            String billId = "BILL" + System.currentTimeMillis();
            pw.println(billId);
            pw.println(finalTotalPrice);
            pw.println(username);

            String response = br.readLine();
            if ("success".equals(response)) {
                generateBillPdf(billId);
                JOptionPane.showMessageDialog(this, "✅ Sale confirmed! PDF generated.");

            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to confirm sale.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error confirming sale: " + e.getMessage());
        }
    }

    //méthode pour generer pdf pour chaque facture
    // 🔄 Version améliorée pour un affichage plus joli
    private void generateBillPdf(String billId) {
        try {
            Document document = new Document();
            String path = PharmacyUtils.billPath + billId + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // 🧾 Titre centré en gras
            Paragraph header = new Paragraph("💊 Pharmacy Bill");
            header.setAlignment(Paragraph.ALIGN_CENTER);
            header.setSpacingAfter(10);
            document.add(header);

            // 📌 Informations sur la facture
            document.add(new Paragraph("Bill ID: " + billId));
            document.add(new Paragraph("Generated By: " + username));
            document.add(new Paragraph("Date: " + java.time.LocalDate.now()));
            document.add(new Paragraph(" ")); // ligne vide

            // 🧾 Tableau simplifié des articles
            document.add(new Paragraph("Items:"));
            document.add(new Paragraph("----------------------------------------"));
            for (int i = 0; i < cartTable.getRowCount(); i++) {
                String medName = cartTable.getValueAt(i, 1).toString();
                String qty = cartTable.getValueAt(i, 4).toString();
                String total = cartTable.getValueAt(i, 5).toString().replace(",", ".");
                document.add(new Paragraph(String.format("• %-20s Qty: %-5s  | Total: Rs %s", medName, qty, total)));
            }
            document.add(new Paragraph("----------------------------------------"));

            // 💰 Montant total bien mis en évidence
            Paragraph total = new Paragraph(String.format("Total Paid: Rs %.2f", finalTotalPrice));
            total.setSpacingBefore(10);
            total.setSpacingAfter(10);
            total.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(total);

            // ✨ Remerciement
            Paragraph footer = new Paragraph("🙏 Thank you for your purchase!");
            footer.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage());
        }
    }

}
