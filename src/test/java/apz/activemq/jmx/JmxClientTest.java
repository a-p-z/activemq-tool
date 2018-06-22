package apz.activemq.jmx;

import apz.activemq.jmx.exception.JmxConnectionNotInitializedError;
import org.junit.Test;

public class JmxClientTest {

    private final JmxClient jmxClient = new JmxClient();

    @Test(expected = JmxConnectionNotInitializedError.class)
    public void whenGetBrokerBeforeConnectionJmxConnectionNotInitializedError() {
        jmxClient.getBroker();
    }
}