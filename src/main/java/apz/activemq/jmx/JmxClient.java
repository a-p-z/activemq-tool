package apz.activemq.jmx;

import apz.activemq.jmx.exception.JmxConnectionException;
import apz.activemq.jmx.exception.JmxConnectionNotInitializedError;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;
import static javax.management.MBeanServerInvocationHandler.newProxyInstance;

public class JmxClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(JmxClient.class);
    public static final Integer DEFAULT_PORT = 1099;
    private static final String SERVICE_URL_FORMAT = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";
    private static final ObjectName BROKER_OBJECT_NAME;

    static {
        try {
            BROKER_OBJECT_NAME = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");
        } catch (final MalformedObjectNameException e) {
            throw new IllegalStateException(e);
        }
    }

    private MBeanServerConnection connection = null;
    private BrokerViewMBean broker = null;

    public JMXServiceURL connect(final String host, final Integer port) throws JmxConnectionException {

        try {
            final JMXServiceURL serviceURL = new JMXServiceURL(format(SERVICE_URL_FORMAT, host, port));
            LOGGER.info("connecting to {}", serviceURL);
            final JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL);
            connection = jmxConnector.getMBeanServerConnection();
            broker = newProxyInstance(connection, BROKER_OBJECT_NAME, BrokerViewMBean.class, true);
            LOGGER.info("connected to {}", serviceURL);
            return serviceURL;

        } catch (final IOException e) {
            throw new JmxConnectionException(host, port, e);
        }
    }

    public BrokerViewMBean getBroker() {

        LOGGER.info("getting broker from server");

        return Optional.ofNullable(broker).orElseThrow(JmxConnectionNotInitializedError::new);
    }
}
