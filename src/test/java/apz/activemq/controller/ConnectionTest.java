package apz.activemq.controller;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.jmx.JmxClient;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.camel.CamelContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.jmx.JmxClient.DEFAULT_PORT;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.AssertUtils.retry;
import static java.util.stream.Collectors.joining;
import static javafx.application.Platform.runLater;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest extends ApplicationTest {

    @Mock
    private JmxClient jmxClient;

    @Mock
    private EventHandler<? super JFXDialogEvent> onConnectedAction;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @Mock
    private SimpleSnackbar snackbar;

    @Mock
    private CamelContext camelContext;

    private final StackPane stackPane = new StackPane();

    private ConnectionController connectionController;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("jmxClient", jmxClient);
        register("scheduledExecutorService", scheduledExecutorService);
        register("snackbar", snackbar);
        register("camelContext", camelContext);

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
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
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
            then(jmxClient).shouldHaveZeroInteractions();
            then(onConnectedAction).should().handle(any());
            then(onConnectedAction).shouldHaveNoMoreInteractions();
            then(scheduledExecutorService).shouldHaveZeroInteractions();
            then(snackbar).shouldHaveZeroInteractions();
            then(camelContext).shouldHaveZeroInteractions();
        });
    }

    @Test
    public void whenSetConnectionTrueProgressBarShouldBeVisible() {
        // given
        runLater(() -> connectionController.show(stackPane));

        // when
        runLater(() -> connectionController.setConnecting(true));

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
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
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();

        final JFXProgressBar progressBar = lookup("#progressBar").queryAs(JFXProgressBar.class);
        assertThat("progress should not be visible", progressBar::isVisible, is(false));
        assertThat("progress should be -1.0", progressBar::getProgress, is(0.0));
    }

    @Test
    public void getHost() {
        // given
        runLater(() -> connectionController.show(stackPane));

        // when
        retry(() -> lookup("#host").queryAs(JFXTextField.class).setText("   activemq.test.com   "));

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
        assertThat("host should be 'activemq.test.com'", connectionController::getHost, is("activemq.test.com"));
    }

    @Test
    public void getDefaultPort() {
        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
        assertThat("default port should be " + DEFAULT_PORT, connectionController::getPort, is(DEFAULT_PORT));
    }

    @Test
    public void getPort() {
        // given
        runLater(() -> connectionController.show(stackPane));

        // when
        retry(() -> lookup("#port").queryAs(JFXTextField.class).setText("2099"));

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
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
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).should().submit(any(Runnable.class));
        then(scheduledExecutorService).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
    }

    @Test
    public void whenClickOnConnectAndHostIsNotValidErrorShouldBeShown() {
        // given
        runLater(() -> connectionController.show(stackPane));
        retry(() -> lookup("#host").queryAs(JFXTextField.class).setText("activemq.test.com/§nvalid"));

        // when
        retry(() -> clickOn("#connect"));

        // then
        final JFXTextField hostTextField = lookup("#host").query();
        final Supplier<String> errors = () -> hostTextField.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
        assertThat("error message should be 'malformed URL'", errors,
                is("malformed URL"));
    }

    @Test
    public void whenClickOnConnectAndHostIsEmptyErrorShouldBeShown() {
        // given
        runLater(() -> connectionController.show(stackPane));

        retry(() -> {
            // when
            clickOn("#connect");

            // then
            final Supplier<String> errors = () -> lookup("#host").queryAs(JFXTextField.class)
                    .getValidators().stream()
                    .filter(ValidatorBase::getHasErrors)
                    .map(ValidatorBase::getMessage)
                    .collect(joining(", "));
            then(jmxClient).shouldHaveZeroInteractions();
            then(onConnectedAction).shouldHaveZeroInteractions();
            then(scheduledExecutorService).shouldHaveZeroInteractions();
            then(snackbar).shouldHaveZeroInteractions();
            then(camelContext).shouldHaveZeroInteractions();
            assertThat("error message should be 'host is required'", errors, is("host is required"));
        });
    }

    @Test
    public void whenFocusOnHostErrorMessageShouldBeReset() {
        // given
        runLater(() -> connectionController.show(stackPane));
        retry(() -> clickOn("#connect"));

        // when
        retry(() -> clickOn("#host"));

        // then
        final JFXTextField hostTextField = lookup("#host").query();
        final Supplier<Boolean> hasErrors = () -> hostTextField.getValidators().stream()
                .noneMatch(ValidatorBase::getHasErrors);
        then(jmxClient).shouldHaveZeroInteractions();
        then(onConnectedAction).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
        assertThat("error message should be reset", hasErrors, is(false));
    }
}