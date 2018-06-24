package apz.activemq.task;

import apz.activemq.controller.ConnectionController;
import apz.activemq.jmx.JmxClient;
import apz.activemq.jmx.exception.JmxConnectionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ConnectTaskTest extends ApplicationTest {

    @Spy
    private ConnectionController connectionController;

    @Mock
    private JmxClient jmxClient;

    @InjectMocks
    private ConnectTask connectTask;

    @Test
    public void onSuccessfulConnectionControllerShouldBeClosed() throws JmxConnectionException {
        // given
        doAnswer(answer -> "activemq.test.com").when(connectionController).getHost();
        doAnswer(answer -> 1099).when(connectionController).getPort();
        doNothing().when(connectionController).setConnecting(anyBoolean());
        doNothing().when(connectionController).close();

        // when
        connectTask.run();

        // then
        verify(connectionController).setConnecting(true);
        verify(connectionController).getHost();
        verify(connectionController).getPort();
        verify(connectionController).close();
        verify(connectionController).setConnecting(false);
        verifyNoMoreInteractions(connectionController);
        verify(jmxClient).connect("activemq.test.com", 1099);
        verifyNoMoreInteractions(jmxClient);
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
        verify(connectionController).setConnecting(true);
        verify(connectionController).getHost();
        verify(connectionController).getPort();
        verify(connectionController).setConnecting(false);
        verifyNoMoreInteractions(connectionController);
        verify(jmxClient).connect("activemq.test.com", 1099);
        verifyNoMoreInteractions(jmxClient);
    }
}