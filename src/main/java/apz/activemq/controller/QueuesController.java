package apz.activemq.controller;

import apz.activemq.component.ConfirmJFXDialog;
import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import apz.activemq.listeners.QueuesTableSkinListener;
import apz.activemq.model.Queue;
import apz.activemq.rowfactory.QueueRowFactory;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.istack.internal.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.util.Utils.setupCellValueFactory;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.collections.FXCollections.observableArrayList;

public class QueuesController implements Initializable {

    private static final String FOOTER_FORMAT = "Showing %d of %d queues";

    @FXML
    public StackPane root;

    @FXML
    private JFXProgressBar progressBar;

    @FXML
    private TextField search;

    @FXML
    private JFXTreeTableView<Queue> table;

    @FXML
    private JFXTreeTableColumn<Queue, String> name;

    @FXML
    private JFXTreeTableColumn<Queue, Number> pending;

    @FXML
    private JFXTreeTableColumn<Queue, Number> consumers;

    @FXML
    private JFXTreeTableColumn<Queue, Number> enqueued;

    @FXML
    private JFXTreeTableColumn<Queue, Number> dequeued;

    @FXML
    private Label footer;

    @Inject
    private JmxClient jmxClient;

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    private final ObservableList<Queue> queues = observableArrayList();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        // search bar: when search test change apply filter
        search.textProperty().addListener(applyFilter());

        // table
        table.setShowRoot(false);
        table.skinProperty().addListener(new QueuesTableSkinListener(table, 49));
        table.setRowFactory(new QueueRowFactory(this));
        table.setRoot(new RecursiveTreeItem<>(queues, RecursiveTreeObject::getChildren));

        // when table predicate change sort rows
        table.predicateProperty().addListener((observable, oldValue, newValue) -> scheduledExecutorService.schedule(() ->
                runLater(() -> table.sort()), 300, MILLISECONDS));

        // when double-click on row browse queue
        table.setOnMousePressed(browseQueue());

        // columns binding
        name.setContextMenu(null);
        setupCellValueFactory(name, queue -> queue.name);

        pending.setContextMenu(null);
        setupCellValueFactory(pending, queue -> queue.pending);

        consumers.setContextMenu(null);
        setupCellValueFactory(consumers, queue -> queue.consumers);

        enqueued.setContextMenu(null);
        setupCellValueFactory(enqueued, queue -> queue.enqueued);

        dequeued.setContextMenu(null);
        setupCellValueFactory(dequeued, queue -> queue.dequeued);

        // footer: when row number or total queue change then change the footer
        footer.textProperty().bind(createStringBinding(
                () -> format(FOOTER_FORMAT, table.getCurrentItemsCount(), queues.size()),
                table.currentItemsCountProperty(), queues));
    }

    @FXML
    public void refresh(final @Nullable MouseEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        final Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() {

                runLater(() -> progressBar.setProgress(-1.0));

                final List<Queue> jmxQueues = jmxClient.getQueues().stream()
                        .map(Queue::new)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));

                runLater(() -> progressBar.setProgress(-0.0));

                final List<String> jmxQueueNames = jmxQueues.stream()
                        .map(q -> q.name.getValue())
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));

                final List<String> queueNames = queues.stream()
                        .map(q -> q.name.getValue())
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));

                runLater(() -> {

                    queues.stream()
                            .filter(q -> !jmxQueueNames.contains(q.name.getValue()))
                            .collect(collectingAndThen(toList(), queues::removeAll));

                    jmxQueues.stream()
                            .filter(q -> !queueNames.contains(q.name.getValue()))
                            .collect(collectingAndThen(toList(), queues::addAll));

                    queues.stream()
                            .map(q -> (Runnable) q::refresh)
                            .forEach(q -> scheduledExecutorService.submit(q));

                    scheduledExecutorService.schedule(() -> runLater(() -> {
                        table.sort();
                        applyFilter().changed(search.textProperty(), search.getText(), search.getText());
                    }), 300, MILLISECONDS);

                    progressBar.setProgress(0.0);
                });

                return null;
            }
        };

        scheduledExecutorService.submit(task);
    }

    /**
     * browse selected queue
     *
     * @param action action
     */
    public void browseQueue(final @Nullable ActionEvent action) {

        Optional.ofNullable(action).ifPresent(ActionEvent::consume);

        final Queue selectedQueue = getSelectedQueue();
        final MessagesController messagesController = newInstance(MessagesController.class);

        messagesController.setQueue(selectedQueue);
        messagesController.setParent(this);
        messagesController.refresh(null);
    }

    /**
     * purge selected queue
     *
     * @param action action
     */
    public void purgeSelectedQueue(final @Nullable ActionEvent action) {

        Optional.ofNullable(action).ifPresent(ActionEvent::consume);

        final Queue selectedQueue = getSelectedQueue();
        final String bodyMessage = format("You are purging %s", selectedQueue.name.getValue());
        final Runnable purgeSelectedQueue = () -> {
            try {
                selectedQueue.purge();

            } catch (final RuntimeException e) {
                throw e;

            } finally {
                selectedQueue.refresh();
            }
        };

        new ConfirmJFXDialog(root, purgeSelectedQueue, "Purge queue", bodyMessage, "Purge");
    }

    /**
     * delete selected queue
     *
     * @param action action
     */
    public void deleteSelectedQueue(final @Nullable ActionEvent action) {

        Optional.ofNullable(action).ifPresent(ActionEvent::consume);

        final Queue selectedQueue = getSelectedQueue();

        try {
            jmxClient.getBroker().removeQueue(selectedQueue.name.getValue());
            queues.remove(selectedQueue);

        } catch (final Exception e) {
            // do nothing
        }
    }

    /**
     * add child to stack pane
     *
     * @param child child
     */
    void addChild(final Node child) {

        requireNonNull(child, "child must not be null");

        root.getChildren().add(child);
    }

    /**
     * remove child from stack pane
     *
     * @param child child
     */
    void removeChild(final Node child) {

        requireNonNull(child, "child must not be null");

        root.getChildren().remove(child);
    }

    /**
     * when text search change update the table predicate
     *
     * @return change listener
     */
    private ChangeListener<String> applyFilter() {

        return (observable, oldValue, newValue) -> table.setPredicate(item ->
                item.getValue().name.get().toLowerCase().contains(newValue.trim().toLowerCase()));
    }

    /**
     * when a row is double clicked browses queue
     *
     * @return event handler
     */
    private EventHandler<MouseEvent> browseQueue() {

        return event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                browseQueue(null);
            }
        };
    }

    /**
     * return the selected queue
     *
     * @return selected queue
     */
    private Queue getSelectedQueue() {
        return table.getSelectionModel().getSelectedItem().getValue();
    }
}
