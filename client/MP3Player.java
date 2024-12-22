// Source code is decompiled from a .class file using FernFlower decompiler.
package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

// class MP3Player {
//    private MediaPlayer mediaPlayer;
//    private Stage mp3Stage;
//    private Stage primaryStage;

//    public MP3Player(byte[] var1, Stage var2) {
//       this.primaryStage = var2;

//       try {
//          File var3 = File.createTempFile("temp", ".mp3");
//          FileOutputStream var4 = new FileOutputStream(var3);

//          try {
//             var4.write(var1);
//          } catch (Throwable var13) {
//             try {
//                var4.close();
//             } catch (Throwable var12) {
//                var13.addSuppressed(var12);
//             }

//             throw var13;
//          }

//          var4.close();
//          Media var15 = new Media(var3.toURI().toString());
//          this.mediaPlayer = new MediaPlayer(var15);
//          VBox var5 = new VBox();
//          MediaView var6 = new MediaView(this.mediaPlayer);
//          var6.setFitWidth(300.0);
//          var6.setFitHeight(150.0);
//          Button var7 = new Button("Play");
//          var7.setOnAction((var1x) -> {
//             this.mediaPlayer.play();
//          });
//          Button var8 = new Button("Pause");
//          var8.setOnAction((var1x) -> {
//             this.mediaPlayer.pause();
//          });
//          Button var9 = new Button("Stop");
//          var9.setOnAction((var1x) -> {
//             this.stopMP3();
//             this.mp3Stage.close();
//          });
//          HBox var10 = new HBox(new Node[]{var7, var8, var9});
//          var10.setSpacing(10.0);
//          var5.getChildren().addAll(new Node[]{var6, var10});
//          Scene var11 = new Scene(var5, 300.0, 200.0);
//          this.mp3Stage = new Stage();
//          this.mp3Stage.setTitle("MP3 Player");
//          this.mp3Stage.setScene(var11);
//          this.mp3Stage.show();
//       } catch (IOException var14) {
//          var14.printStackTrace();
//       }

//    }

//    private void stopMP3() {
//       try {
//          if (this.mediaPlayer != null) {
//             this.mediaPlayer.stop();
//             this.mp3Stage.close();
//             this.primaryStage.show();
//          }
//       } catch (Exception var2) {
//          var2.printStackTrace();
//       }

//    }
// }
class MP3Player {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage mp3Stage;
    private Stage primaryStage;
    Path tempFile;

    public MP3Player(Path tempFile, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.tempFile = tempFile;
    }

    public void createMp3()  {
        Service<Void> Mp3Service = new Service<>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() throws Exception {

                        while (tempFile == null ||
                                Files.size(tempFile) < 1024 * 1024) { // Wait until at least 1MB is downloaded
                            Thread.sleep(100);
                        }
                        // Construire l'interface MP3 sur le FX Application Thread
                        Platform.runLater(() -> {
                        Media media = new Media(tempFile.toUri().toString());
                        mediaPlayer = new MediaPlayer(media);
                        mediaView = new MediaView(mediaPlayer);
                        mediaView.setFitWidth(300);
                        mediaView.setFitHeight(150);
                        mediaPlayer.setOnReady(() -> mediaPlayer.play());
                        System.out.println("construction mp3");
                        buildMp3UI();
                        });
                        return null;
                    }
                };
            };
        };
        Mp3Service.start();
    }

    private void buildMp3UI() {
        VBox mp3Root = new VBox();
       

        Button playButton = new Button("Play");
        playButton.setOnAction(event -> mediaPlayer.play());

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(event -> mediaPlayer.pause());

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(event -> {
            stopMP3();
            mp3Stage.close();
        });
        // if (mediaPlayer != null) {
        // mediaPlayer.play();
        // }
        HBox controls = new HBox(playButton, pauseButton, stopButton);
        controls.setSpacing(10);

        mp3Root.getChildren().addAll(mediaView, controls);

        Scene mp3Scene = new Scene(mp3Root, 300, 200);
        mp3Stage = new Stage();
        mp3Stage.setTitle("MP3 Player");
        mp3Stage.setScene(mp3Scene);
        mp3Stage.show();

    }

    // VBox mp3Root = new VBox();
    // MediaView mediaView = new MediaView(mediaPlayer);
    // mediaView.setFitWidth(300);
    // mediaView.setFitHeight(150);

    // Button playButton = new Button("Play");
    // playButton.setOnAction(event -> mediaPlayer.play());

    // Button pauseButton = new Button("Pause");
    // pauseButton.setOnAction(event -> mediaPlayer.pause());
    // if (mediaPlayer != null) {
    // mediaPlayer.play();
    // }
    // Button stopButton = new Button("Stop");
    // stopButton.setOnAction(event -> {
    // stopMP3(); // Appel de la méthode stopMP3() pour arrêter la lecture du
    // fichier MP3
    // mp3Stage.close(); // Fermer la fenêtre MP3
    // });

    // HBox controls = new HBox(playButton, pauseButton, stopButton);
    // controls.setSpacing(10);
    // if (mediaPlayer != null) {
    // mediaPlayer.play();
    // }
    // mp3Root.getChildren().addAll(mediaView, controls);

    // Scene mp3Scene = new Scene(mp3Root, 300, 200);
    // mp3Stage = new Stage();
    // mp3Stage.setTitle("MP3 Player");
    // mp3Stage.setScene(mp3Scene);
    // mp3Stage.show();
    // }

    private void stopMP3() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                primaryStage.show(); // Afficher à nouveau la fenêtre principale après la fermeture de la fenêtre MP3
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}