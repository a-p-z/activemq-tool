package apz.activemq.controller;

import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Queue;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.scene.Scene;
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

import static apz.activemq.Configuration.configureScheduledExecutorService;
import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.AssertUtils.assumeThat;
import static apz.activemq.utils.MockUtils.spyQueueViewMBean;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
        final List<QueueViewMBean> queueViewMBeans = spyQueueViewMBean(50L, 0L, 100L);
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        when(jmxClient.getQueues()).thenReturn(queueViewMBeans);

        // when
        clickOn("#refresh");

        // then
        verify(jmxClient).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 50 rows", table.getRoot()::getChildren, hasSize(50));
        assertThat("the first row should be 'queue.test.alabama'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alabama"));
    }

    @Test
    @Ignore("table not refresh when an element is removed from observable list")
    public void whenRemoveQueueAndClickOnRefreshTableShouldBeUpdated() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final List<QueueViewMBean> queueViewMBeans1 = spyQueueViewMBean(50L, 0L, 100L);
        final List<QueueViewMBean> queueViewMBeans2 = new ArrayList<>(spyQueueViewMBean(50L, 0L, 100L));
        queueViewMBeans2.remove(0);
        when(jmxClient.getQueues())
                .thenReturn(queueViewMBeans1)
                .thenReturn(queueViewMBeans2);
        initializeTable(table);

        // when
        clickOn("#refresh");

        // then
        verify(jmxClient, times(2)).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 49 rows", table.getRoot()::getChildren, hasSize(49));
        assertThat("the first row should be 'queue.test.alaska'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alaska"));
    }

    @Test
    public void whenAddQueueAndClickOnRefreshTableShouldBeUpdated() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final List<QueueViewMBean> queueViewMBeans1 = new ArrayList<>(spyQueueViewMBean(50L, 0L, 100L));
        final List<QueueViewMBean> queueViewMBeans2 = spyQueueViewMBean(50L, 0L, 100L);
        queueViewMBeans1.remove(0);
        when(jmxClient.getQueues())
                .thenReturn(queueViewMBeans1)
                .thenReturn(queueViewMBeans2);
        queuesController.refresh(null);

        // when
        clickOn("#refresh");

        // then
        verify(jmxClient, times(2)).getQueues();
        verifyNoMoreInteractions(jmxClient);
        assertThat("table should have 50 rows", table.getRoot()::getChildren, hasSize(50));
        assertThat("last row should be 'queue.test.alabama'", table.getRoot().getChildren().get(49).getValue().name::getValue, is("queue.test.alabama"));
    }

    private void initializeTable(final JFXTreeTableView<Queue> table) {
        queuesController.refresh(null);
        assumeThat("table should have 50 row", table.getRoot()::getChildren, hasSize(50));
    }
}