package apz.activemq.controller;

import apz.activemq.camel.SerializationDataFormat;
import apz.activemq.component.AutoCompleteJFXComboBox;
import apz.activemq.component.CreateConsumerJFXDialog;
import apz.activemq.component.CreateProducerJFXDialog;
import apz.activemq.component.SimpleSnackbar;
import apz.activemq.jmx.JmxClient;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.camel.CamelContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.AssertUtils.assumeThat;
import static java.lang.String.format;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class ProcessesControllerTest extends ApplicationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private SimpleSnackbar snackbar;

    @Mock
    private CamelContext camelContext;

    @Mock
    private SerializationDataFormat serializationDataFormat;

    @Mock
    private JmxClient jmxClient;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("snackbar", snackbar);
        register("camelContext", camelContext);
        register("serializationDataFormat", serializationDataFormat);
        register("jmxClient", jmxClient);
        register("scheduledExecutorService", scheduledExecutorService);

        final ProcessesController processesController = newInstance(ProcessesController.class);

        stackPane.getChildren().add(processesController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void whenClickOnCreateConsumerTheDialogShouldBeVisible() {

        clickOn("#nodesList");
        clickOn("#consumer");

        final CreateConsumerJFXDialog createConsumerJFXDialog = lookup("#createConsumerJFXDialog").queryAs(CreateConsumerJFXDialog.class);

        then(scheduledExecutorService).should().execute(any());

        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
        then(serializationDataFormat).shouldHaveZeroInteractions();
        then(jmxClient).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveNoMoreInteractions();

        assertThat("dialog should be visible", createConsumerJFXDialog::isVisible, is(true));
    }

    @Test
    public void whenClickOnCreateProducerTheDialogShouldBeVisible() {

        clickOn("#nodesList");
        clickOn("#producer");

        final CreateProducerJFXDialog createProducerJFXDialog = lookup("#createProducerJFXDialog").queryAs(CreateProducerJFXDialog.class);

        then(scheduledExecutorService).should().execute(any());

        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveZeroInteractions();
        then(serializationDataFormat).shouldHaveZeroInteractions();
        then(jmxClient).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveNoMoreInteractions();

        assertThat("dialog should be visible", createProducerJFXDialog::isVisible, is(true));
    }

    @Test
    public void whenCreateAConsumerThePaneShouldBeVisible() throws Exception {

        clickOn("#nodesList");
        clickOn("#consumer");

        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");
        lookup("#destination").queryAs(JFXTextField.class).setText(format("file:%s?fileName=queue.txt&autoCreate=true", folder.getRoot()));

        clickOn("#createConsumer");

        then(camelContext).should().getRouteDefinitions();
        then(camelContext).should().addRoutes(any());
        then(scheduledExecutorService).should().execute(any());

        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveNoMoreInteractions();
        then(serializationDataFormat).shouldHaveZeroInteractions();
        then(jmxClient).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveNoMoreInteractions();

        final AnchorPane consumerPane = lookup("#consumer-001").queryAs(AnchorPane.class);
        assertThat("consumer pane should be visible", consumerPane::isVisible, is(true));
    }

    @Test
    public void whenCloseAConsumerThePaneShouldBeRemoved() throws Exception {

        given(camelContext.removeRoute(any())).willReturn(true);

        clickOn("#nodesList");
        clickOn("#consumer");

        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");
        lookup("#destination").queryAs(JFXTextField.class).setText(format("file:%s?fileName=queue.txt&autoCreate=true", folder.getRoot()));

        clickOn("#createConsumer");

        final AnchorPane consumerPane = lookup("#consumer-001").queryAs(AnchorPane.class);
        assumeThat("consumer pane should be visible", consumerPane::isVisible, is(true));

        clickOn("#closeButton");

        then(camelContext).should().getRouteDefinitions();
        then(camelContext).should().addRoutes(any());
        then(camelContext).should().stopRoute(anyString());
        then(camelContext).should().removeRoute(anyString());
        then(scheduledExecutorService).should().execute(any());

        then(snackbar).shouldHaveZeroInteractions();
        then(camelContext).shouldHaveNoMoreInteractions();
        then(serializationDataFormat).shouldHaveZeroInteractions();
        then(jmxClient).shouldHaveZeroInteractions();
        then(scheduledExecutorService).shouldHaveNoMoreInteractions();

        assertThat("dialog should be visible", lookup("#consumer-001")::query, nullValue());
    }
}