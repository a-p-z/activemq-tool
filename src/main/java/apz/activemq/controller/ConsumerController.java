package apz.activemq.controller;

import apz.activemq.camel.ConsumerRouteBuilder;
import apz.activemq.camel.SerializationDataFormat;
import apz.activemq.component.SimpleSnackbar;
import apz.activemq.injection.Inject;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleLongProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import static apz.activemq.validator.StreamURIExistValidator.getPath;
import static com.jfoenix.effects.JFXDepthManager.setDepth;
import static de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon.PAUSE;
import static de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon.PLAY;
import static java.lang.String.format;
import static javafx.animation.Animation.INDEFINITE;
import static javafx.beans.binding.Bindings.createStringBinding;
import static javafx.util.Duration.seconds;

public class ConsumerController implements Initializable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConsumerController.class);

    private static final String[] BACKGROUND_COLORS = {
            "e05256", // RED
            "#6c75c6", // BLUE
            "#b3d062", // GREEN
            "#fff94d", // YELLOW
            "#9d6fc3"};// VIOLET

    private static final String[] BUTTON_COLORS = {
            "#dd3c41", // RED
            "#5a64bf", // BLUE
            "#a9ca4e", // GREEN
            "#fff71a", // YELLOW
            "#915dbb"};// VIOLET

    private static final Color[] TEXT_COLORS = {
            Color.valueOf("#570f12"), // RED
            Color.valueOf("#1d2149"), // BLUE
            Color.valueOf("#404f17"), // GREEN
            Color.valueOf("#666300"), // YELLOW
            Color.valueOf("#351e48")};// VIOLET

    @FXML
    private AnchorPane root;

    @FXML
    private JFXButton stopResumeButton;

    @FXML
    private MaterialDesignIconView iconView;

    @FXML
    private Label title;

    @FXML
    private Label info;

    @FXML
    private Label from;

    @FXML
    private Label marshal;

    @FXML
    private Label transform;

    @FXML
    private Label to;

    @Inject
    private SimpleSnackbar snackbar;

    @Inject
    private CamelContext camelContext;

    @Inject
    private SerializationDataFormat serializationDataFormat;

    private final SimpleLongProperty processedProperty = new SimpleLongProperty(0L);

    private ProcessesController parent;
    private String routeId;

    @Override
    public void initialize(final @Nonnull URL location, final @Nonnull ResourceBundle resources) {
        setDepth(root, 1);
    }

    public void addRoute(final @Nonnull String source, final @Nonnull String destination) {

        final int routeDefinitionsSize = camelContext.getRouteDefinitions().size();
        final int num = routeDefinitionsSize % 5;
        routeId = format("CONSUMER-%03d", num + 1);
        final ConsumerRouteBuilder consumerRouteBuilder = new ConsumerRouteBuilder(routeId, source, destination, serializationDataFormat);

        root.setId(format("consumer-%03d", num + 1));
        root.setStyle("-fx-background-color: " + BACKGROUND_COLORS[num]);
        stopResumeButton.setStyle("-fx-background-color: " + BUTTON_COLORS[num]);

        title.setTextFill(TEXT_COLORS[num]);
        from.setTextFill(TEXT_COLORS[num]);
        marshal.setTextFill(TEXT_COLORS[num]);
        transform.setTextFill(TEXT_COLORS[num]);
        to.setTextFill(TEXT_COLORS[num]);
        info.setTextFill(TEXT_COLORS[num]);

        title.setText(routeId);
        info.textProperty().bind(createStringBinding(() -> format("%d message%c consumed", processedProperty.get(), processedProperty.get() != 1L ? 's' : '\0'), processedProperty));
        from.setText(format("from(\"%s\")", source));
        marshal.setText("   .marshal()");
        transform.setText("   .transform(body().append(\"\\n\"))");
        to.setText(format("   .to(\"file:%s\")", getPath(destination).map(Path::getFileName).map(Path::toString).orElse("")));

        try {
            camelContext.addRoutes(consumerRouteBuilder);
            parent.addProcess(root);
            final KeyFrame keyFrame = new KeyFrame(seconds(1), actionEvent -> processedProperty.set(consumerRouteBuilder.getProcessed()));
            final Timeline timeline = new Timeline();

            timeline.getKeyFrames().setAll(keyFrame);
            timeline.setCycleCount(INDEFINITE);
            timeline.play();

        } catch (final Exception e) {
            snackbar.error(format("Create %s failed", routeId));
            LOGGER.error("error adding route to camel context", e);
        }
    }

    @FXML
    public void stopResume(final @Nullable ActionEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        if (PLAY.name().equals(iconView.getGlyphName())) {
            try {
                camelContext.resumeRoute(routeId);
                iconView.setIcon(PAUSE);

            } catch (final Exception e) {
                snackbar.error(format("Resume %s failed", routeId));
                LOGGER.error("error resuming route to camel context", e);
            }

        } else {
            try {
                camelContext.stopRoute(routeId);
                iconView.setIcon(PLAY);

            } catch (final Exception e) {
                snackbar.error(format("Stop %s failed", routeId));
                LOGGER.error("error stopping route of camel context", e);
            }
        }
    }

    @FXML
    public void close(final @Nullable ActionEvent event) {

        Optional.ofNullable(event).ifPresent(Event::consume);

        try {
            camelContext.stopRoute(routeId);

        } catch (final Exception e) {
            snackbar.error(format("Stop %s failed", routeId));
            LOGGER.error("error stopping route to camel context", e);
        }

        try {
            boolean removed = camelContext.removeRoute(routeId);

            if (removed) {
                parent.removeProcess(root);
            } else {
                snackbar.error(format("Close %s failed", routeId));
                LOGGER.error("error removing route to camel context");
            }

        } catch (final Exception e) {
            snackbar.error(format("Close %s failed", routeId));
            LOGGER.error("error removing route to camel context", e);
        }
    }

    public void setParent(final @Nonnull ProcessesController processesController) {
        parent = processesController;
    }
}
