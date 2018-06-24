package apz.activemq.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

import static javafx.scene.text.Font.font;
import static javafx.scene.text.FontWeight.NORMAL;

public class ConnectionController implements Initializable {

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
        dialog.close();
    }
}
