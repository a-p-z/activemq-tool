package apz.activemq.task;

import apz.activemq.controller.ConnectionController;
import apz.activemq.jmx.JmxClient;
import apz.activemq.jmx.exception.JmxConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;
import static javafx.application.Platform.runLater;

public class ConnectTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectTask.class);

    private final ConnectionController connectionController;
    private final JmxClient jmxClient;

    public ConnectTask(final ConnectionController connectionController, final JmxClient jmxClient) {

        requireNonNull(connectionController, "connectionController must not be null");
        requireNonNull(jmxClient, "jmxClient must not be null");

        this.connectionController = connectionController;
        this.jmxClient = jmxClient;
    }

    @Override
    public void run() {

        runLater(() -> connectionController.setConnecting(true));

        try {
            jmxClient.connect(connectionController.getHost(), connectionController.getPort());
            connectionController.close();

        } catch (final JmxConnectionException e) {
            LOGGER.error("Error connecting to " + e.getHost() + ":" + e.getPort(), e);

        } finally {
            runLater(() -> connectionController.setConnecting(false));
        }
    }
}
