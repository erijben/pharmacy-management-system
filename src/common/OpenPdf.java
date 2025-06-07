package common;

import Pharmacy_client.PharmacyUtils;

import javax.swing.*;
import java.io.File;

//fichier qui ouvre un fichier pdf
public class OpenPdf {
    //Méthode openById(String id)
    public static void openById(String id) {
        try {
            //Cherche un fichier PDF basé sur PharmacyUtils.billPath et l id fourni
            File file = new File(PharmacyUtils.billPath + id + ".pdf");
//Si le fichier existe, il est ouvert avec le programme PDF par défaut de l'ordinateur
            if (file.exists()) {
                // Ouvre le fichier PDF avec l'application par défaut sous Windows
                Process p = Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + file.getAbsolutePath());
            } else {
                //Sinon, affiche une erreur.
                JOptionPane.showMessageDialog(null, "Fichier introuvable : " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de l'ouverture du fichier PDF :\n" + e);
        }
    }
}
