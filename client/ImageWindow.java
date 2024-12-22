package client;
// Source code is decompiled from a .class file using FernFlower decompiler.
import java.io.ByteArrayInputStream;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class ImageWindow {
   private Stage imageStage;

   public ImageWindow(byte[] var1) {
      try {
         ByteArrayInputStream var2 = new ByteArrayInputStream(var1);
         Image var3 = new Image(var2);
         ImageView var4 = new ImageView(var3);
         VBox var5 = new VBox();
         var5.getChildren().add(var4);
         Scene var6 = new Scene(var5);
         this.imageStage = new Stage();
         this.imageStage.setTitle("Image Viewer");
         this.imageStage.setScene(var6);
         this.imageStage.show();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }

   public Stage getImageStage() {
      return this.imageStage;
   }
}
/*class ImageWindow {
    private Stage imageStage;

    public ImageWindow(byte[] imageData) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            Image image = new Image(inputStream);
            ImageView imageView = new ImageView(image);

            VBox imageRoot = new VBox();
            imageRoot.getChildren().add(imageView);

            Scene imageScene = new Scene(imageRoot);
            imageStage = new Stage();
            imageStage.setTitle("Image Viewer");
            imageStage.setScene(imageScene);
            imageStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Stage getImageStage() {
        return imageStage;
    }
} */