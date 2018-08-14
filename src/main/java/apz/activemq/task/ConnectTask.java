package apz.activemq.task;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.controller.ConnectionController;
import apz.activemq.jmx.JmxClient;
import apz.activemq.jmx.exception.JmxConnectionException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javafx.application.Platform.runLater;

public class ConnectTask implements Runnable {

    private final ConnectionController connectionController;
    private final SimpleSnackbar snackbar;
    private final JmxClient jmxClient;

    public ConnectTask(final ConnectionController connectionController, final SimpleSnackbar snackbar, final JmxClient jmxClient) {

        requireNonNull(connectionController, "connectionController must not be null");
        requireNonNull(snackbar, "snackbar must not be null");
        requireNonNull(jmxClient, "jmxClient must not be null");

        this.connectionController = connectionController;
        this.snackbar = snackbar;
        this.jmxClient = jmxClient;
    }

    @Override
    public void run() {

        runLater(() -> connectionController.setConnecting(true));

        try {
            final String host = connectionController.getHost();
            final Integer port = connectionController.getPort();
            jmxClient.connect(host, port);
            snackbar.info(format("Connected to %s:%d", host, port));
            connectionController.close();

        } catch (final JmxConnectionException e) {
            snackbar.error(format("Connection failed to %s:%d", e.getHost(), e.getPort()));

        } finally {
            runLater(() -> connectionController.setConnecting(false));
        }
    }
}
