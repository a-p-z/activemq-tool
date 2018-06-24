package apz.activemq.controller;

import apz.activemq.jmx.JmxClient;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.jmx.JmxClient.DEFAULT_PORT;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.AssertUtils.retry;
import static javafx.application.Platform.runLater;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest extends ApplicationTest {

    @Mock
    private JmxClient jmxClient;

    @Mock
    private EventHandler<? super JFXDialogEvent> onConnectedAction;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    private final StackPane stackPane = new StackPane();

    private ConnectionController connectionController;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("jmxClient", jmxClient);
        register("scheduledExecutorService", scheduledExecutorService);

        connectionController = newInstance(ConnectionController.class);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void show() {
        // when
        runLater(() -> connectionController.show(stackPane));

        // then
        verifyZeroInteractions(jmxClient);
        verifyZeroInteractions(onConnectedAction);
        verifyZeroInteractions(scheduledExecutorService);
        assertThat("dialog should be shown", lookup("#dialog")::query, notNullValue());
    }

    @Test
    public void whenCloseConnectOnConnectedActionShouldBeHandled() {
        // given
        connectionController.setOnConnected(onConnectedAction);
        runLater(() -> connectionController.show(stackPane));

        // when
        runLater(() -> connectionController.close());

        // then
        retry(() -> {
            verifyZeroInteractions(jmxClient);
            verify(onConnectedAction).handle(any());
            verifyNoMoreInteractions(onConnectedAction);
            verifyZeroInteractions(scheduledExecutorService);
        });
        assertThat("dialog should be closed", lookup("#dialog")::query, nullValue());
    }

    @Test
    public void whenSetConnectionTrueProgressBarShouldBeVisible() {
        // given
        runLater(() -> connectionController.show(stackPane));

        // when
        runLater(() -> connectionController.setConnecting(true));

        // then

        verifyZeroInteractions(jmxClient);
        verifyZeroInteractions(onConnectedAction);
        verifyZeroInteractions(scheduledExecutorService);
        assertThat("progress should not be null", lookup("#progressBar")::query, notNullValue());
        assertThat("progress should be visible", lookup("#progressBar").query()::isVisible, is(true));
        assertThat("progress should be -1.0", lookup("#progressBar").queryAs(JFXProgressBar.class)::getProgress, is(-1.0));
    }

    @Test
    public void whenSetConnectionFalseProgressBarShouldNotBeVisible() {
        // given
        runLater(() -> connectionController.show(stackPane));

        // when
        runLater(() -> connectionController.setConnecting(false));

        // then
        verifyZeroInteractions(jmxClient);
        verifyZeroInteractions(onConnectedAction);
        verifyZeroInteractions(scheduledExecutorService);
        assertThat("progress should not be null", lookup("#progressBar")::query, notNullValue());
        assertThat("progress should not be visible", lookup("#progressBar").query()::isVisible, is(false));
        assertThat("progress should be -1.0", lookup("#progressBar").queryAs(JFXProgressBar.class)::getProgress, is(0.0));
    }

    @Test
    public void getHost() {
        // given
        runLater(() -> connectionController.show(stackPane));

        // when
        retry(() -> lookup("#host").queryAs(JFXTextField.class).setText("   activemq.test.com   "));

        // then
        verifyZeroInteractions(jmxClient);
        verifyZeroInteractions(onConnectedAction);
        verifyZeroInteractions(scheduledExecutorService);
        assertThat("host should be 'activemq.test.com'", connectionController::getHost, is("activemq.test.com"));
    }

    @Test
    public void getDefaultPort() {
        // then
        verifyZeroInteractions(jmxClient);
        verifyZeroInteractions(onConnectedAction);
        verifyZeroInteractions(scheduledExecutorService);
        assertThat("default port should be " + DEFAULT_PORT, connectionController::getPort, is(DEFAULT_PORT));
    }

    @Test
    public void getPort() {
        // given
        runLater(() -> connectionController.show(stackPane));

        // when
        retry(() -> lookup("#port").queryAs(JFXTextField.class).setText("2099"));

        // then
        verifyZeroInteractions(jmxClient);
        verifyZeroInteractions(onConnectedAction);
        verifyZeroInteractions(scheduledExecutorService);
        assertThat("port should be 2099", connectionController::getPort, is(2099));
    }

    @Test
    public void whenClickOnConnectAndHostIsValidATaskShouldBeSubmitted() {
        // given
        runLater(() -> connectionController.show(stackPane));
        retry(() -> lookup("#host").queryAs(JFXTextField.class).setText("activemq.test.com"));

        // when
        retry(() -> clickOn("#connect"));

        // then
        verifyZeroInteractions(jmxClient);
        verifyZeroInteractions(onConnectedAction);
        verify(scheduledExecutorService).submit(any(Runnable.class));
        verifyNoMoreInteractions(scheduledExecutorService);
    }
}