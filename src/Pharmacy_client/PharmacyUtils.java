package Pharmacy_client;

import java.io.File;

public class PharmacyUtils {
    public static String billPath = "C:\\PharmacyBills\\";


    static {
        // Vérifier si le dossier existe, sinon le créer
        File folder = new File(billPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}
