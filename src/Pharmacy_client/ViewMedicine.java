package Pharmacy_client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;

public class ViewMedicine extends JPanel {
    private JTable table;
    private DefaultTableModel model;

    public ViewMedicine() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("Click on a row to delete a medicine", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(infoLabel, BorderLayout.NORTH);

        model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[]{"ID", "Medicine ID", "Name", "Company Name", "Quantity", "Price"});
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.setGridColor(Color.LIGHT_GRAY);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        add(scrollPane, BorderLayout.CENTER);

        // appel de loadMedicines() pour afficher tous les médicaments

        loadMedicines();

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int index = table.getSelectedRow();
                TableModel model = table.getModel();
                String id = model.getValueAt(index, 0).toString();

                int a = JOptionPane.showConfirmDialog(null, "Do you want to delete this medicine?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (a == 0) {
                    deleteMedicine(id);
                }
            }
        });
    }

    private void loadMedicines() {
        model.setRowCount(0);
        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("viewMedicines");

            String line;
            while (!(line = br.readLine()).equals("end")) {
                String[] data = line.split("::");
                if (data.length == 6) {
                    model.addRow(data);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur chargement des médicaments : " + e.getMessage());
        }
    }

    private void deleteMedicine(String id) {
        try (Socket socket = new Socket("127.0.0.1", 9000);
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("deleteMedicine");
            pw.println(id);

            String response = br.readLine();
            if ("success".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, "✅ Medicine deleted successfully.");
                reloadTable();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Failed to delete medicine.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void reloadTable() {
        loadMedicines();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("View Medicine (Socket Version)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ViewMedicine());
        frame.setSize(900, 450);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
