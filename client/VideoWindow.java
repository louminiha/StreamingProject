package client;

import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.application.Platform;
import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.application.Platform;

public class VideoWindow {
     MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage videoStage;
    private Stage primaryStage;
    Path tempFile;

    public VideoWindow(Path tempFile, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.tempFile = tempFile;
        // createVideoWindow();
    }

    public void createVideoWindow() {
        Service<Void> videoService = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        // try {
                        // File tempFile = File.createTempFile("video", ".mp4");
                        // io video.mp4 io no atao mis-a jour tsikelikely
                        // File tempFile = new File("video.mp4");
                        // System.out.println("temp file name " + tempFile.getName());
                        // try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        // fos.write(videoData);
                        // }
                        while (tempFile == null ||
                                Files.size(tempFile) < 1024 * 1024) { // Wait until at least 1MB is downloaded
                            Thread.sleep(100);
                        }
                        Platform.runLater(() -> {
                            System.out.println("shdncfskjlc ----------------- ");
                            System.out.println("Chemin du fichier temporaire : " + tempFile.toAbsolutePath());
                            Media media = new Media(tempFile.toUri().toString());
                            mediaPlayer = new MediaPlayer(media);
                            mediaView = new MediaView(mediaPlayer);
                            mediaView.setFitWidth(300);
                            mediaView.setFitHeight(150);
                            mediaPlayer.setOnEndOfMedia(() -> {
                                System.out.println("La vidéo est terminée !");
                                mediaPlayer.stop();
                                videoStage.close(); // Fermer la fenêtre principale
                            });

                            createVideoStage();
                        });

                        // } catch (IOException e) {
                        // e.printStackTrace();
                        // }
                        return null;
                    }
                };
            }
        };

        videoService.start();
    }

    private void createVideoStage() {
        if (videoStage != null && videoStage.isShowing()) {
            // Si la fenêtre est déjà ouverte, ne rien faire
            return;
        }
        VBox videoRoot = new VBox();
        videoRoot.getChildren().add(mediaView);

        HBox controls = new HBox();
        controls.setSpacing(10);

        Button playButton = new Button("Play");
        playButton.setOnAction(event -> mediaPlayer.play());
        controls.getChildren().add(playButton);

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> mediaPlayer.pause());
        controls.getChildren().add(pauseButton);

        Slider progressBar = new Slider();
        progressBar.setMin(0);
        progressBar.setMax(100);
        progressBar.setValue(0);
        // progressBar.valueProperty().addListener((observable, oldValue, newValue) ->
        // mediaPlayer.seek(mediaPlayer.getTotalDuration().multiply(newValue.doubleValue()
        // / 100)));

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(event -> {
            stopVideo();
            videoStage.close();
        });
        controls.getChildren().add(stopButton);

        videoRoot.getChildren().addAll(controls, progressBar);

        Scene videoScene = new Scene(videoRoot, 300, 250);
        videoStage = new Stage();
        videoStage.setTitle("Video Player");
        videoStage.setScene(videoScene);
        videoStage.show();
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
        // Mettre à jour la barre de progression à chaque changement de position dans la
        // vidéo
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressBar.isValueChanging()) {
                // Calculer le pourcentage de la vidéo déjà lue par seconde
                double progress = newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds() * 100;
                progressBar.setValue(progress);
            }
        });
    }
    private void stopVideo() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
