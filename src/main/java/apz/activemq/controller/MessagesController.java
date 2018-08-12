package apz.activemq.controller;

import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Message;
import apz.activemq.model.Queue;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.istack.internal.Nullable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.util.Utils.setupCellValueFactory;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.control.TreeSortMode.ONLY_FIRST_LEVEL;

public class MessagesController implements Initializable {

    private static final String FOOTER_FORMAT = "Showing %d of %d (messages are limited by browser page size %d)";

    @FXML
    public StackPane root;

    @FXML
    private Label queueName;

    @FXML
    private Label separator;

    @FXML
    private JFXProgressBar progressBar;

    @FXML
    private TextField search;

    @FXML
    private JFXTreeTableView<Message> table;

    @FXML
    private JFXTreeTableColumn<Message, String> messageId;

    @FXML
    private JFXTreeTableColumn<Message, String> correlationId;

    @FXML
    private JFXTreeTableColumn<Message, String> mode;

    @FXML
    private JFXTreeTableColumn<Message, Number> priority;

    @FXML
    private JFXTreeTableColumn<Message, Boolean> redelivered;

    @FXML
    private JFXTreeTableColumn<Message, String> replyTo;

    @FXML
    private JFXTreeTableColumn<Message, Date> timestamp;

    @FXML
    private JFXTreeTableColumn<Message, String> type;

    @FXML
    private JFXTreeTableColumn<Message, String> destination;

    @FXML
    private JFXTreeTableColumn<Message, Number> expiration;

    @FXML
    private JFXTreeTableColumn<Message, Number> deliveryCount;

    @FXML
    private JFXTreeTableColumn<Message, String> groupId;

    @FXML
    private JFXTreeTableColumn<Message, Number> groupSequence;

    @FXML
    private JFXTreeTableColumn<Message, String> producerTxId;

    @FXML
    private JFXTreeTableColumn<Message, Number> activeMQBrokerInTime;

    @FXML
    private JFXTreeTableColumn<Message, Number> activeMQBrokerOutTime;

    @FXML
    private JFXTreeTableColumn<Message, Number> size;

    @FXML
    private JFXTreeTableColumn<Message, String> body;

    @FXML
    private Label footer;

    @Inject
    private JmxClient jmxClient;

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    private final ObservableList<Message> messages = observableArrayList();
    private final ObjectProperty<Queue> queue = new SimpleObjectProperty<>();

    public void initialize(final URL location, final ResourceBundle resources) {

        queueName.textProperty().bind(createStringBinding(() -> null != queue.getValue() ? queue.getValue().name.getValue() : "", queue));

        // search bar: when search test change apply filter
        search.textProperty().addListener(applyFilter());

        // table
        table.setShowRoot(false);
        table.getSelectionModel().setSelectionMode(MULTIPLE);
        table.setRoot(new RecursiveTreeItem<>(messages, RecursiveTreeObject::getChildren));
        table.setSortMode(ONLY_FIRST_LEVEL);
        table.predicateProperty().addListener((observable, oldValue, newValue) -> scheduledExecutorService.schedule(() ->
                runLater(() -> table.sort()), 300, MILLISECONDS));

        // columns binding
        messageId.setContextMenu(null);
        setupCellValueFactory(messageId, message -> message.id);

        correlationId.setContextMenu(null);
        setupCellValueFactory(correlationId, message -> message.correlationId);

        mode.setContextMenu(null);
        setupCellValueFactory(mode, message -> message.mode);

        priority.setContextMenu(null);
        setupCellValueFactory(priority, message -> message.priority);

        redelivered.setContextMenu(null);
        setupCellValueFactory(redelivered, message -> message.redelivered);

        replyTo.setContextMenu(null);
        setupCellValueFactory(replyTo, message -> message.replyTo);

        timestamp.setContextMenu(null);
        setupCellValueFactory(timestamp, message -> message.timestamp);

        type.setContextMenu(null);
        setupCellValueFactory(type, message -> message.type);

        destination.setContextMenu(null);
        setupCellValueFactory(destination, message -> message.destination);

        expiration.setContextMenu(null);
        setupCellValueFactory(expiration, message -> message.expiration);

        deliveryCount.setContextMenu(null);
        setupCellValueFactory(deliveryCount, message -> message.deliveryCount);

        groupId.setContextMenu(null);
        setupCellValueFactory(groupId, message -> message.groupId);

        groupSequence.setContextMenu(null);
        setupCellValueFactory(groupSequence, message -> message.groupSequence);

        producerTxId.setContextMenu(null);
        setupCellValueFactory(producerTxId, message -> message.producerTxId);

        activeMQBrokerInTime.setContextMenu(null);
        setupCellValueFactory(activeMQBrokerInTime, message -> message.activeMQBrokerInTime);

        activeMQBrokerOutTime.setContextMenu(null);
        setupCellValueFactory(activeMQBrokerOutTime, message -> message.activeMQBrokerOutTime);

        size.setContextMenu(null);
        setupCellValueFactory(size, message -> message.size);

        body.setContextMenu(null);
        setupCellValueFactory(body, message -> message.body);

        // footer: when row number or total messages change then change the footer
        footer.setText(format(FOOTER_FORMAT, table.getCurrentItemsCount(), messages.size(), null != queue.getValue() ? queue.getValue().getMaxPageSize() : 0));
        footer.textProperty().bind(createStringBinding(
                () -> format(FOOTER_FORMAT, table.getCurrentItemsCount(), messages.size(), null != queue.getValue() ? queue.getValue().getMaxPageSize() : 0),
                table.currentItemsCountProperty(), messages, queue));
    }

    /**
     * clear all messages and browses them from queue
     *
     * @param event mouse event
     */
    @FXML
    public void refresh(final @Nullable MouseEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        final Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                runLater(() -> progressBar.setProgress(-1.0));

                final List<Message> refreshed = queue.getValue().browse();

                runLater(() -> {
                    messages.clear();
                    messages.addAll(refreshed);
                    table.setCurrentItemsCount(table.getRoot().getChildren().size());
                    scheduledExecutorService.schedule(() -> runLater(() -> {
                        table.sort();
                        applyFilter().changed(search.textProperty(), search.getText(), search.getText());
                    }), 300, MILLISECONDS);
                });

                runLater(() -> progressBar.setProgress(-0.0));

                return null;
            }
        };

        scheduledExecutorService.submit(task);
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

    /**
     * when text search change update the table predicate
     *
     * @return change listener
     */
    private ChangeListener<? super String> applyFilter() {

        return (observable, oldValue, newValue) -> {

            final List<String> keys = table.getColumns().stream()
                    .filter(TableColumnBase::isVisible)
                    .map(TableColumnBase::getText)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            table.setPredicate(item -> item.getValue().contains(keys, newValue.trim().toLowerCase()));
        };
    }
}