package apz.activemq.jmx;

import apz.activemq.jmx.exception.JmxConnectionNotInitializedError;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class JmxClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(JmxClient.class);

    private BrokerViewMBean broker = null;

    public BrokerViewMBean getBroker() {

        LOGGER.info("getting broker from server");

        return Optional.ofNullable(broker).orElseThrow(JmxConnectionNotInitializedError::new);
    }
}
