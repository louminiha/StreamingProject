package client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;
import configuration.*;

public class StreamingVideoClient extends Application {

    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage primaryStage;
    private Stage videoStage;
    //
    VideoWindow videoWindow;
    private Socket socket;
    private Slider progressBar = new Slider();
    private Service<Void> videoService;
    private ObjectOutputStream out;
    private ObjectInputStream in;// zavatra no-selectionena, ilay azo a partir anle serveur
    private BufferedOutputStream tempFileOutputStream;
    private Path tempVideoFile;
    static int port;
    static String host;
    private ExecutorService executorService;
    ListView<String> listView = new ListView<>();
    private ListView<String> listViewPlaylists = new ListView<>();
        private Map<String, List<String>> playlistChansons = new HashMap<>();
        private boolean isAutoPlayEnabled = false;

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            port = Configuration.readPortFromFile("configuration\\config.txt");
            host = Configuration.readHostFromFile("configuration\\config.txt");
            socket = new Socket(host, port); // connexion avec le serveur
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            CheckBox autoPlayCheckBox = new CheckBox("Activer la lecture automatique");
         autoPlayCheckBox.setOnAction(e -> toggleAutoPlay(autoPlayCheckBox.isSelected())); 
            System.out.println("Connecte !");
           
            //out.writeObject("envoi les videos");
            // configuration du menu
            TabPane tabPane = new TabPane();

            Tab tabChansons = new Tab("Liste des chansons");
            Tab tabPlaylists = new Tab("Playlists");

            tabChansons.setClosable(false);
            tabPlaylists.setClosable(false);
            @SuppressWarnings("unchecked")
            List<String> videoList = (List<String>) in.readObject();
            setupChansonsTab(tabChansons, videoList);
            setupPlaylistsTab(tabPlaylists);
            tabPane.getTabs().addAll(tabChansons, tabPlaylists);
            System.out.println("Liste des vidéos reçue du serveur.");

            ObservableList<String> observableList = FXCollections.observableArrayList(videoList);
            listView.setItems(observableList);
            // alaina ilay liste avy am serveur de apetraka ao anaty listeView
        
            listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (videoService != null) {
                        videoService.cancel(); // Annuler le service actuel s'il existe
                    }
                    try {
                       
                        System.out.println(newValue);
                        this.out.writeObject(newValue);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    new Thread(() -> listenForServerResponses(newValue)).start();
                    // createVideoService(newValue); // Créer un nouveau service pour le nouveau
                    // fichier sélectionné
                }
            });

            VBox root = new VBox();
            root.setSpacing(10);
            root.setPadding(new Insets(10));
            
            Button refreshButton = new Button("Rafraîchir");
            refreshButton.setOnAction(event -> restartApplication());
           // root.getChildren().addAll(listView, refreshButton);
            root.getChildren().addAll(tabPane, listView, refreshButton,autoPlayCheckBox);

            Scene scene = new Scene(root, 307, 250);
            primaryStage.setTitle("Video Player");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void listenForServerResponses(String fileName) {
        try {
            while (true) {
                Object response = in.readObject();
                // System.out.println(response);
                if ("start".equals(response)) {
                    // Create a temporary file for the video
                    if (fileName.endsWith(".mp4")) {
                        tempVideoFile = Files.createTempFile("streaming", ".mp4");
                    } else if (fileName.endsWith(".mp3")) {
                        tempVideoFile = Files.createTempFile("streaming", ".mp3");
                    }
                    if (tempFileOutputStream != null) {
                        tempFileOutputStream.close();
                    }
                    tempFileOutputStream = new BufferedOutputStream(new FileOutputStream(tempVideoFile.toFile()));
    
                    System.out.println(fileName);
                    // if (fileName.endsWith(".mp4")) {
                    new Thread(() -> openVideoWindow()).start();

                } else if ("envoi".equals(response)) {
                    try {
                        int bytesRead = in.readInt();
                        System.out.println(bytesRead + " bytes");
                        System.out.println("size " + Files.size(tempVideoFile));
                        byte[] buffer = new byte[bytesRead];
                        in.readFully(buffer);
                        if (fileName.endsWith(".mp4")) {
                            if (this.videoWindow!= null && this.videoWindow.mediaPlayer != null
                                    && this.videoWindow.mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                                System.out.println("closer");
                                tempFileOutputStream.close();
                                tempFileOutputStream = null;
                                // out.writeObject("stop");
                                break;
                            }
                            
                        }
                        if (tempFileOutputStream != null) {
                            tempFileOutputStream.write(buffer);
                            tempFileOutputStream.flush();
                           
                            // refresh le mediaPlayer
                            // new Thread(() -> refreshMediaPlayer()).start();
                            // Platform.runLater(() -> refreshMediaPlayer());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if ("fin".equals(response)) {
                    // Close the file stream when video is fully received
                    // refreshMediaPlayer();
                    if (tempFileOutputStream != null) {
                        System.out.println("fin d'envoi ");
                        tempFileOutputStream.close();
                        tempFileOutputStream = null;
                        // if(isAutoPlayEnabled==true)
                        // {
                        //     out.writeObject("lecture automatique ON");
                        // }else {
                        //     out.writeObject("lecture automatique OFF");
                        // }
                        //  restartApplication();
                    }
                     break;
                } else if ("VIDEO_ERROR".equals(response)) {
                    Platform.runLater(() -> showErrorDialog("Streaming Error", "Could not stream the video."));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("-- "+e.getMessage());
            System.out.println(socket.isClosed());
           // Platform.runLater(() -> showErrorDialog("Connection Lost", "Lost connection to the server."));
        }
    }
    private void toggleAutoPlay(boolean isEnabled) {
        isAutoPlayEnabled = isEnabled;
        System.out.println("Lecture automatique activée : " + isAutoPlayEnabled);
    }

    private void openVideoWindow() {
        this.videoWindow = new VideoWindow(tempVideoFile, primaryStage);
        this.videoWindow.createVideoWindow();
    }

    // Méthode pour redémarrer l'application
    private void restartApplication() {
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                new StreamingVideoClient().start(stage);
                primaryStage.close(); // Ferme la fenêtre principale actuelle
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
    }
    private void showOptions(String chanson) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addToPlaylist = new MenuItem("Ajouter à une playlist");
        //afinc d'ouvrir la fenetre d'ajout ou creation de playlist 
        addToPlaylist.setOnAction(e -> openPlaylistWindow(chanson));

        contextMenu.getItems().add(addToPlaylist);
        contextMenu.show(listView, 300, 300); // Position ajustée
    }
    private void setupChansonsTab(Tab tabChansons,List<String> chansonListe) {
        listView.getItems().addAll(chansonListe);

        // Ajout du menu contextuel pour chaque chanson
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        Button optionsButton = new Button("...");
                        optionsButton.setOnAction(e -> showOptions(item));
                        setGraphic(optionsButton);
                    }
                }
            };
            return cell;
        });

        tabChansons.setContent(listView);
    }
    // Configuration de l'onglet des playlists
    private void setupPlaylistsTab(Tab tabPlaylists) {
        listViewPlaylists.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedPlaylist = listViewPlaylists.getSelectionModel().getSelectedItem();
                if (selectedPlaylist != null) {
                    //lancer la playlist selectionnee
                    openPlaylistDetailsWindow(selectedPlaylist);
                }
            }
        });

        tabPlaylists.setContent(listViewPlaylists);
    }
     private void openPlaylistWindow(String chanson) {
        Stage stage = new Stage();
        VBox vbox = new VBox(10);

        // ListView des playlists existantes
        ListView<String> playlistsView = new ListView<>();
        playlistsView.getItems().addAll(listViewPlaylists.getItems()); // Copier les playlists existantes

        // Bouton pour créer une nouvelle playlist
        Button createPlaylistButton = new Button("Créer une nouvelle playlist");
        //fenetre de creation de playlist
        createPlaylistButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Créer une nouvelle playlist");
            dialog.setContentText("Nom de la playlist:");
            dialog.showAndWait().ifPresent(playlistName -> {
                listViewPlaylists.getItems().add(playlistName);
                playlistsView.getItems().add(playlistName);
                //ajouter une playlist 
                playlistChansons.put(playlistName, new ArrayList<>());
            });
        });

        // Bouton pour ajouter à une playlist
        Button addButton = new Button("Ajouter à la playlist");
        addButton.setOnAction(e -> {
            String selectedPlaylist = playlistsView.getSelectionModel().getSelectedItem();
            if (selectedPlaylist != null) {
                playlistChansons.get(selectedPlaylist).add(chanson);
                System.out.println("Ajouté " + chanson + " à la playlist " + selectedPlaylist);
                stage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une playlist !");
                alert.showAndWait();
            }
        });
        //ajouter les conteneurs : listeDePlaylist, bouttondeCreationPlaylist, AjoutPlaylistBoutton
        vbox.getChildren().addAll(playlistsView, createPlaylistButton, addButton);

        Scene scene = new Scene(vbox, 300, 200);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ajouter à une playlist");
        stage.show();
    }
    
    private void openPlaylistDetailsWindow(String playlistName) {
        Stage stage = new Stage();
        VBox vbox = new VBox(10);

        Label titleLabel = new Label("Chansons dans la playlist : " + playlistName);

        ListView<String> chansonsView = new ListView<>();
        chansonsView.getItems().addAll(playlistChansons.getOrDefault(playlistName, new ArrayList<>()));

        // Button playButton = new Button("Lancer la playlist");
        // playButton.setOnAction(e -> {
        //     System.out.println("Lecture de la playlist : " + playlistName);
        //     chansonsView.getItems().forEach(chanson -> System.out.println("Lecture : " + chanson));
        //     stage.close();
        // });
        chansonsView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (videoService != null) {
                    videoService.cancel(); // Annuler le service actuel s'il existe
                }
                try {
                    //out = new ObjectOutputStream(socket.getOutputStream());
                    System.out.println(newValue);
                    this.out.writeObject(newValue);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                new Thread(() -> listenForServerResponses(newValue)).start();
                // createVideoService(newValue); // Créer un nouveau service pour le nouveau
                // fichier sélectionné
            }
        });

        vbox.getChildren().addAll(titleLabel, chansonsView);

        Scene scene = new Scene(vbox, 300, 300);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Détails de la playlist");
        stage.show();
    }
    private void displayImage(byte[] imageData) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            Image image = new Image(inputStream);
            ImageView imageView = new ImageView(image);

            // Créez une nouvelle fenêtre pour afficher l'image
            Stage imageStage = new Stage();
            imageStage.setTitle("Image Viewer");
            imageStage.setScene(new Scene(new VBox(imageView), 400, 300));
            imageStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
