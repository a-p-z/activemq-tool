package apz.activemq.controller;

import apz.activemq.component.ConfirmJFXDialog;
import apz.activemq.component.SelectDestinationJFXDialog;
import apz.activemq.component.SimpleSnackbar;
import apz.activemq.contextmenu.HideColumnContextMenu;
import apz.activemq.contextmenu.ShowColumnContextMenu;
import apz.activemq.converter.MessageToStringConverter;
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
import javafx.beans.binding.StringExpression;
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
import javafx.scene.control.TreeTableView.TreeTableViewSelectionModel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import static apz.activemq.util.Utils.setupCellValueFactory;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.collections.FXCollections.emptyObservableList;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.control.SelectionMode.MULTIPLE;
import static javafx.scene.control.TreeSortMode.ONLY_FIRST_LEVEL;
import static javafx.scene.input.KeyCode.C;

public class MessagesController implements Initializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(MessagesController.class);
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

    @Inject
    private MessageToStringConverter messageToStringConverter;

    @Inject
    private SimpleSnackbar snackbar;

    private final ObservableList<Message> messages = observableArrayList();
    private final ObjectProperty<Queue> queue = new SimpleObjectProperty<>();

    @Override
    public void initialize(final @Nonnull URL location, final @Nonnull ResourceBundle resources) {

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
        table.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == C) {
                copySelectedMessagesToClipboard();
            }
        });

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

                try {
                    final List<Message> refreshed = queue.getValue().browse();

                    runLater(() -> {
                        messages.clear();
                        messages.addAll(refreshed);
                        table.setCurrentItemsCount(table.getRoot().getChildren().size());
                        sortAndApplyFilter();
                    });
                } catch (final RuntimeException e) {
                    snackbar.error("Messages refresh failed");
                    LOGGER.error("error refreshing messages", e);

                } finally {
                    runLater(() -> progressBar.setProgress(-0.0));
                }

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
    public void addMessageUserColumns(final @Nonnull Set<?> messageUserKeys) {

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
            long deleted = selectedMessages.stream()
                    .filter(message -> queue.getValue().removeMessage(message.id.getValue()))
                    .filter(messages::remove)
                    .count();

            snackbar("deleted", deleted, total);
            sortAndApplyFilter();
        };

        new ConfirmJFXDialog(root, deleteSelectedMessages, headingMessage, bodyMessage, "Delete");
    }

    /**
     * show dialog for copy selected messages
     */
    public void copySelectedMessagesTo() {

        final List<Message> selectedMessages = getSelectedMessages();
        final int total = selectedMessages.size();
        final String selectHeading = format("Copy %d message%c", total, total > 1 ? 's' : '\0');
        final String confirmationHeading = format("You are coping message%c to:", total > 1 ? 's' : '\0');
        final Function<String, Runnable> copySelectedMessagesTo = destination -> () -> {
            long copied = selectedMessages.stream()
                    .map(m -> m.id)
                    .map(StringExpression::getValue)
                    .filter(messageId -> queue.getValue().copyMessageTo(messageId, destination))
                    .count();

            snackbar("copied", copied, total);
        };
        final Consumer<String> onSelected = destination -> new ConfirmJFXDialog(root, copySelectedMessagesTo.apply(destination), confirmationHeading, destination, "Copy");
        final SelectDestinationJFXDialog selectDestinationJFXDialog = new SelectDestinationJFXDialog(root, selectHeading, queue.getValue().name.getValue(), onSelected);

        scheduledExecutorService.execute(() -> jmxClient.getQueues().stream()
                .map(DestinationViewMBean::getName)
                .forEach(selectDestinationJFXDialog::addDestination));
    }

    /**
     * show dialog for copy selected messages
     */
    public void copySelectedMessagesToClipboard() {

        final List<Message> selectedMessages = getSelectedMessages();
        final ClipboardContent clipboardContent = new ClipboardContent();

        final Boolean copied = selectedMessages.stream()
                .map(message -> messageToStringConverter.convert(message))
                .collect(collectingAndThen(joining("\n"), clipboardContent::putString));

        if (copied) {
            snackbar.info("Messages have been copied");
        } else {
            snackbar.error("Copy messages failed");
        }

        table.getSelectionModel().clearSelection();

        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    /**
     * show dialog for move selected messages
     */
    public void moveSelectedMessagesTo() {

        final List<Message> selectedMessages = getSelectedMessages();
        final int total = selectedMessages.size();
        final String selectHeading = format("Move %d message%c", total, total > 1 ? 's' : '\0');
        final String confirmationHeading = format("You are moving message%c to:", total > 1 ? 's' : '\0');
        final Function<String, Runnable> moveSelectedMessagesTo = destination -> () -> {
            long moved = selectedMessages.stream()
                    .filter(message -> queue.getValue().moveMessageTo(message.id.getValue(), destination))
                    .filter(messages::remove)
                    .count();

            snackbar("moved", moved, total);
            sortAndApplyFilter();
        };
        final Consumer<String> onSelected = destination -> new ConfirmJFXDialog(root, moveSelectedMessagesTo.apply(destination), confirmationHeading, destination, "Move");
        final SelectDestinationJFXDialog selectDestinationJFXDialog = new SelectDestinationJFXDialog(root, selectHeading, queue.getValue().name.getValue(), onSelected);


        scheduledExecutorService.execute(() -> jmxClient.getQueues().stream()
                .map(DestinationViewMBean::getName)
                .forEach(queue -> runLater((() -> selectDestinationJFXDialog.addDestination(queue)))));
    }

    /**
     * set queue of messages
     *
     * @param queue queue
     */
    void setQueue(final @Nonnull Queue queue) {
        this.queue.set(queue);
    }

    /**
     * set parent
     *
     * @param parent queue controller
     */
    void setParent(final @Nonnull QueuesController parent) {
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

        final TreeTableViewSelectionModel<Message> selectionModel = table.getSelectionModel();
        final ObservableList<TreeItem<Message>> selectedItems = null != selectionModel ? selectionModel.getSelectedItems() : emptyObservableList();

        return selectedItems.stream()
                .map(TreeItem::getValue)
                .filter(Objects::nonNull)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private void sortAndApplyFilter() {

        scheduledExecutorService.schedule(() -> runLater(() -> {
            table.sort();
            applyFilter().changed(search.textProperty(), search.getText(), search.getText());
        }), 300, MILLISECONDS);
    }

    private void snackbar(final @Nonnull String action, final long count, final int total) {

        if (count == total) {
            snackbar.info(format("All message have been %s", action));

        } else if (count > 0) {
            snackbar.warn(format("%d of %d have not been %s", total - count, total, action));

        } else {
            snackbar.error(format("No message have been %s", action));
        }
    }
}
