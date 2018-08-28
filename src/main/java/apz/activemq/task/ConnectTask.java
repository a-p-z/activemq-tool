package apz.activemq.task;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.controller.ConnectionController;
import apz.activemq.jmx.JmxClient;
import apz.activemq.jmx.exception.JmxConnectionException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import static java.lang.String.format;
import static javafx.application.Platform.runLater;
import static org.apache.camel.component.jms.JmsComponent.jmsComponentAutoAcknowledge;

public class ConnectTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectTask.class);

    private final ConnectionController connectionController;
    private final SimpleSnackbar snackbar;
    private final JmxClient jmxClient;
    private final CamelContext camelContext;

    public ConnectTask(final @Nonnull ConnectionController connectionController, final @Nonnull SimpleSnackbar snackbar, final @Nonnull JmxClient jmxClient, final @Nonnull CamelContext camelContext) {
        this.connectionController = connectionController;
        this.snackbar = snackbar;
        this.jmxClient = jmxClient;
        this.camelContext = camelContext;
    }

    @Override
    public void run() {

        runLater(() -> connectionController.setConnecting(true));

        try {
            final String host = connectionController.getHost();
            final Integer port = connectionController.getPort();

            jmxClient.connect(host, port);

            final String brokerURL = jmxClient.getBroker().getTransportConnectorByType("tcp").split("\\?")[0];
            final ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(brokerURL);

            camelContext.addComponent("jms", jmsComponentAutoAcknowledge(activeMQConnectionFactory));
            camelContext.addComponent("activemq", jmsComponentAutoAcknowledge(activeMQConnectionFactory));

            snackbar.info(format("Connected to %s:%d", host, port));
            connectionController.close();

        } catch (final JmxConnectionException e) {
            snackbar.error(format("Connection failed to %s:%d", e.getHost(), e.getPort()));
            LOGGER.error(format("error connecting to %s:%d", e.getHost(), e.getPort()), e);

        } finally {
            runLater(() -> connectionController.setConnecting(false));
        }
    }
}
