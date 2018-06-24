package apz.activemq;

import apz.activemq.controller.ConnectionController;
import apz.activemq.controller.NavigationController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static apz.activemq.Configuration.configureHostServices;
import static apz.activemq.Configuration.configureJmxClient;
import static apz.activemq.controller.ControllerFactory.newInstance;

public class ActiveMQTool extends Application {

    public static void main(final String... args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        configureHostServices(this);
        configureJmxClient();

        final NavigationController navigationController = newInstance(NavigationController.class);
        final ConnectionController connectionController = newInstance(ConnectionController.class);

        stackPane.getChildren().add(navigationController.root);
        connectionController.setOnConnected(event -> navigationController.showBrokerView(null));
        connectionController.show(stackPane);

        stage.setTitle("ActiveMQ Tool");
        stage.getIcons().add(new Image("img/activemq-title-icon.png"));
        stage.setScene(scene);
        stage.show();
    }
}
