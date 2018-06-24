package apz.activemq.controller;

import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import apz.activemq.task.ConnectTask;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.jmx.JmxClient.DEFAULT_PORT;
import static java.lang.Integer.parseInt;
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

    @Inject
    private JmxClient jmxClient;

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    @FXML
    private JFXProgressBar progressBar;

    public void initialize(final URL location, final ResourceBundle resources) {
        // head
        head.setFont(font("Verdana, Helvetica, Arial, sans-serif", NORMAL, 14));

    }

    public void show(final StackPane container) {
        dialog.setDialogContainer(container);
        dialog.show();
    }

    public void close() {
        dialog.close();
    }

    public void setOnConnected(final EventHandler<? super JFXDialogEvent> handler) {
        dialog.setOnDialogClosed(handler);
    }

    @FXML
    private void connect() {
        scheduledExecutorService.submit(new ConnectTask(this, jmxClient));
    }

    public void setConnecting(final boolean connecting) {
        progressBar.setVisible(connecting);
        progressBar.setProgress(connecting ? -1.0 : 0.0);
        host.setDisable(connecting);
        connect.setDisable(connecting);
        port.setDisable(connecting);
    }

    public String getHost() {
        return host.getText().trim();
    }

    public Integer getPort() {
        return !port.getText().isEmpty() ? parseInt(port.getText()) : DEFAULT_PORT;
    }
}
