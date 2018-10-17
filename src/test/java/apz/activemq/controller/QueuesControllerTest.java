package apz.activemq.controller;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Queue;
import apz.activemq.utils.ActiveMQJMXService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static apz.activemq.Configuration.configureMessageToStringConverter;
import static apz.activemq.Configuration.configureObjectMapper;
import static apz.activemq.Configuration.configureScheduledExecutorService;
import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.get;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.utils.AssertUtils.assertThat;
import static apz.activemq.utils.AssertUtils.assumeThat;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.scene.input.DataFormat.PLAIN_TEXT;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.CONTROL;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class QueuesControllerTest extends ApplicationTest {

    private static final ActiveMQJMXService ACTIVE_MQJMX_SERVICE = new ActiveMQJMXService();

    @Mock
    private JmxClient jmxClient;

    @Mock
    private SimpleSnackbar snackbar;

    private QueuesController queuesController;

    @Before
    public void before() {
        given(jmxClient.getBroker()).willReturn(ACTIVE_MQJMX_SERVICE.getBroker());
        given(jmxClient.getQueues()).willReturn(ACTIVE_MQJMX_SERVICE.getQueues());
    }

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        configureObjectMapper();
        register("jmxClient", jmxClient);
        register("snackbar", snackbar);
        configureScheduledExecutorService();
        configureMessageToStringConverter(get("objectMapper", ObjectMapper.class));

        queuesController = newInstance(QueuesController.class);

        stackPane.getChildren().add(queuesController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }



    @Test
 //   @Ignore("table not refresh when an element is removed from observable list")
    public void whenRemoveQueueAndClickOnRefreshTableShouldBeUpdated() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        ACTIVE_MQJMX_SERVICE.deleteQuery("queue.test.alabama");
        clickOn("#refresh");

        // then
        then(jmxClient).should(times(2)).getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table should have 49 rows", ACTIVE_MQJMX_SERVICE::getQueues, hasSize(49));
        assertThat("table should have 49 rows", table.getRoot()::getChildren, hasSize(49));
        //assertThat("footer should be 'Showing 49 of 49 queues'", footer::getText, is("Showing 49 of 49 queues"));
        assertThat("the first row should be 'queue.test.alaska'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alaska"));
        ACTIVE_MQJMX_SERVICE.createQuery("queue.test.alabama");
        clickOn("#refresh");
    }

    @Test
    public void whenAddQueueAndClickOnRefreshTableShouldBeUpdated() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        queuesController.refresh(null);

        // when
        ACTIVE_MQJMX_SERVICE.createQuery("queue.new.oregon");
        clickOn("#refresh");
        clickOn("#search")
                .write("queue.new.oregon");

        // then
        then(jmxClient).should(times(2)).getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table should have 51 rows", table.getRoot()::getChildren, hasSize(1));
        assertThat("footer should be 'Showing 1 of 51 queues'", footer::getText, is("Showing 1 of 51 queues"));
        ACTIVE_MQJMX_SERVICE.deleteQuery("queue.new.oregon");
        clickOn("#refresh");
    }

    @Test
    public void whenSortTableAndValuesChangeSortShouldBeMaintained() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn("#pending.column-header");
        clickOn("#refresh");

        // then
        final Function<Integer, String> queue = i -> table.getRoot().getChildren().get(i).getValue().name.getValue();
        then(jmxClient).should(times(2)).getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
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
        initializeTable(table);

        // when
        clickOn("#search")
                .write("alaska");

        // then
        then(jmxClient).should().getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table should have 1 row", table.getRoot()::getChildren, hasSize(1));
        assertThat("footer should be 'Showing 1 of 50 queues'", footer::getText, is("Showing 1 of 50 queues"));
        assertThat("the first row should be 'queue.test.alaska'", table.getRoot().getChildren().get(0).getValue().name::getValue, is("queue.test.alaska"));
    }

    @Test
    public void whenSearchIsGenericMultipleResultShouldBeShown() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn("#search")
                .write("al");
        clickOn("#pending.column-header");

        // then
        then(jmxClient).should().getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
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
        queuesController.refresh(null);

        // when
        clickOn("#search")
                .write("test.a");
        clickOn("#pending.column-header");
        clickOn("#refresh");

        // then
        final Function<Integer, Queue> queue = i -> table.getRoot().getChildren().get(i).getValue();
        then(jmxClient).should(times(2)).getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
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

    @Test
    public void whenClickOnPurgeQueueShouldBePurged() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        initializeTable(table);

        // when
        rightClickOn(table.getChildrenUnmodifiable().get(1))
                .clickOn("#purge");
        clickOn("#confirm");

        // then
        then(jmxClient).should().getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).should().info(any());
        then(snackbar).shouldHaveNoMoreInteractions();
    }

    @Test
    public void whenClickOnDeleteQueueShouldBeDeleted() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        initializeTable(table);

        // when
        rightClickOn(table.getChildrenUnmodifiable().get(1))
                .clickOn("#delete");
        clickOn("#confirm");

        // then
        then(jmxClient).should().getQueues();
        then(jmxClient).should().getBroker();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).should().info(any());
        then(snackbar).shouldHaveNoMoreInteractions();
    }

    @Test
    public void whenClickOnBrowseMessagesShouldBeShown() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        initializeTable(table);

        // when
        rightClickOn(table.getChildrenUnmodifiable().get(1))
                .clickOn("#browse");

        // then
        then(jmxClient).should().getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("titles should not be null", lookup("#title")::queryAll, notNullValue());
        assertThat("titles should contain QUEUES ad MESSAGES",
                () -> lookup("#title").queryAllAs(Label.class).stream().map(Labeled::getText).collect(toList()),
                containsInAnyOrder("QUEUES", "MESSAGES"));
    }

    @Test
    public void whenDoubleClickOnOnARowBrowseQueue() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        initializeTable(table);

        // when
        doubleClickOn(table.getChildrenUnmodifiable().get(1));

        // then
        then(jmxClient).should().getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("titles should not be null", lookup("#title")::queryAll, notNullValue());
        assertThat("titles should contain QUEUES ad MESSAGES",
                () -> lookup("#title").queryAllAs(Label.class).stream().map(Labeled::getText).collect(toList()),
                containsInAnyOrder("QUEUES", "MESSAGES"));
    }

    @Test
    public void whenPressControlCSelectedQueueShouldBeCopiedToClipboard() {
        // given
        final JFXTreeTableView<Queue> table = lookup("#table").query();
        initializeTable(table);

        // when
        clickOn(table.getChildrenUnmodifiable().get(1));
        press(CONTROL, C).release(CONTROL, C);

        // then
        final AtomicReference<String> content = new AtomicReference<>();
        then(jmxClient).should().getQueues();
        then(jmxClient).shouldHaveNoMoreInteractions();
        then(snackbar).should().info(any());
        then(snackbar).shouldHaveNoMoreInteractions();
        runLater(() -> content.set(Clipboard.getSystemClipboard().getContent(PLAIN_TEXT).toString()));
        assertThat("clipboard should contain the queue", content::get, startsWith("Queue{name=queue.test."));
    }

    private void initializeTable(final JFXTreeTableView<Queue> table) {
        queuesController.refresh(null);
        assumeThat("table should have 50 row", table.getRoot()::getChildren, hasSize(50));
    }
}