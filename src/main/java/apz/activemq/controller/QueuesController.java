package apz.activemq.controller;

import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Queue;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.sun.istack.internal.Nullable;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.util.Utils.setupCellValueFactory;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

public class QueuesController implements Initializable {

    @FXML
    public StackPane root;

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

    @Inject
    private JmxClient jmxClient;

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    private final ObservableList<Queue> queues = observableArrayList();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        // table
        table.setShowRoot(false);
        table.setRowFactory(null);
        table.setRoot(new RecursiveTreeItem<>(queues, RecursiveTreeObject::getChildren));

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
    }

    @FXML
    public void refresh(final @Nullable MouseEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        final Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() {

                final List<Queue> jmxQueues = jmxClient.getQueues().stream()
                        .map(Queue::new)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));

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
                });

                return null;
            }
        };

        scheduledExecutorService.submit(task);
    }
}
