package apz.activemq.controller;

import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Queue;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static apz.activemq.Configuration.configureScheduledExecutorService;
import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.AssertUtils.assumeThat;
import static apz.activemq.utils.MockUtils.spyQueueViewMBean;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.Silent.class)
public class QueuesTest extends ApplicationTest {

    @Mock
    private JmxClient jmxClient;

    private QueuesController queuesController;

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("jmxClient", jmxClient);
        configureScheduledExecutorService();

        queuesController = newInstance(QueuesController.class);

        stackPane.getChildren().add(queuesController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void whenClickOnRefreshTableShouldBePopulated() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        final List<QueueViewMBean> queueViewMBeans = spyQueueViewMBean(50L, 0L, 100L);
        given(jmxClient.getQueues()).willReturn(queueViewMBeans);

        // when
        clickOn("#refresh");

        // then
        verify(jmxClient).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 50 rows", table.getRoot()::getChildren, hasSize(50));
        assertThat("footer should be 'Showing 50 of 50 queues'", footer::getText, is("Showing 50 of 50 queues"));
        assertThat("the first row should be 'queue.test.alabama'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alabama"));
    }

    @Test
    @Ignore("table not refresh when an element is removed from observable list")
    public void whenRemoveQueueAndClickOnRefreshTableShouldBeUpdated() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        final List<QueueViewMBean> queueViewMBeans1 = spyQueueViewMBean(50L, 0L, 100L);
        final List<QueueViewMBean> queueViewMBeans2 = new ArrayList<>(spyQueueViewMBean(50L, 0L, 100L));
        queueViewMBeans2.remove(0);
        given(jmxClient.getQueues())
                .willReturn(queueViewMBeans1)
                .willReturn(queueViewMBeans2);
        initializeTable(table);

        // when
        clickOn("#refresh");

        // then
        verify(jmxClient, times(2)).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 49 rows", table.getRoot()::getChildren, hasSize(49));
        assertThat("footer should be 'Showing 49 of 50 queues'", footer::getText, is("Showing 49 of 50 queues"));
        assertThat("the first row should be 'queue.test.alaska'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alaska"));
    }

    @Test
    public void whenAddQueueAndClickOnRefreshTableShouldBeUpdated() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        final List<QueueViewMBean> queueViewMBeans1 = new ArrayList<>(spyQueueViewMBean(50L, 0L, 100L));
        final List<QueueViewMBean> queueViewMBeans2 = spyQueueViewMBean(50L, 0L, 100L);
        queueViewMBeans1.remove(0);
        given(jmxClient.getQueues())
                .willReturn(queueViewMBeans1)
                .willReturn(queueViewMBeans2);
        queuesController.refresh(null);

        // when
        clickOn("#refresh");

        // then
        verify(jmxClient, times(2)).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 50 rows", table.getRoot()::getChildren, hasSize(50));
        assertThat("footer should be 'Showing 50 of 50 queues'", footer::getText, is("Showing 50 of 50 queues"));
        assertThat("last row should be 'queue.test.alabama'", table.getRoot().getChildren().get(49).getValue().name::getValue, is("queue.test.alabama"));
    }

    @Test
    public void whenSortTableAndValuesChangeSortShouldBeMaintained() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        final List<QueueViewMBean> queueViewMBeans1 = spyQueueViewMBean(50L, 0L, 100L);
        final List<QueueViewMBean> queueViewMBeans2 = spyQueueViewMBean(50L, 0L, 100L);
        given(jmxClient.getQueues())
                .willReturn(queueViewMBeans1)
                .willReturn(queueViewMBeans2);
        initializeTable(table);

        // when
        clickOn("#pending.column-header");
        clickOn("#refresh");

        // then
        final Function<Integer, String> queue = i -> table.getRoot().getChildren().get(i).getValue().name.getValue();
        verify(jmxClient, times(2)).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("footer should be 'Showing 50 of 50 queues'", footer::getText, is("Showing 50 of 50 queues"));
        assertThat("table should have 50 rows", table.getRoot()::getChildren, hasSize(50));
        IntStream.range(0, 49).boxed().forEach(i -> {
            final Supplier<Boolean> compare = () -> table.getRoot().getChildren().get(i).getValue().pending.getValue() <=
                    table.getRoot().getChildren().get(i + 1).getValue().pending.getValue();
            assertThat("pending messages of '" + queue.apply(i) + "' should less or equal than pending messages of '" + queue.apply(i + 1) + "'",
                    compare, is(true));
        });
    }

    @Test
    public void whenSearchOneQueueOneResultShouldBeShown() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        final List<QueueViewMBean> queueViewMBeans = spyQueueViewMBean(50L, 0L, 100L);
        given(jmxClient.getQueues()).willReturn(queueViewMBeans);
        initializeTable(table);

        // when
        clickOn("#search")
                .write("alaska");

        // then
        verify(jmxClient).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 1 row", table.getRoot()::getChildren, hasSize(1));
        assertThat("footer should be 'Showing 1 of 50 queues'", footer::getText, is("Showing 1 of 50 queues"));
        assertThat("the first row should be 'queue.test.alaska'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alaska"));
    }

    @Test
    public void whenSearchIsGenericMultipleResultShouldBeShown() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        final List<QueueViewMBean> queueViewMBeans = spyQueueViewMBean(50L, 0L, 100L);
        given(jmxClient.getQueues()).willReturn(queueViewMBeans);
        initializeTable(table);

        // when
        clickOn("#search")
                .write("al");

        // then
        verify(jmxClient).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 3 row", table.getRoot()::getChildren, hasSize(3));
        assertThat("footer should be 'Showing 3 of 50 queues'", footer::getText, is("Showing 3 of 50 queues"));
        assertThat("the first row should be 'queue.test.alabama'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alabama"));
        assertThat("the second row should be 'queue.test.alaska'", table.getRoot().getChildren().get(1).getValue().name::getValue, is("queue.test.alaska"));
        assertThat("the third row should be 'queue.test.california'", table.getRoot().getChildren().get(2).getValue().name::getValue, is("queue.test.california"));
    }

    @Test
    public void whenSearchSortAndRefreshFilterShouldBeAppliedAndSortMaintained() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        final List<QueueViewMBean> queueViewMBeans1 = new ArrayList<>(spyQueueViewMBean(50L, 0L, 100L));
        final List<QueueViewMBean> queueViewMBeans2 = spyQueueViewMBean(50L, 0L, 100L);
        queueViewMBeans1.remove(0);
        given(jmxClient.getQueues())
                .willReturn(queueViewMBeans1)
                .willReturn(queueViewMBeans2);
        queuesController.refresh(null);

        // when
        clickOn("#search")
                .write("test.a");
        clickOn("#pending.column-header");
        clickOn("#refresh");

        // then
        final Function<Integer, Queue> queue = i -> table.getRoot().getChildren().get(i).getValue();
        verify(jmxClient, times(2)).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 4 row", table.getRoot()::getChildren, hasSize(4));
        assertThat("footer should be 'Showing 4 of 50 queues'", footer::getText, is("Showing 4 of 50 queues"));
        assertThat("the first row should contain 'test.a'", table.getRoot().getChildren().get(0).getValue().name::getValue, containsString("test.a"));
        assertThat("the second row should contain 'test.a'", table.getRoot().getChildren().get(1).getValue().name::getValue, containsString("test.a"));
        assertThat("the third row should contain 'test.a'", table.getRoot().getChildren().get(2).getValue().name::getValue, containsString("test.a"));
        assertThat("the fourth row should contain 'test.a'", table.getRoot().getChildren().get(3).getValue().name::getValue, containsString("test.a"));
        IntStream.range(0, 3).boxed().forEach(i -> {
            final Supplier<Long> pending = () -> queue.apply(i + 1).pending.getValue() - queue.apply(i).pending.getValue();
            assertThat("pending messages of '" + queue.apply(i).name.getValue() + "' should less or equal than pending messages of '" + queue.apply(i + 1).name.getValue() + "'", pending, greaterThanOrEqualTo(0L));
        });
    }

    private void initializeTable(final JFXTreeTableView<Queue> table) {
        queuesController.refresh(null);
        assumeThat("table should have 50 row", table.getRoot()::getChildren, hasSize(50));
    }
}