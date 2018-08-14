package apz.activemq.component;

import apz.activemq.ActiveMQTool;
import com.jfoenix.controls.JFXSnackbar;
import javafx.scene.layout.Pane;

public class SimpleSnackbar extends JFXSnackbar {

    public SimpleSnackbar(final Pane snackbarContainer) {
        super(snackbarContainer);
        setId("simpleSnackbar");
        getStylesheets().add(ActiveMQTool.class.getResource("css/simpleSnackbar.css").toExternalForm());
    }

    public void info(final String message) {
        final SnackbarEvent event = new SnackbarEvent(message, "info-toast");

        enqueue(event);
    }

    public void warn(final String message) {
        final SnackbarEvent event = new SnackbarEvent(message, "warning-toast");
        enqueue(event);
    }

    public void error(final String message) {
        final SnackbarEvent event = new SnackbarEvent(message, "error-toast");
        enqueue(event);
    }
}
