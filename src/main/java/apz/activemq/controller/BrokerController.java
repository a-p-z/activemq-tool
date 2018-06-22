package apz.activemq.controller;

import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Broker;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.sun.istack.internal.Nullable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.jfoenix.effects.JFXDepthManager.setDepth;
import static javafx.application.Platform.runLater;

public class BrokerController implements Initializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(BrokerController.class);

    @FXML
    public VBox root;

    @FXML
    private JFXButton refresh;

    @FXML
    private HBox infoCard;

    @FXML
    private Label id;

    @FXML
    private Label name;

    @FXML
    private Label version;

    @FXML
    private Label uptime;

    @FXML
    private HBox storeCard;

    @FXML
    private JFXSpinner store;

    @FXML
    private HBox memoryCard;

    @FXML
    private JFXSpinner memory;

    @FXML
    private HBox tempCard;

    @FXML
    private JFXSpinner temp;

    @Inject
    private JmxClient jmxClient;

    private final Broker broker = new Broker();

    public void initialize(final URL location, final ResourceBundle resources) {

        setDepth(infoCard, 1);
        setDepth(storeCard, 1);
        setDepth(memoryCard, 1);
        setDepth(tempCard, 1);

        name.textProperty().bind(broker.name);
        version.textProperty().bind(broker.version);
        id.textProperty().bind(broker.id);
        uptime.textProperty().bind(broker.uptime);
        store.progressProperty().bind(broker.store);
        memory.progressProperty().bind(broker.memory);
        temp.progressProperty().bind(broker.temp);
    }

    @FXML
    public void refresh(final @Nullable MouseEvent event) {

        Optional.ofNullable(event).ifPresent(MouseEvent::consume);

        // avoid compulsive refresh
        refresh.setDisable(true);

        final BrokerViewMBean brokerViewMBean = jmxClient.getBroker();

        runLater(() -> {
            broker.refresh(brokerViewMBean);
            refresh.setDisable(false);
        });
    }
}
