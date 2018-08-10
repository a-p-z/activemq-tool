package apz.activemq.controller;

import apz.activemq.model.Queue;
import com.sun.istack.internal.Nullable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;
import static javafx.beans.binding.Bindings.createStringBinding;

public class MessagesController implements Initializable {

    @FXML
    public StackPane root;

    @FXML
    private Label queueName;

    @FXML
    private Label separator;

    private final ObjectProperty<Queue> queue = new SimpleObjectProperty<>();

    public void initialize(final URL location, final ResourceBundle resources) {
        queueName.textProperty().bind(createStringBinding(() -> null != queue.getValue() ? queue.getValue().name.getValue() : "", queue));
    }

    /**
     * when mouse enter in queue name separator change
     *
     * @param event mouse event
     */
    @FXML
    public void onMouseEnteredInQueueName(final @Nullable MouseEvent event) {
        Optional.ofNullable(event).ifPresent(Event::consume);
        separator.setText("<");
    }

    /**
     * when mouse exit from queue name separator change
     *
     * @param event mouse event
     */
    @FXML
    public void onMouseExitedFromQueueName(final @Nullable MouseEvent event) {
        Optional.ofNullable(event).ifPresent(Event::consume);
        separator.setText(">");
    }

    /**
     * set queue of messages
     *
     * @param queue queue
     */
    void setQueue(final Queue queue) {

        requireNonNull(queue, "queue must not be null");

        this.queue.set(queue);
    }

    /**
     * set parent
     *
     * @param parent queue controller
     */
    void setParent(final QueuesController parent) {

        requireNonNull(parent, "parent must not be null");

        // when click on title back to queues view
        queueName.setOnMouseClicked(event -> {
            parent.removeChild(root);
            queue.getValue().refresh();
        });

        parent.addChild(root);
    }
}