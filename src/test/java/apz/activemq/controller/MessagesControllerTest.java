package apz.activemq.controller;

import apz.activemq.component.SimpleSnackbar;
import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Message;
import apz.activemq.model.Queue;
import apz.activemq.utils.ActiveMQJMXService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
import static apz.activemq.utils.AssertUtils.retry;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static javafx.application.Platform.runLater;
import static javafx.scene.input.DataFormat.PLAIN_TEXT;
import static javafx.scene.input.KeyCode.C;
import static javafx.scene.input.KeyCode.CONTROL;
import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.SHIFT;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class MessagesControllerTest extends ApplicationTest {

    private static final ActiveMQJMXService ACTIVE_MQJMX_SERVICE = new ActiveMQJMXService();
    private static final QueueViewMBean QUEUE = ACTIVE_MQJMX_SERVICE.getQueues().stream()
            .filter(q -> q.getQueueSize() > 0).findAny().orElseThrow(() -> new AssertionError("all queue are empty"));
    private static final int QUEUE_SIZE = QUEUE.getQueueSize() <= QUEUE.getMaxPageSize() ? (int) QUEUE.getQueueSize() : QUEUE.getMaxPageSize();

    @Mock
    private JmxClient jmxClient;

    @Mock
    private QueuesController queuesController;

    @Mock
    private SimpleSnackbar snackbar;

    private MessagesController messagesController;

    @Before
    public void before() {
        given(jmxClient.getQueues()).willReturn(ACTIVE_MQJMX_SERVICE.getQueues());
    }

    @Override
    public void start(final Stage stage) {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);

        clearRegistry();
        register("jmxClient", jmxClient);
        register("snackbar", snackbar);
        configureObjectMapper();
        configureScheduledExecutorService();
        configureMessageToStringConverter(get("objectMapper", ObjectMapper.class));

        messagesController = newInstance(MessagesController.class);
        messagesController.setQueue(new Queue(QUEUE));
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
        then(jmxClient).shouldHaveZeroInteractions();
        then(queuesController).should().addChild(any());
        then(queuesController).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("separator should be '<'", lookup("#separator").queryLabeled()::getText, is("<"));
    }

    @Test
    public void whenMouseExitedFromQueueNameSeparatorShouldChange() {
        // when
        moveTo("#queueName")
                .moveTo("#title");

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(queuesController).should().addChild(any());
        then(queuesController).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("separator should be '>'", lookup("#separator").queryLabeled()::getText, is(">"));
    }

    @Test
    public void whenClickOnQueueNameMessagesViewShouldBeRemoved() {
        // when
        clickOn("#queueName");

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(queuesController).should().addChild(any());
        then(queuesController).should().removeChild(any());
        then(queuesController).shouldHaveNoMoreInteractions();
        then(snackbar).shouldHaveZeroInteractions();
    }

    @Test
    public void whenClickOnRefreshTableShouldBePopulated() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();

        // when
        clickOn("#refresh");

        // then
        final Supplier<String> getFirstRowMessageId = () -> table.getRoot().getChildren().get(0).getValue().id.getValue();
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table should have " + QUEUE_SIZE + " rows", table.getRoot()::getChildren, hasSize(QUEUE_SIZE));
        assertThat("the id of the first row should start with 'ID:producer.test.com'", getFirstRowMessageId, startsWith("ID:producer.test.com"));
        assertThat("footer should be 'Showing " + QUEUE_SIZE + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText,
                is("Showing " + QUEUE_SIZE + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
    }

    @Test
    public void whenSortTableAndValuesChangeSortShouldBeMaintained() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn("#messageId.column-header");
        clickOn("#refresh");

        // then
        final Function<Integer, String> messageId = i -> table.getRoot().getChildren().get(i).getValue().id.getValue();
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table should have " + QUEUE_SIZE + " rows", table.getRoot()::getChildren, hasSize(QUEUE_SIZE));
        assertThat("footer should be 'Showing " + QUEUE_SIZE + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText,
                is("Showing " + QUEUE_SIZE + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
        IntStream.range(0, 41).boxed().forEach(i -> {
            final Supplier<Integer> compare = () -> table.getRoot().getChildren().get(i).getValue().id.getValue()
                    .compareTo(table.getRoot().getChildren().get(i + 1).getValue().id.getValue());
            assertThat("id '" + messageId.apply(i) + "' should less or equal than id '" + messageId.apply(i + 1) + "'", compare, lessThanOrEqualTo(0));
        });
    }

    @Test
    public void whenSearchOneQueueOneResultShouldBeShown() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        final String messageId = table.getRoot().getChildren().get(10).getValue().id.getValue();
        clickOn("#search")
                .write(messageId.substring(26, 39));

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table should have 1 row", table.getRoot()::getChildren, hasSize(1));
        assertThat("the first row should be '" + messageId + "'", table.getRoot().getChildren().get(0).getValue().id::getValue, is(messageId));
        assertThat("footer should be 'Showing 1 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText,
                is("Showing 1 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
    }

    @Test
    public void whenSearchIsGenericMultipleResultShouldBeShown() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn("#search")
                .write("NON-PERSISTENT");

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table current items count should be less then " + QUEUE_SIZE + "", table::getCurrentItemsCount, lessThan(QUEUE_SIZE));
        assertThat("footer should be 'Showing " + table.getCurrentItemsCount() + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText, is("Showing " + table.getCurrentItemsCount() + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
        IntStream.range(0, table.getCurrentItemsCount()).boxed().forEach(i -> {
            final String messageId = table.getRoot().getChildren().get(i).getValue().id.getValue();
            final Supplier<String> mode = () -> table.getRoot().getChildren().get(i).getValue().mode.getValue();
            assertThat("mode of message '" + messageId + "' should 'NON-PERSISTENT'", mode, is("NON-PERSISTENT"));
        });
    }

    @Test
    public void whenSearchSortAndRefreshFilterShouldBeAppliedAndSortMaintained() {
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
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table current items count should be less then " + QUEUE_SIZE + "", table::getCurrentItemsCount, lessThan(QUEUE_SIZE));
        assertThat("footer should be 'Showing " + table.getCurrentItemsCount() + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText, is("Showing " + table.getCurrentItemsCount() + " of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
        final int currentIntemCount = table.getCurrentItemsCount();
        IntStream.range(0, currentIntemCount).boxed().forEach(i -> {
            final String messageId = table.getRoot().getChildren().get(i).getValue().id.getValue();
            final Supplier<String> mode = () -> table.getRoot().getChildren().get(i).getValue().mode.getValue();
            assertThat("mode of message '" + messageId + "' should 'NON-PERSISTENT'", mode, is("NON-PERSISTENT"));
        });
    }

    @Test
    public void whenClickShowAllAllColumnsShouldBeVisible() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);

        // when
        scrollToLastColumn(table);
        rightClickOn("#add.column-header")
                .clickOn("#showAll");

        // then
        final Supplier<List<TreeTableColumn<Message, ?>>> visibleColumns = () -> table.getColumns().stream().filter(TableColumnBase::isVisible).collect(collectingAndThen(toList(), Collections::unmodifiableList));
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("visible columns should be 21", visibleColumns, hasSize(21));
    }

    @Test
    public void whenClickHideAllOnlyMessageIdColumnShouldBeVisible() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);

        // when
        scrollToLastColumn(table);
        rightClickOn("#add.column-header")
                .clickOn("#showAll");

        scrollToLastColumn(table);
        rightClickOn("#add.column-header")
                .clickOn("#hideAll");

        // then
        final Supplier<List<TreeTableColumn<Message, ?>>> visibleColumns = () -> table.getColumns().stream().filter(TableColumnBase::isVisible).collect(collectingAndThen(toList(), Collections::unmodifiableList));
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("visible columns should be 2", visibleColumns, hasSize(2));
        assertThat("first column should be 'Message id", visibleColumns.get().get(0)::getText, is("Message id"));
        assertThat("second column should be '+'", visibleColumns.get().get(1)::getText, is("+"));
    }

    @Test
    public void whenClickHideModeColumnItShouldNotBeVisible() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);
        final int initialVisibleColumnSize = (int) table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .count();

        // when
        rightClickOn("#mode.column-header")
                .clickOn("#hide");

        // then
        final Supplier<List<TreeTableColumn<Message, ?>>> visibleColumns2 = () -> table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("visible columns should be " + (initialVisibleColumnSize - 1), visibleColumns2, hasSize(initialVisibleColumnSize - 1));
        assertThat("table should not contain 'Mode' column", () -> visibleColumns2.get().stream().map(TableColumnBase::getText).collect(toList()), not(hasItem("Mode")));
    }

    @Test
    public void whenClickHideAndClickShowModeColumnItShouldBeVisible() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);
        final int initialVisibleColumnSize = (int) table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .count();

        // when
        rightClickOn("#mode.column-header")
                .clickOn("#hide");
        scrollToLastColumn(table);
        rightClickOn("#add.column-header")
                .moveBy(10, 260)
                .clickOn();

        // then
        final Supplier<List<TreeTableColumn<Message, ?>>> visibleColumns2 = () -> table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("visible columns should be " + (initialVisibleColumnSize), visibleColumns2, hasSize(initialVisibleColumnSize));
        assertThat("table should not contain 'Mode' column", () -> visibleColumns2.get().stream().map(TableColumnBase::getText).collect(toList()), hasItem("Mode"));
    }

    @Test
    public void whenClickShowBodyColumnBodiesShouldBeVisible() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);
        final int initialVisibleColumnSize = (int) table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .count();

        // when
        retry(() -> scrollToLastColumn(table));
        rightClickOn("#add.column-header")
                .moveBy(10, 100)
                .clickOn();

        // then
        final Supplier<List<TreeTableColumn<Message, ?>>> visibleColumns2 = () -> table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("visible columns should be " + (initialVisibleColumnSize + 1), visibleColumns2, hasSize(initialVisibleColumnSize + 1));
        assertThat("new column should be 'Body'", visibleColumns2.get().get(initialVisibleColumnSize - 1)::getText, is("Body"));
    }

    @Test
    public void whenSearchTheValueOfAHiddenColumnNoResultShouldBeReturned() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        final String value = table.getRoot().getChildren().get(10).getValue().body.getValue();
        clickOn("#search")
                .write(value);

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table current items count should be 0", table::getCurrentItemsCount, is(0));
        assertThat("footer should be 'Showing 0 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText, is("Showing 0 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
    }

    @Test
    public void whenShowAHiddenColumnAndSearchAValueOneResultShouldBeReturned() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        final String body = table.getRoot().getChildren().get(10).getValue().body.getValue();
        scrollToLastColumn(table);
        rightClickOn("#add.column-header")
                .moveBy(10, 100)
                .clickOn();
        clickOn("#search")
                .write(body);

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table current items count should be 1", table::getCurrentItemsCount, is(1));
        assertThat("footer should be 'Showing 1 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText, is("Showing 1 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
        assertThat("body of message should be '" + body + "'", table.getRoot().getChildren().get(0).getValue().body::getValue, is(body));
    }

    @Test
    public void whenClickShowUserColumnItShouldBeVisible() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);
        final int initialVisibleColumnSize = (int) table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .count();

        // when
        scrollToLastColumn(table);
        rightClickOn("#add.column-header")
                .moveBy(10, 340)
                .clickOn();

        // then
        final Supplier<List<TreeTableColumn<Message, ?>>> visibleColumns2 = () -> table.getColumns().stream()
                .filter(TableColumnBase::isVisible)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("visible columns should be " + (initialVisibleColumnSize + 1), visibleColumns2, hasSize(initialVisibleColumnSize + 1));
        assertThat("new column should be 'exceptionType'", visibleColumns2.get().get(initialVisibleColumnSize - 1)::getText, is("jobId"));
    }

    @Test
    public void whenShowAUserColumnAndSearchAValueOneResultShouldBeReturned() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        final String jobId = (String) table.getRoot().getChildren().get(10).getValue().messageUserProperties.getValue().get("jobId");
        scrollToLastColumn(table);
        rightClickOn("#add.column-header")
                .moveBy(10, 340)
                .clickOn();
        clickOn("#search")
                .write(jobId);

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table current items count should be 1", table::getCurrentItemsCount, is(1));
        assertThat("footer should be 'Showing 1 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText, is("Showing 1 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
        assertThat("jobId of message should be '" + jobId + "'", () -> table.getRoot().getChildren().get(0).getValue().messageUserProperties.getValue().get("jobId"), is(jobId));
    }

    @Test
    public void whenSearchAValueOfAHiddenUserColumnNoResultShouldBeReturned() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        final String jobId = (String) table.getRoot().getChildren().get(10).getValue().messageUserProperties.getValue().get("jobId");
        clickOn("#search")
                .write(jobId);

        // then
        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).shouldHaveZeroInteractions();
        assertThat("table current items count should be 0", table::getCurrentItemsCount, is(0));
        assertThat("footer should be 'Showing 0 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)'", footer::getText, is("Showing 0 of " + QUEUE_SIZE + " (messages are limited by browser page size 400)"));
    }

    @Test
    public void whenDeleteMessagesTheyShouldBeRemovedFromTable() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn(table)
                .press(SHIFT)
                .moveBy(0, 90)
                .clickOn()
                .release(SHIFT);

        rightClickOn()
                .clickOn("#delete");

        retry(() -> clickOn("#confirm"));

        // then
        retry(() -> {
            then(jmxClient).shouldHaveZeroInteractions();
            then(snackbar).should().warn(any());
            then(snackbar).shouldHaveNoMoreInteractions();
        });
        assertThat("table current items count should be less than " + QUEUE_SIZE, table::getCurrentItemsCount, lessThan(QUEUE_SIZE));
    }

    @Test
    public void whenCopyMessagesTheyShouldBeCopiedToDestination() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);

        clickOn(table)
                .press(SHIFT)
                .moveBy(0, 90)
                .clickOn()
                .release(SHIFT);

        rightClickOn()
                .clickOn("#copyTo");

        clickOn("#autoCompleteJFXComboBox")
                .write("california")
                .moveBy(-120, 30)
                .clickOn()
                .clickOn("#select");
        clickOn("#confirm");

        // then
        then(jmxClient).should().getQueues();
        then(snackbar).should().warn(any());
        then(snackbar).shouldHaveNoMoreInteractions();
    }

    @Test
    public void whenMoveMessagesTheyShouldBeMovedToDestinationAndRemovedFromTable() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        final Label footer = lookup("#footer").query();
        initializeTable(table);

        // when
        clickOn(table)
                .press(SHIFT)
                .moveBy(0, 90)
                .clickOn()
                .release(SHIFT);
        rightClickOn()
                .clickOn("#moveTo");

        clickOn("#autoCompleteJFXComboBox")
                .write("california")
                .moveBy(-120, 30)
                .clickOn();
        clickOn("#select");
        clickOn("#confirm");

        // then
        retry(() -> {
            try {
                then(jmxClient).should().getQueues();
                then(jmxClient).shouldHaveNoMoreInteractions();
                then(snackbar).should().warn(any());
                then(snackbar).shouldHaveNoMoreInteractions();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertThat("table current items count should be less than " + QUEUE_SIZE, table::getCurrentItemsCount, lessThan(QUEUE_SIZE));
    }

    @Test
    public void whenCopyMessagesTheyShouldBeCopiedToClipboard() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);

        // when
        clickOn(table.getChildrenUnmodifiable().get(1))
                .press(SHIFT)
                .push(DOWN)
                .push(DOWN)
                .release(SHIFT)
                .rightClickOn()
                .clickOn("#copyToClipboard");

        // then
        final AtomicInteger linesNum = new AtomicInteger(-1);

        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).should().info(any());
        then(snackbar).shouldHaveNoMoreInteractions();
        runLater(() -> linesNum.set(Clipboard.getSystemClipboard().getContent(PLAIN_TEXT).toString().split("\n").length));
        assertThat("clipboard should contain three lines", linesNum::get, is(3));
    }

    @Test
    public void whenPushControlCSelectedMessagesShouldBeCopiedToClipboard() {
        // given
        final JFXTreeTableView<Message> table = lookup("#table").query();
        initializeTable(table);

        // when
        clickOn(table.getChildrenUnmodifiable().get(1))
                .press(SHIFT)
                .push(DOWN)
                .push(DOWN)
                .release(SHIFT);
        push(CONTROL, C);

        // then
        final AtomicInteger linesNum = new AtomicInteger(-1);

        then(jmxClient).shouldHaveZeroInteractions();
        then(snackbar).should().info(any());
        then(snackbar).shouldHaveNoMoreInteractions();
        runLater(() -> {
            linesNum.set(Clipboard.getSystemClipboard().getContent(PLAIN_TEXT).toString().split("\n").length);
            assertThat("clipboard should contain three lines", linesNum::get, is(3));
        });
    }

    private void initializeTable(final JFXTreeTableView<Message> table) {
        messagesController.refresh(null);
        assumeThat("table should have " + QUEUE_SIZE + " row", table.getRoot()::getChildren, hasSize(QUEUE_SIZE));
    }

    private void scrollToLastColumn(final JFXTreeTableView<Message> table) {
        retry(() -> {
            runLater(() -> table.scrollToColumnIndex(table.getColumns().size() - 1));
            clickOn("#add.column-header");
        });
    }
}
