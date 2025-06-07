package Pharmacy_client;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ViewUserPanel extends JPanel {
    private JTable userTable;
    private String username;

    public ViewUserPanel(String tempUsername) {
        this.username = tempUsername;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel("📋 Double-click a row to delete a user (except yourself)");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        add(infoLabel, BorderLayout.NORTH);

        userTable = new JTable();
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userTable.setRowHeight(28);
        userTable.setGridColor(Color.LIGHT_GRAY);
        userTable.setSelectionBackground(new Color(200, 230, 255));
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        ((DefaultTableCellRenderer) userTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(scrollPane, BorderLayout.CENTER);

        String[] columnNames = {
                "ID", "Name", "Role", "DOB", "Mobile", "Email", "Username", "Password", "Address"
        };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable.setModel(model);
        loadUsers(model);


        userTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    deleteUserOnClick();
                }
            }
        });
    }

    private void loadUsers(DefaultTableModel model) {
        try (
                Socket socket = new Socket("127.0.0.1", 9000);
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            pw.println("viewUsers");

            String line;
            while ((line = br.readLine()) != null && !line.equals("end")) {
                String[] data = line.split("::");
                model.addRow(data);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }

    private void deleteUserOnClick() {
        int index = userTable.getSelectedRow();
        if (index == -1) return;

        TableModel model = userTable.getModel();
        String id = model.getValueAt(index, 0).toString();
        String selectedUsername = model.getValueAt(index, 6).toString();

        if (username.equals(selectedUsername)) {
            JOptionPane.showMessageDialog(this, "❌ You can't delete your own account.");
        } else {
            int a = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this user?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);
            if (a == 0) {
                try (
                        Socket socket = new Socket("127.0.0.1", 9000);
                        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
                    pw.println("deleteUser");
                    pw.println(id);

                    String response = br.readLine();
                    if ("success".equals(response)) {
                        JOptionPane.showMessageDialog(this, "✅ User deleted successfully");
                        reloadTable();
                    } else {
                        JOptionPane.showMessageDialog(this, "❌ Deletion failed.");
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
                }
            }
        }
    }

    private void reloadTable() {
        DefaultTableModel model = (DefaultTableModel) userTable.getModel();
        model.setRowCount(0);
        loadUsers(model);
    }
}
