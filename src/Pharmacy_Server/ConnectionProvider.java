package Pharmacy_Server;

import java.sql.Connection;
import java.sql.DriverManager;

//class passerelle entre serveur et BD
public class ConnectionProvider {
    public static Connection getCon() { //methode statique
        try {
            //charger le driver jdbc
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/pharmacy?useSSL=false",
                    //utiliser le root sans mdp ici
                    "root",
                    "" // ← mot de passe si tu en as un
            );
        } catch (Exception e) {
            System.out.println("Erreur de connexion MySQL : " + e.getMessage());
            return null;
        }
    }
}
