package serveur;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import configuration.Configuration;

public class StreamingVideoServer {
    private static List<String> videoList = new ArrayList<>();
    static int port;

    static {
        // Ajoutez ici toutes les videos que le serveur possede
        videoList.add("reponse.mp4");
        videoList.add("lapin.mp4");
        videoList.add("fanampiana2.mp3");
        videoList.add("boubla.jpg");
        videoList.add("coder2.mp4");
    }

    public static void main(String[] args) throws IOException {
        Configuration c = new Configuration();
        port = Configuration.readPortFromFile("configuration\\config.txt");
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Attente du client sur le port " + port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new VideoServerThread(clientSocket)).start();
            // ca start le lancement de la fenetre
        }
    }

    public static List<String> getVideoList() {
        return videoList;
    }

}

class VideoServerThread implements Runnable {
    private Socket clientSocket;
    public VideoServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

            // Envoyer la liste des fichiers au client
            System.out.println("liste video envoye");
            out.writeObject(StreamingVideoServer.getVideoList());
            out.flush();

             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            while (true) {
                String selectedFile = (String) in.readObject();

                // Charger et envoyer le fichier sélectionné
                File file = new File("D:/S3/ProjetStreaming2/videos/" + selectedFile);
                if (!file.exists()) {
                    out.writeObject("Erreur : Fichier introuvable.");
                    return;
                }
                try {
                    System.out.println("Demande du client en cours ...");
                    streamVideoToClient(out, selectedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void streamVideoToClient(ObjectOutputStream out, String nomFichier) throws ClassNotFoundException {

        String filePath = "D:/S3/ProjetStreaming2/videos/" + nomFichier + ""; // Chemin du fichier a streamer
        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(filePath))) {
            out.writeObject("start");
            System.out.println("Video commence ");
            // Stream video in chunks
            byte[] buffer = new byte[1024];
            int bytesRead;
            int donnee_envoye = 0;
            int envoi = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                //
                out.writeObject("envoi");
                donnee_envoye += fileInputStream.available();
                System.out.println("-- > " + bytesRead);
                out.writeInt(bytesRead);
                out.write(buffer, 0, bytesRead);
                envoi++;
            }
            System.out.println(bytesRead);
            // fin stream
            out.writeObject("fin");

            System.out.println("Fin de l'envoi " + nomFichier);
            out.reset();
        } catch (IOException e) {
            try {
                out.writeObject("erreur");
                out.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
    }

}
