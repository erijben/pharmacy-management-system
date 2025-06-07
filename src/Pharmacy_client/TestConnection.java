package Pharmacy_client;

import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        Connection con = ConnectionProvider.getCon();
        if (con != null) {
            System.out.println("✅ Connexion réussie !");
        } else {
            System.out.println("❌ Connexion échouée.");
        }
    }
}
