package Pharmacy_Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class ServerSocketHandler {

    public static void main(String[] args) {
        System.out.println("🔌 Serveur en écoute sur le port 9000...");

        //Le serveur démarre et écoute sur le port 9000
        try (ServerSocket serverSocket = new ServerSocket(9000)) {


            while (true) {
                Socket clientSocket = serverSocket.accept(); //accept() = bloquant → attend jusqu’à ce qu’un client frappe à la porte
                System.out.println("✅ Client connecté : " + clientSocket.getInetAddress());

                //Dès qu'un client arrive, on accepte la connexion et on démarre un nouveau Thread
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            System.out.println("❌ Erreur serveur : " + e.getMessage());
        }
    }

    //Elle s’occupe d'un seul client connecté.
    private static void handleClient(Socket socket) {
        try (
                //Préparer la communication
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); //br → pour lire ce que le client envoie (ex: "login")
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true) //pw → pour écrire une réponse au client
        ) {
            if (!br.ready()) return;
            //Lire la demande du client ( login, adduser,viewuser) pour savoir ce que veut le client
            String request = br.readLine();

            //Réagir selon la commande (selon la cmd on appelle une methode specifique)
            switch (request) {
                case "login" -> handleLogin(br, pw);
                case "addUser" -> handleAddUser(br, pw);
                case "checkUsername" -> handleCheckUsername(br, pw);
                case "viewUsers" -> handleViewUsers(pw);
                case "addMedicine" -> handleAddMedicine(br, pw);
                case "viewMedicines" -> handleViewMedicines(pw);         // 📌 Ajouté
                case "deleteMedicine" -> handleDeleteMedicine(br, pw);   // 📌 Ajouté
                case "searchMedicine" -> handleSearchMedicine(br, pw);
                case "searchMedicineById" -> handleSearchMedicineById(br, pw);
                case "updateMedicine" -> handleUpdateMedicine(br, pw);
                case "deleteUser" -> handleDeleteUser(br, pw);
                case "updateUser" -> handleUpdateUser(br, pw);
                case "addBill" -> handleAddBill(br, pw);
                case "viewBills" -> handleViewBills(pw);
                case "getProfile" -> handleGetProfile(br, pw);
                case "updateProfile" -> handleUpdateProfile(br, pw);
                case "countUsers" -> handleCountUsers(pw);
                case "countMedicines" -> handleCountMedicines(pw);
                default -> {
                    //Si la commande n'est pas reconnue → on envoie "unknown" au client.
                    pw.println("unknown");
                    System.out.println("❓ Requête inconnue : " + request);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Erreur traitement client : " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Erreur fermeture socket : " + e.getMessage());
            }
        }
    }

    // ==================== HANDLERS POUR CHAQUE COMMANDE ====================

    private static void handleLogin(BufferedReader br, PrintWriter pw) throws IOException {
        //On lit username et password.
        String username = br.readLine();
        String password = br.readLine();
        //On appelle isLoginValid() pour vérifier dans la base de données.
        if (isLoginValid(username, password)) {
            String role = getUserRole(username);
            pw.println("success");
            pw.println(role);
        } else {
            pw.println("fail");
        }
    }
    private static void handleGetProfile(BufferedReader br, PrintWriter pw) throws IOException {
        String username = br.readLine();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("SELECT name, mobileNumber, email, address FROM appuser WHERE username=?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String response = rs.getString("name") + "::" +
                        rs.getString("mobileNumber") + "::" +
                        rs.getString("email") + "::" +
                        rs.getString("address");
                pw.println(response);
            } else {
                pw.println("not_found");
            }
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleUpdateProfile(BufferedReader br, PrintWriter pw) throws IOException {
        String username = br.readLine();
        String name = br.readLine();
        String mobile = br.readLine();
        String email = br.readLine();
        String address = br.readLine();

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE appuser SET name=?, mobileNumber=?, email=?, address=? WHERE username=?"
            );
            ps.setString(1, name);
            ps.setString(2, mobile);
            ps.setString(3, email);
            ps.setString(4, address);
            ps.setString(5, username);
            int rows = ps.executeUpdate();
            pw.println(rows > 0 ? "success" : "fail");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleCountMedicines(PrintWriter pw) {
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();

            ResultSet rs1 = st.executeQuery("SELECT COUNT(*) FROM medicine");
            rs1.next();
            String totalMedicines = rs1.getString(1);

            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM bill");
            rs2.next();
            String totalBills = rs2.getString(1);

            ResultSet rs3 = st.executeQuery("SELECT SUM(quantity) FROM medicine");
            rs3.next();
            String totalStock = rs3.getString(1);

            pw.println(totalMedicines + "::" + totalBills + "::" + totalStock);
        } catch (Exception e) {
            pw.println("fail");
            e.printStackTrace();
        }
    }

    private static void handleCountUsers(PrintWriter pw) {
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();

            ResultSet rsTotal = st.executeQuery("SELECT COUNT(*) FROM appuser");
            rsTotal.next();
            int total = rsTotal.getInt(1);

            ResultSet rsPharmacists = st.executeQuery("SELECT COUNT(*) FROM appuser WHERE userRole='Pharmacist'");
            rsPharmacists.next();
            int pharmacists = rsPharmacists.getInt(1);

            ResultSet rsAdmins = st.executeQuery("SELECT COUNT(*) FROM appuser WHERE userRole='Admin'");
            rsAdmins.next();
            int admins = rsAdmins.getInt(1);

            pw.println(total + "::" + pharmacists + "::" + admins);
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleAddUser(BufferedReader br, PrintWriter pw) throws IOException {
        String role = br.readLine();
        String name = br.readLine();
        String dob = br.readLine();
        String mobile = br.readLine();
        String email = br.readLine();
        String username = br.readLine();
        String password = br.readLine();
        String address = br.readLine();

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO appuser (userRole, name, dob, mobileNumber, email, username, password, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, role);
            ps.setString(2, name);
            ps.setString(3, dob);
            ps.setString(4, mobile);
            ps.setString(5, email);
            ps.setString(6, username);
            ps.setString(7, password);
            ps.setString(8, address);
            ps.executeUpdate();
            pw.println("success");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleCheckUsername(BufferedReader br, PrintWriter pw) throws IOException {
        String username = br.readLine(); //Le serveur reçoit le nom d'utilisateur envoyé par le client
        try {
            //Le serveur se connecte à la base de données
            Connection con = ConnectionProvider.getCon();
            //requête SQL pour chercher un utilisateur avec ce username.
            PreparedStatement ps = con.prepareStatement("SELECT * FROM appuser WHERE username=?");
            //Il remplace ? par la vraie valeur du username reçu (ex: "erij123").
            ps.setString(1, username);
            //executer la requete
            ResultSet rs = ps.executeQuery();
            pw.println(rs.next() ? "exists" : "available");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleViewUsers(PrintWriter pw) {
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM appuser");

            while (rs.next()) {
                String row = rs.getString("appuser_pk") + "::" +
                        rs.getString("name") + "::" +
                        rs.getString("userRole") + "::" +
                        rs.getString("dob") + "::" +
                        rs.getString("mobileNumber") + "::" +
                        rs.getString("email") + "::" +
                        rs.getString("username") + "::" +
                        rs.getString("password") + "::" +
                        rs.getString("address");
                pw.println(row);
            }
            pw.println("end");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleAddMedicine(BufferedReader br, PrintWriter pw) throws IOException {
        String id = br.readLine();
        String name = br.readLine();
        String company = br.readLine();
        String qty = br.readLine();
        String price = br.readLine();

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO medicine (uniqueId, name, companyName, quantity, price) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, id);
            ps.setString(2, name);
            ps.setString(3, company);
            ps.setString(4, qty);
            ps.setString(5, price);
            ps.executeUpdate();
            pw.println("success");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleViewMedicines(PrintWriter pw) {
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM medicine");

            while (rs.next()) {
                String row = rs.getString("medicine_pk") + "::" +
                        rs.getString("uniqueId") + "::" +
                        rs.getString("name") + "::" +
                        rs.getString("companyName") + "::" +
                        rs.getString("quantity") + "::" +
                        rs.getString("price");
                pw.println(row);
            }
            pw.println("end");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleDeleteMedicine(BufferedReader br, PrintWriter pw) throws IOException {
        String id = br.readLine();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("DELETE FROM medicine WHERE medicine_pk=?");
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            pw.println(rows > 0 ? "success" : "fail");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleSearchMedicine(BufferedReader br, PrintWriter pw) throws IOException {
        String key = br.readLine();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM medicine WHERE name LIKE ? OR uniqueId LIKE ?"
            );
            ps.setString(1, key + "%");
            ps.setString(2, key + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String row = rs.getString("uniqueId") + "::" +
                        rs.getString("name") + "::" +
                        rs.getString("companyName") + "::" +
                        rs.getString("price") + "::" +
                        rs.getString("quantity");
                pw.println(row);
            }
            pw.println("end");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleSearchMedicineById(BufferedReader br, PrintWriter pw) throws IOException {
        String id = br.readLine();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT name, companyName, quantity, price FROM medicine WHERE uniqueId=?"
            );
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String response = rs.getString("name") + "::" +
                        rs.getString("companyName") + "::" +
                        rs.getString("quantity") + "::" +
                        rs.getString("price");
                pw.println(response);
            } else {
                pw.println("not_found");
            }
        } catch (SQLException e) {
            pw.println("fail");
        }
    }
    private static void handleViewBills(PrintWriter pw) {
        try {
            Connection con = ConnectionProvider.getCon();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM bill");

            while (rs.next()) {
                String row = rs.getString("billId") + "::" +
                        rs.getString("billDate") + "::" +
                        rs.getString("totalPaid") + "::" +
                        rs.getString("generatedBy");
                pw.println(row);
            }
            pw.println("end");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }


    private static void handleUpdateMedicine(BufferedReader br, PrintWriter pw) throws IOException {
        String id = br.readLine();
        String name = br.readLine();
        String company = br.readLine();
        String quantity = br.readLine();
        String price = br.readLine();

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE medicine SET name=?, companyName=?, quantity=?, price=? WHERE uniqueId=?"
            );
            ps.setString(1, name);
            ps.setString(2, company);
            ps.setString(3, quantity);
            ps.setString(4, price);
            ps.setString(5, id);
            int rows = ps.executeUpdate();
            pw.println(rows > 0 ? "success" : "fail");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }
    private static void handleAddBill(BufferedReader br, PrintWriter pw) throws IOException {
        String billId = br.readLine();
        String totalPaid = br.readLine();
        String generatedBy = br.readLine();

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bill (billId, billDate, totalPaid, generatedBy) VALUES (?, CURRENT_DATE, ?, ?)"
            );
            ps.setString(1, billId);
            ps.setString(2, totalPaid);
            ps.setString(3, generatedBy);
            ps.executeUpdate();
            pw.println("success");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }


    private static void handleDeleteUser(BufferedReader br, PrintWriter pw) throws IOException {
        String id = br.readLine();
        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement("DELETE FROM appuser WHERE appuser_pk=?");
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            pw.println(rows > 0 ? "success" : "fail");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }

    private static void handleUpdateUser(BufferedReader br, PrintWriter pw) throws IOException {
        String username = br.readLine();
        String name = br.readLine();
        String dob = br.readLine();
        String mobile = br.readLine();
        String email = br.readLine();
        String address = br.readLine();
        String role = br.readLine();

        try {
            Connection con = ConnectionProvider.getCon();
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE appuser SET name=?, dob=?, mobileNumber=?, email=?, address=?, userRole=? WHERE username=?"
            );
            ps.setString(1, name);
            ps.setString(2, dob);
            ps.setString(3, mobile);
            ps.setString(4, email);
            ps.setString(5, address);
            ps.setString(6, role);
            ps.setString(7, username);
            int rows = ps.executeUpdate();
            pw.println(rows > 0 ? "success" : "fail");
        } catch (SQLException e) {
            pw.println("fail");
        }
    }


    private static boolean isLoginValid(String username, String password) {
        try {
            Connection con = ConnectionProvider.getCon();
            String sql = "SELECT * FROM appuser WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private static String getUserRole(String username) {
        try {
            Connection con = ConnectionProvider.getCon();
            String sql = "SELECT userRole FROM appuser WHERE username=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("userRole") : "Unknown";
        } catch (SQLException e) {
            return "Unknown";
        }
    }
}
