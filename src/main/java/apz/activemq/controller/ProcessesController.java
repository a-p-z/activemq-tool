package apz.activemq.controller;

import apz.activemq.component.CreateConsumerJFXDialog;
import apz.activemq.component.CreateProducerJFXDialog;
import apz.activemq.injection.Inject;
import apz.activemq.jmx.JmxClient;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXNodesList;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Paint;
import org.apache.activemq.broker.jmx.DestinationViewMBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static com.jfoenix.controls.JFXButton.ButtonType.RAISED;
import static de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon.*;
import static javafx.application.Platform.runLater;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.scene.text.TextAlignment.CENTER;

public class ProcessesController implements Initializable {

    private static final String ANIMATED_OPTION_BUTTON = "animated-option-button";
    private static final String ANIMATED_OPTION_SUB_BUTTON = "animated-option-sub-button";

    @FXML
    public StackPane root;

    @FXML
    public TilePane processContainer;

    @FXML
    private JFXNodesList nodesList;

    @Inject
    private JmxClient jmxClient;

    @Inject
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        final AnimatedNode plus = new AnimatedNode();
        final ProcessAnimatedNode consumer = new ProcessAnimatedNode("consumer", DOWNLOAD);
        final ProcessAnimatedNode producer = new ProcessAnimatedNode("producer", UPLOAD);

        nodesList.setSpacing(10);
        nodesList.setAlignment(CENTER_LEFT);
        nodesList.addAnimatedNode(plus);
        nodesList.addAnimatedNode(consumer);
        nodesList.addAnimatedNode(producer);

        consumer.setOnMouseClicked(this::createConsumerJFXDialog);
        producer.setOnMouseClicked(this::createProducerJFXDialog);
    }

    private void createConsumerJFXDialog(final @Nullable MouseEvent event) {

        Optional.ofNullable(event).ifPresent(MouseEvent::consume);

        nodesList.animateList(false);

        final ConsumerController consumerController = newInstance(ConsumerController.class);
        consumerController.setParent(this);

        final CreateConsumerJFXDialog createConsumerJFXDialog = new CreateConsumerJFXDialog(root, consumerController::addRoute, null);

        scheduledExecutorService.execute(() -> jmxClient.getQueues().stream()
                .map(DestinationViewMBean::getName)
                .map(queue -> "jms:" + queue)
                .forEach(uri -> runLater(() -> createConsumerJFXDialog.addQueueSuggestion(uri))));
    }

    private void createProducerJFXDialog(final @Nullable MouseEvent event) {

        Optional.ofNullable(event).ifPresent(MouseEvent::consume);

        nodesList.animateList(false);

        final ProducerController producerController = newInstance(ProducerController.class);
        producerController.setParent(this);

        final CreateProducerJFXDialog createProducerJFXDialog = new CreateProducerJFXDialog(root, producerController::addRoute, null);

        scheduledExecutorService.execute(() -> jmxClient.getQueues().stream()
                .map(DestinationViewMBean::getName)
                .map(queue -> "jms:" + queue)
                .forEach(uri -> runLater(() -> createProducerJFXDialog.addQueueSuggestion(uri))));
    }

    public void addProcess(final @Nonnull AnchorPane processPane) {
        int index = processContainer.getChildren().size() - 1;
        processContainer.getChildren().add(index, processPane);
    }

    public void removeProcess(final @Nonnull AnchorPane processPane) {
        processContainer.getChildren().remove(processPane);
    }

    private static class AnimatedNode extends JFXButton {

        private static final String STYLE = "-fx-text-fill:#fbfbfb; -fx-background-color:#c12766; -fx-background-radius: 25;";

        AnimatedNode() {

            final MaterialDesignIconView icon = new MaterialDesignIconView(PLUS);

            setGraphic(icon);
            setTooltip(new Tooltip("createProcess process"));
            setPrefHeight(50);
            setPrefWidth(50);
            setStyle(STYLE);
            setButtonType(RAISED);
            getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON);

            icon.setSize("32");
            icon.setFill(Paint.valueOf("#fbfbfb"));
            icon.setTextAlignment(CENTER);
        }
    }

    private static class ProcessAnimatedNode extends HBox {

        private static final String STYLE = "-fx-background-color:#fbfbfb; -fx-background-radius: 20;";
        private static final Paint FILL = Paint.valueOf("#c12766");

        private final JFXButton button;

        ProcessAnimatedNode(final @Nonnull String text, final @Nonnull MaterialDesignIcon icon) {

            final MaterialDesignIconView iconView = new MaterialDesignIconView(icon);
            final Label label = new Label(text);

            button = new JFXButton(null, iconView);
            button.setButtonType(RAISED);
            button.setPrefWidth(40);
            button.setPrefHeight(40);
            button.setStyle(STYLE);
            button.getStyleClass().addAll(ANIMATED_OPTION_BUTTON, ANIMATED_OPTION_SUB_BUTTON);

            iconView.setSize("24");
            iconView.setFill(FILL);

            label.setPrefHeight(40);
            label.setPrefWidth(150);

            setId(text);
            setSpacing(10);
            getChildren().addAll(button, label);
        }
    }
}
