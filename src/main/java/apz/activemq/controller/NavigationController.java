package apz.activemq.controller;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static apz.activemq.controller.ControllerFactory.newInstance;

public class NavigationController implements Initializable {

    @FXML
    public AnchorPane root;

    @FXML
    private AnchorPane container;

    private final BrokerController brokerController;
    private final QueuesController queuesController;
    private final InfoController infoController;

    public NavigationController() {
        brokerController = newInstance(BrokerController.class);
        queuesController = newInstance(QueuesController.class);
        infoController = newInstance(InfoController.class);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
    }

    @FXML
    public void showBrokerView(final @Nullable ActionEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        container.getChildren().clear();
        container.getChildren().add(brokerController.root);
        brokerController.refresh(null);
    }

    @FXML
    public void showQueuesView(final @Nullable ActionEvent event) {
        Optional.ofNullable(event).ifPresent(Event::consume);
        container.getChildren().clear();
        container.getChildren().add(queuesController.root);
    }

    @FXML
    public void showInfoView(final @Nullable ActionEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        container.getChildren().clear();
        container.getChildren().add(infoController.root);
    }
}
