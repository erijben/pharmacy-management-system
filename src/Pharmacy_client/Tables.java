package Pharmacy_client;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Statement;

public class Tables {
    public static void main(String[] args) {
        try {
            Connection con = Pharmacy_client.ConnectionProvider.getCon();
            Statement st = con.createStatement();

            // Table Appuser
            st.executeUpdate("CREATE TABLE IF NOT EXISTS appuser (" +
                    "appuser_pk INT AUTO_INCREMENT PRIMARY KEY, " +
                    "userRole VARCHAR(200), " +
                    "name VARCHAR(200), " +
                    "dob VARCHAR(50), " +
                    "mobileNumber VARCHAR(50), " +
                    "email VARCHAR(200), " +
                    "username VARCHAR(200), " +
                    "password VARCHAR(50), " +
                    "address VARCHAR(200))");

            // Table Medicine
            st.executeUpdate("CREATE TABLE IF NOT EXISTS medicine (" +
                    "medicine_pk INT AUTO_INCREMENT PRIMARY KEY, " +
                    "uniqueId VARCHAR(200), " +
                    "name VARCHAR(200), " +
                    "companyName VARCHAR(200), " +
                    "quantity BIGINT, " +
                    "price BIGINT)");

            // Table Bill (corrigé)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bill (" +
                    "bill_pk INT AUTO_INCREMENT PRIMARY KEY, " +
                    "billId VARCHAR(200), " +
                    "billDate VARCHAR(50), " +
                    "totalPaid BIGINT, " +
                    "generatedBy VARCHAR(50))");

            JOptionPane.showMessageDialog(null, "✅ Tables created successfully!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ Error: " + e.getMessage());
        }
    }
}
