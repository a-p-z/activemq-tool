package apz.activemq.controller;

import apz.activemq.component.ConfirmJFXDialog;
import apz.activemq.contextmenu.HideColumnContextMenu;
import apz.activemq.contextmenu.ShowColumnContextMenu;
import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Message;
import apz.activemq.model.Queue;
import apz.activemq.rowfactory.MessageRowFactory;
import apz.activemq.util.Utils;
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
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

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
    private JFXTreeTableColumn<Message, Void> add;

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
        table.setRowFactory(new MessageRowFactory(this));
        table.setRoot(new RecursiveTreeItem<>(messages, RecursiveTreeObject::getChildren));
        table.setSortMode(ONLY_FIRST_LEVEL);
        table.predicateProperty().addListener((observable, oldValue, newValue) -> scheduledExecutorService.schedule(() ->
                runLater(() -> table.sort()), 300, MILLISECONDS));

        // columns binding
        messageId.setContextMenu(null);
        setupCellValueFactory(messageId, message -> message.id);

        correlationId.setContextMenu(new HideColumnContextMenu(correlationId));
        setupCellValueFactory(correlationId, message -> message.correlationId);

        mode.setContextMenu(new HideColumnContextMenu(mode));
        setupCellValueFactory(mode, message -> message.mode);

        priority.setContextMenu(new HideColumnContextMenu(priority));
        setupCellValueFactory(priority, message -> message.priority);

        redelivered.setContextMenu(new HideColumnContextMenu(redelivered));
        setupCellValueFactory(redelivered, message -> message.redelivered);

        replyTo.setContextMenu(new HideColumnContextMenu(replyTo));
        setupCellValueFactory(replyTo, message -> message.replyTo);

        timestamp.setContextMenu(new HideColumnContextMenu(timestamp));
        setupCellValueFactory(timestamp, message -> message.timestamp);

        type.setContextMenu(new HideColumnContextMenu(type));
        setupCellValueFactory(type, message -> message.type);

        destination.setContextMenu(new HideColumnContextMenu(destination));
        setupCellValueFactory(destination, message -> message.destination);

        expiration.setContextMenu(new HideColumnContextMenu(expiration));
        setupCellValueFactory(expiration, message -> message.expiration);

        deliveryCount.setContextMenu(new HideColumnContextMenu(deliveryCount));
        setupCellValueFactory(deliveryCount, message -> message.deliveryCount);

        groupId.setContextMenu(new HideColumnContextMenu(groupId));
        setupCellValueFactory(groupId, message -> message.groupId);

        groupSequence.setContextMenu(new HideColumnContextMenu(groupSequence));
        setupCellValueFactory(groupSequence, message -> message.groupSequence);

        producerTxId.setContextMenu(new HideColumnContextMenu(producerTxId));
        setupCellValueFactory(producerTxId, message -> message.producerTxId);

        activeMQBrokerInTime.setContextMenu(new HideColumnContextMenu(activeMQBrokerInTime));
        setupCellValueFactory(activeMQBrokerInTime, message -> message.activeMQBrokerInTime);

        activeMQBrokerOutTime.setContextMenu(new HideColumnContextMenu(activeMQBrokerOutTime));
        setupCellValueFactory(activeMQBrokerOutTime, message -> message.activeMQBrokerOutTime);

        size.setContextMenu(new HideColumnContextMenu(size));
        setupCellValueFactory(size, message -> message.size);

        body.setContextMenu(new HideColumnContextMenu(body));
        setupCellValueFactory(body, message -> message.body);

        add.setContextMenu(new ShowColumnContextMenu(table));

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
     * add message user column to the table if not present
     * @param messageUserKeys message user key
     */
    public void addMessageUserColumns(final Set<?> messageUserKeys) {

        requireNonNull(messageUserKeys, "messageUserKeys must not be null");

        final List<String> currentColumnNames = table.getColumns().stream()
                .map(TableColumnBase::getText)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        messageUserKeys.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(name -> !currentColumnNames.contains(name))
                .map((Function<String, JFXTreeTableColumn<Message, Object>>) JFXTreeTableColumn::new)
                .peek(column -> column.setVisible(false))
                .peek(column -> column.setContextMenu(new HideColumnContextMenu(column)))
                .peek(Utils::setupCellValueFactory)
                .forEach(column -> table.getColumns().add(currentColumnNames.size() - 1, column));
    }

    /**
     * delete selected messages
     */
    public void deleteSelectedMessages() {

        final List<Message> selectedMessages = getSelectedMessages();
        final int total = selectedMessages.size();
        final String headingMessage = format("Delete %d message%c", total, total > 1 ? 's' : '\0');
        final String bodyMessage = format("You are deleting %d message%c", total, total > 1 ? 's' : '\0');
        final Runnable deleteSelectedMessages = () -> {
            selectedMessages.stream()
                    .filter(message -> queue.getValue().removeMessage(message.id.getValue()))
                    .forEach(messages::remove);

            scheduledExecutorService.schedule(() -> runLater(() -> {
                table.sort();
                applyFilter().changed(search.textProperty(), search.getText(), search.getText());
            }), 300, MILLISECONDS);
        };

        new ConfirmJFXDialog(root, deleteSelectedMessages, headingMessage, bodyMessage, "Delete");
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
                    .filter(key -> !"+".equals(key))
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            table.setPredicate(item -> item.getValue().contains(keys, newValue.trim().toLowerCase()));
        };
    }

    /**
     * get selected messages
     *
     * @return selected messages
     */
    private List<Message> getSelectedMessages() {

        return table.getSelectionModel().getSelectedItems().stream()
                .map(TreeItem::getValue)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
