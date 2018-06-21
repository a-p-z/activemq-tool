package apz.activemq;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ActiveMQTool extends Application {

    public static void main(final String... args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        stage.setTitle("ActiveMQ Tool");
        stage.getIcons().add(new Image("img/activemq-title-icon.png"));
        stage.setScene(scene);
        stage.show();
    }
}
