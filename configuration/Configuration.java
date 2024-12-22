package configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Configuration {
     public static int readPortFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        
        // Lire chaque ligne du fichier
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("port :")) {
                // Extraire et retourner le numéro de port
                return Integer.parseInt(line.split(":")[1].trim());
            }
        }
        reader.close();
        throw new IOException("Port non trouvé dans le fichier de configuration");
    }
    public static String readHostFromFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        
        // Lire chaque ligne du fichier
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("host :")) {
                // Extraire et retourner le numéro de port
                return line.split(":")[1].trim();
            }
        }
        reader.close();
        throw new IOException("Port non trouvé dans le fichier de configuration");
    }
}
