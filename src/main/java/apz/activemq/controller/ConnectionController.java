package apz.activemq.controller;

import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import apz.activemq.jmx.exception.JmxConnectionException;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static apz.activemq.jmx.JmxClient.DEFAULT_PORT;
import static java.lang.Integer.parseInt;
import static javafx.application.Platform.runLater;
import static javafx.scene.text.Font.font;
import static javafx.scene.text.FontWeight.NORMAL;

public class ConnectionController implements Initializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectionController.class);

    @FXML
    private JFXDialog dialog;

    @FXML
    private Label head;

    @FXML
    private JFXTextField host;

    @FXML
    private JFXTextField port;

    @FXML
    private JFXButton connect;

    @Inject
    private JmxClient jmxClient;

    public void initialize(final URL location, final ResourceBundle resources) {
        // head
        head.setFont(font("Verdana, Helvetica, Arial, sans-serif", NORMAL, 14));

        // avoid compulsive connection
        connect.setOnMouseClicked(event -> {
            host.setDisable(true);
            connect.setDisable(true);
            port.setDisable(true);
        });
    }

    public void show(final StackPane container) {
        dialog.setDialogContainer(container);
        dialog.show();
    }

    public void setOnConnected(final EventHandler<? super JFXDialogEvent> handler) {
        dialog.setOnDialogClosed(handler);
    }

    @FXML
    private void connect() {

        try {
            final String host = this.host.getText().trim();
            final Integer port = !this.port.getText().isEmpty() ? parseInt(this.port.getText()) : DEFAULT_PORT;

            jmxClient.connect(host, port);
            dialog.close();

        } catch (final JmxConnectionException e) {
            LOGGER.error("Error connecting to " + host + ":" + port, e);
        } finally {
            runLater(() -> {
                host.setDisable(false);
                connect.setDisable(false);
                port.setDisable(false);
            });
        }
    }
}
