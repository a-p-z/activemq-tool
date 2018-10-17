package apz.activemq.task;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.controller.ConnectionController;
import apz.activemq.jmx.JmxClient;
import apz.activemq.jmx.exception.JmxConnectionException;
import apz.activemq.utils.ActiveMQJMXService;
import org.apache.camel.CamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;

import static apz.activemq.utils.AssertUtils.retry;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public class ConnectTaskTest extends ApplicationTest {

    private static final ActiveMQJMXService ACTIVE_MQJMX_SERVICE = new ActiveMQJMXService();

    @Spy
    private ConnectionController connectionController;

    @Mock
    private JmxClient jmxClient;

    @Mock
    private SimpleSnackbar snackbar;

    @Mock
    private CamelContext camelContext;

    @InjectMocks
    private ConnectTask connectTask;

    @Test
    public void onSuccessfulConnectionControllerShouldBeClosed() {
        // given
        doAnswer(answer -> "activemq.test.com").when(connectionController).getHost();
        doAnswer(answer -> 1099).when(connectionController).getPort();
        doNothing().when(connectionController).setConnecting(anyBoolean());
        doNothing().when(connectionController).close();
        given(jmxClient.getBroker()).willReturn(ACTIVE_MQJMX_SERVICE.getBroker());
        // when
        connectTask.run();

        // then
        retry(() -> {
            then(connectionController).should().setConnecting(true);
            then(connectionController).should().getHost();
            then(connectionController).should().getPort();
            then(connectionController).should().close();
            then(connectionController).should().setConnecting(false);
            try {
                then(jmxClient).should().connect("activemq.test.com", 1099);
            } catch (JmxConnectionException e) {
                throw new RuntimeException(e);
            }
            then(jmxClient).should().getBroker();
            then(snackbar).should().info(anyString());
            then(connectionController).shouldHaveNoMoreInteractions();
            then(jmxClient).shouldHaveNoMoreInteractions();
            then(snackbar).shouldHaveNoMoreInteractions();
        });
    }

    @Test
    public void onFailedConnectionControllerShouldBeNotClosed() throws JmxConnectionException {
        // given
        doAnswer(answer -> "activemq.test.com").when(connectionController).getHost();
        doAnswer(answer -> 1099).when(connectionController).getPort();
        doNothing().when(connectionController).setConnecting(anyBoolean());
        given(jmxClient.connect("activemq.test.com", 1099))
                .willThrow(new JmxConnectionException("activemq.test.com", 1099, new IOException()));

        // when
        connectTask.run();

        // then
        retry(() -> {
            then(connectionController).should().setConnecting(true);
            then(connectionController).should().getHost();
            then(connectionController).should().getPort();
            then(connectionController).should().setConnecting(false);
            try {
                then(jmxClient).should().connect("activemq.test.com", 1099);
            } catch (final JmxConnectionException e) {
                throw new RuntimeException(e);
            }
            then(snackbar).should().error(anyString());

            then(connectionController).shouldHaveNoMoreInteractions();
            then(jmxClient).shouldHaveNoMoreInteractions();
            then(snackbar).shouldHaveNoMoreInteractions();
        });
    }
}