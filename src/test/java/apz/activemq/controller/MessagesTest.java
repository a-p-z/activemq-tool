package apz.activemq.controller;

import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Message;
import apz.activemq.model.Queue;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import javax.management.openmbean.OpenDataException;

import java.util.function.Supplier;

import static apz.activemq.Configuration.configureScheduledExecutorService;
import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.MockUtils.spyQueueViewMBean;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MessagesTest extends ApplicationTest {

    private final QueueViewMBean queueViewBean = spyQueueViewMBean(1L, 42L, 43L).get(0);

    @Mock
    private JmxClient jmxClient;

    @Mock
    private QueuesController queuesController;

    private MessagesController messagesController;

    @Override
    public void start(final Stage stage) {


        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("jmxClient", jmxClient);
        configureScheduledExecutorService();

        messagesController = newInstance(MessagesController.class);
        messagesController.setQueue(new Queue(queueViewBean));
        messagesController.setParent(queuesController);
        stackPane.getChildren().add(messagesController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void whenMouseEnteredInQueueNameSeparatorShouldChange() {
        // when
        moveTo("#queueName");

        // then
        verify(queueViewBean).getName();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        verify(queuesController).addChild(any());
        verifyNoMoreInteractions(queuesController);
        assertThat("separator should be '<'", lookup("#separator").queryLabeled()::getText, is("<"));
    }

    @Test
    public void whenMouseExitedFromQueueNameSeparatorShouldChange() {
        // when
        moveTo("#queueName")
                .moveTo("#title");

        // then
        verify(queueViewBean).getName();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        verify(queuesController).addChild(any());
        verifyNoMoreInteractions(queuesController);
        assertThat("separator should be '>'", lookup("#separator").queryLabeled()::getText, is(">"));
    }

    @Test
    public void whenClickOnQueueNameMessagesViewShouldBeRemoved() {
        // when
        clickOn("#queueName");

        // then
        verify(queueViewBean).getName();
        verify(queueViewBean).getQueueSize();
        verify(queueViewBean).getConsumerCount();
        verify(queueViewBean).getEnqueueCount();
        verify(queueViewBean).getDequeueCount();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        verify(queuesController).addChild(any());
        verify(queuesController).removeChild(any());
        verifyNoMoreInteractions(queuesController);
    }

    @Test
    public void whenClickOnRefreshTableShouldBePopulated() throws OpenDataException {
        // when
        clickOn("#refresh");

        // then
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Supplier<String> getFirstRowMessageId = () -> table.getRoot().getChildren().get(0).getValue().id.getValue();
        verify(queueViewBean).getName();
        verify(queueViewBean).browse();
        verifyNoMoreInteractions(queueViewBean);
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 42 rows", table.getRoot()::getChildren, hasSize(42));
        assertThat("the id of the first row should start with 'ID:producer.test.com'", getFirstRowMessageId, startsWith("ID:producer.test.com"));
    }
}
