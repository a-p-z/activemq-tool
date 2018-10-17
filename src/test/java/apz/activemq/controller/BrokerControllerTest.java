package apz.activemq.controller;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.jmx.JmxClient;
import apz.activemq.utils.ActiveMQJMXService;
import com.jfoenix.controls.JFXSpinner;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class BrokerControllerTest extends ApplicationTest {

    private static final ActiveMQJMXService ACTIVE_MQJMX_SERVICE = new ActiveMQJMXService();

    @Mock
    private JmxClient jmxClient;

    @Mock
    private SimpleSnackbar snackbar;

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("jmxClient", jmxClient);
        register("snackbar", snackbar);

        final BrokerController brokerController = newInstance(BrokerController.class);

        stackPane.getChildren().add(brokerController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void whenClickOnRefreshValueShouldBeSet() {
        // given
        given(jmxClient.getBroker()).willReturn(ACTIVE_MQJMX_SERVICE.getBroker());

        // when
        clickOn("#refresh");

        // then
        final Label id = lookup("#id").query();
        final Label name = lookup("#name").query();
        final Label version = lookup("#version").query();
        final Label uptime = lookup("#uptime").query();
        final JFXSpinner store = lookup("#store").query();
        final JFXSpinner memory = lookup("#memory").query();
        final JFXSpinner temp = lookup("#temp").query();
        then(jmxClient).should().getBroker();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("id should be 'ID:activemq.test.com-64874-2439984034094-1:1'", id::getText, is("ID:activemq.test.com-64874-2439984034094-1:1"));
        assertThat("name should be 'localhost'", name::getText, is("localhost"));
        assertThat("version should be '5.14.5'", version::getText, is("5.14.5"));
        assertThat("uptime should be '113 days 3 hours'", uptime::getText, notNullValue());
        assertThat("store should be 0.3", store::getProgress, notNullValue());
        assertThat("memory should be 0.6", memory::getProgress, notNullValue());
        assertThat("temp should be 0.9", temp::getProgress, notNullValue());
    }

    @Test
    public void whenClickOnRefreshAndExceptionOccurred() {
        given(jmxClient.getBroker()).willReturn(ACTIVE_MQJMX_SERVICE.getNotWorkingBrokerViewMBean());

        // when
        clickOn("#refresh");

        // then
        final Label id = lookup("#id").query();
        final Label name = lookup("#name").query();
        final Label version = lookup("#version").query();
        final Label uptime = lookup("#uptime").query();
        final JFXSpinner store = lookup("#store").query();
        final JFXSpinner memory = lookup("#memory").query();
        final JFXSpinner temp = lookup("#temp").query();
        then(jmxClient).should().getBroker();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).should().error(any());
        then(snackbar).shouldHaveNoMoreInteractions();
        assertThat("id should be null", id::getText, nullValue());
        assertThat("name should be null", name::getText, nullValue());
        assertThat("version should be null", version::getText, nullValue());
        assertThat("uptime should be null", uptime::getText, nullValue());
        assertThat("store should be 0.0", store::getProgress, is(0.0));
        assertThat("memory should be 0.0", memory::getProgress, is(0.0));
        assertThat("temp should be 0.0", temp::getProgress, is(0.0));
    }
}