package apz.activemq.controller;

import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Message;
import apz.activemq.model.Queue;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import javax.management.openmbean.OpenDataException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static apz.activemq.Configuration.configureScheduledExecutorService;
import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.MockUtils.spyQueueViewMBean;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
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
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
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
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
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
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        verify(queuesController).addChild(any());
        verify(queuesController).removeChild(any());
        verifyNoMoreInteractions(queuesController);
    }

    @Test
    public void whenClickOnRefreshTableShouldBePopulated() throws OpenDataException {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();

        // when
        clickOn("#refresh");

        // then
        final Supplier<String> getFirstRowMessageId = () -> table.getRoot().getChildren().get(0).getValue().id.getValue();
        verify(queueViewBean).getName();
        verify(queueViewBean).browse();
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        assertThat("table should have 42 rows", table.getRoot()::getChildren, hasSize(42));
        assertThat("the id of the first row should start with 'ID:producer.test.com'", getFirstRowMessageId, startsWith("ID:producer.test.com"));
        assertThat("footer should be 'Showing 42 of 42 (messages are limited by browser page size 400)'", footer::getText, is("Showing 42 of 42 (messages are limited by browser page size 400)"));
    }

    @Test
    public void whenSortTableAndValuesChangeSortShouldBeMaintained() throws OpenDataException {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn("#messageId.column-header");
        clickOn("#refresh");

        // then
        final Function<Integer, String> messageId = i -> table.getRoot().getChildren().get(i).getValue().id.getValue();
        verify(queueViewBean).getName();
        verify(queueViewBean, times(2)).browse();
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        assertThat("table should have 42 rows", table.getRoot()::getChildren, hasSize(42));
        assertThat("footer should be 'Showing 42 of 42 (messages are limited by browser page size 400)'", footer::getText, is("Showing 42 of 42 (messages are limited by browser page size 400)"));
        IntStream.range(0, 41).boxed().forEach(i -> {
            final Supplier<Integer> compare = () -> table.getRoot().getChildren().get(i).getValue().id.getValue()
                    .compareTo(table.getRoot().getChildren().get(i + 1).getValue().id.getValue());
            assertThat("id '" + messageId.apply(i) + "' should less or equal than id '" + messageId.apply(i + 1) + "'", compare, lessThanOrEqualTo(0));
        });
    }

    @Test
    public void whenSearchOneQueueOneResultShouldBeShown() throws OpenDataException {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        final String messageId = table.getRoot().getChildren().get(10).getValue().id.getValue();
        clickOn("#search")
                .write(messageId.substring(26, 39));

        // then
        verify(queueViewBean).getName();
        verify(queueViewBean).browse();
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        assertThat("table should have 1 row", table.getRoot()::getChildren, hasSize(1));
        assertThat("the first row should be '" + messageId + "'", table.getRoot().getChildren().get(0).getValue().id::getValue, is(messageId));
        assertThat("footer should be 'Showing 1 of 42 (messages are limited by browser page size 400)'", footer::getText, is("Showing 1 of 42 (messages are limited by browser page size 400)"));
    }

    @Test
    public void whenSearchIsGenericMultipleResultShouldBeShown() throws OpenDataException {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn("#search")
                .write("NON-PERSISTENT");

        // then
        verify(queueViewBean).getName();
        verify(queueViewBean).browse();
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        assertThat("table current items count should be less then 42", table::getCurrentItemsCount, lessThan(42));
        assertThat("footer should be 'Showing " + table.getCurrentItemsCount() + " of 42 (messages are limited by browser page size 400)'", footer::getText, is("Showing " + table.getCurrentItemsCount() + " of 42 (messages are limited by browser page size 400)"));
        IntStream.range(0, table.getCurrentItemsCount()).boxed().forEach(i -> {
            final String messageId = table.getRoot().getChildren().get(i).getValue().id.getValue();
            final Supplier<String> mode = () -> table.getRoot().getChildren().get(i).getValue().mode.getValue();
            assertThat("mode of message '" + messageId + "' should 'NON-PERSISTENT'", mode, is("NON-PERSISTENT"));
        });
    }

    @Test
    public void whenSearchSortAndRefreshFilterShouldBeAppliedAndSortMaintained() throws OpenDataException {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn("#search")
                .write("NON-PERSISTENT");
        clickOn("#messageId.column-header");
        clickOn("#refresh");

        // then
        verify(queueViewBean).getName();
        verify(queueViewBean, times(2)).browse();
        verify(queueViewBean, atLeast(1)).getMaxPageSize();
        verifyNoMoreInteractions(queueViewBean);
        verifyZeroInteractions(jmxClient);
        assertThat("table current items count should be less then 42", table::getCurrentItemsCount, lessThan(42));
        assertThat("footer should be 'Showing " + table.getCurrentItemsCount() + " of 42 (messages are limited by browser page size 400)'", footer::getText, is("Showing " + table.getCurrentItemsCount() + " of 42 (messages are limited by browser page size 400)"));
        IntStream.range(0, table.getCurrentItemsCount()).boxed().forEach(i -> {
            final String messageId = table.getRoot().getChildren().get(i).getValue().id.getValue();
            final Supplier<String> mode = () -> table.getRoot().getChildren().get(i).getValue().mode.getValue();
            assertThat("mode of message '" + messageId + "' should 'NON-PERSISTENT'", mode, is("NON-PERSISTENT"));
        });
    }

    private void initializeTable(final JFXTreeTableView<Message> table) {
        messagesController.refresh(null);
        assertThat("table should have 42 row", table.getRoot()::getChildren, hasSize(42));
    }
}
