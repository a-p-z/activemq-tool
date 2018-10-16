package apz.activemq.controller;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.jmx.JmxClient;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.MockUtils.spyBrokerViewMBean;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NavigationControllerTest extends ApplicationTest {

    @Mock
    private HostServicesDelegate hostServices;

    @Mock
    private JmxClient jmxClient;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @Mock
    private SimpleSnackbar snackbar;

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("hostServices", hostServices);
        register("jmxClient", jmxClient);
        register("scheduledExecutorService", scheduledExecutorService);
        register("snackbar", snackbar);

        final NavigationController navigationController = newInstance(NavigationController.class);

        stackPane.getChildren().add(navigationController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void whenClickOnInfoTitleShouldBeInfo() {
        // when
        clickOn("#info");

        // then
        final Label title = lookup("#title").query();
        then(hostServices).shouldHaveZeroInteractions();
        then(jmxClient).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("title should be INFO", title::getText, is("INFO"));
    }

    @Test
    public void whenClickOnBrokerTitleShouldBeBroker() {
        // given
        final BrokerViewMBean brokerViewMBean = spyBrokerViewMBean("id", "name", "version", "uptime", 30, 60, 90);
        given(jmxClient.getBroker()).willReturn(brokerViewMBean);

        // when
        clickOn("#broker");

        // then
        final Label title = lookup("#title").query();
        then(hostServices).shouldHaveZeroInteractions();
        then(jmxClient).should().getBroker();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("title should be BROKER", title::getText, is("BROKER"));
    }

    @Test
    public void whenClickOnQueuesTitleShouldBeQueues() {
        // when
        clickOn("#queues");

        // then
        final Label title = lookup("#title").query();
        then(hostServices).shouldHaveZeroInteractions();
        then(jmxClient).shouldHaveZeroInteractions();
        then(scheduledExecutorService).should().submit(any(Runnable.class));
        then(scheduledExecutorService).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("title should be QUEUES", title::getText, is("QUEUES"));
    }

    @Test
    public void whenClickOnProcessesTitleShouldBeProcesses() {
        // when
        clickOn("#processes");

        // then
        final Label title = lookup("#title").query();
        then(hostServices).shouldHaveZeroInteractions();
        then(jmxClient).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("title should be PROCESSES", title::getText, is("PROCESSES"));
    }
}