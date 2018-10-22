package apz.activemq;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.controller.ConnectionController;
import apz.activemq.controller.NavigationController;
import apz.activemq.injection.Injector;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static apz.activemq.Configuration.*;
import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.*;
import static java.lang.Runtime.getRuntime;

public class ActiveMQTool extends Application {

    public static void main(final String... args) {
        getRuntime().addShutdownHook(new Thread(Injector::clearRegistry));
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);
        final SimpleSnackbar snackbar = new SimpleSnackbar(stackPane);

        configureHostServices(this);
        configureJmxClient();
        configureScheduledExecutorService();
        configureObjectMapper();
        configureMessageToStringConverter(get("objectMapper", ObjectMapper.class));
        configureCamelContext();
        configureSerializationDataFormat(get("objectMapper", ObjectMapper.class));
        register("snackbar", snackbar);

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

    @Override
    public void stop() {
        Platform.exit();
        shutdownExecutorServices();
        clearRegistry();
    }
}
