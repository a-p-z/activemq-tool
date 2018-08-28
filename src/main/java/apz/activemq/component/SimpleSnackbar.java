package apz.activemq.component;

import apz.activemq.ActiveMQTool;
import com.jfoenix.controls.JFXSnackbar;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;

public class SimpleSnackbar extends JFXSnackbar {

    public SimpleSnackbar(final @Nonnull Pane snackbarContainer) {
        super(snackbarContainer);
        setId("simpleSnackbar");
        getStylesheets().add(ActiveMQTool.class.getResource("css/simpleSnackbar.css").toExternalForm());
    }

    public void info(final @Nonnull String message) {
        final SnackbarEvent event = new SnackbarEvent(message, "info-toast");

        enqueue(event);
    }

    public void warn(final @Nonnull String message) {
        final SnackbarEvent event = new SnackbarEvent(message, "warning-toast");
        enqueue(event);
    }

    public void error(final @Nonnull String message) {
        final SnackbarEvent event = new SnackbarEvent(message, "error-toast");
        enqueue(event);
    }
}
