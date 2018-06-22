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

public class NavigationController implements Initializable {

    @FXML
    public AnchorPane root;

    @FXML
    private AnchorPane container;

    private final BrokerController brokerController;
    private final InfoController infoController;

    public NavigationController() {
        brokerController = ControllerFactory.newInstance(BrokerController.class);
        infoController = ControllerFactory.newInstance(InfoController.class);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        showInfoView(null);
    }

    @FXML
    public void showBrokerView(final @Nullable ActionEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        container.getChildren().clear();
        container.getChildren().add(brokerController.root);
    }

    @FXML
    public void showInfoView(final @Nullable ActionEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        container.getChildren().clear();
        container.getChildren().add(infoController.root);
    }
}
