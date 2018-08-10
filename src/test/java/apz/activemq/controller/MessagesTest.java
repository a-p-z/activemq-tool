package apz.activemq.controller;

import apz.activemq.model.Queue;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.MockUtils.spyQueueViewMBean;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MessagesTest extends ApplicationTest {

    private final QueueViewMBean queueViewBean = spyQueueViewMBean(1L, 42L, 43L).get(0);

    @Mock
    private QueuesController queuesController;

    private MessagesController messagesController;

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

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
        verify(queuesController).addChild(any());
        verifyNoMoreInteractions(queuesController);
        assertThat("separator should be '>'", lookup("#separator").queryLabeled()::getText, is(">"));
    }

    @Test
    public void whenClickOnQueueNameMessagesViewShouldBeRemoved() {
        // when
        clickOn("#queueName");

        // then
        verify(queuesController).addChild(any());
        verify(queuesController).removeChild(any());
        verifyNoMoreInteractions(queuesController);
    }
}
