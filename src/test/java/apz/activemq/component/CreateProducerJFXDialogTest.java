package apz.activemq.component;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.ValidationFacade;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static apz.activemq.utils.AssertUtils.assertThat;
import static java.util.stream.Collectors.joining;
import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.RIGHT;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class CreateProducerJFXDialogTest extends ApplicationTest {

    private final static boolean IS_MACOSX = System.getProperty("os.name").startsWith("Mac");

    final private StackPane container = new StackPane();

    @Rule
    public TemporaryFolder folder =  new TemporaryFolder(IS_MACOSX
            ? new File("/private/" + System.getProperty("java.io.tmpdir"))
            : new File(System.getProperty("java.io.tmpdir")));

    @Mock
    private BiConsumer<String, String> action;

    private CreateProducerJFXDialog dialog;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(container, 800, 580);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();

        dialog = new CreateProducerJFXDialog(container, action, folder.getRoot());
    }

    @Test
    public void whenCreateWithoutASourceErrorShouldBeShown() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        clickOn("#createProducer");

        final ValidationFacade validationFacade = lookup("#sourceValidationFacade").query();
        final Supplier<String> errors = () -> validationFacade.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));

        then(action).should(never()).accept(anyString(), anyString());

        assertThat("error message should be 'source uri is required'", errors,
                is("source uri is required"));
        assertThat("dialog should not be closed", closed::get, is(false));
    }

    @Test
    public void whenCreateWithoutADestinationErrorShouldBeShown() throws IOException {

        final File messages = folder.newFile("messages.txt");
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        lookup("#source").queryAs(JFXTextField.class).setText("stream:file?fileName=" + messages.getAbsolutePath());

        clickOn("#createProducer");

        final ValidationFacade validationFacade = lookup("#destinationValidationFacade").query();
        final Supplier<String> errors = () -> validationFacade.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));

        then(action).should(never()).accept(anyString(), anyString());

        assertThat("error message should be 'destination uri is required'", errors,
                is("destination uri is required"));
        assertThat("dialog should not be closed", closed::get, is(false));
    }

    @Test
    public void whenCreateWithANotValidSourceSchemaErrorShouldBeShown() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        lookup("#source").queryAs(JFXTextField.class).setText("file:/tmp/messages.txt");

        clickOn("#createProducer");

        final ValidationFacade validationFacade = lookup("#sourceValidationFacade").query();
        final Supplier<String> errors = () -> validationFacade.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));

        then(action).should(never()).accept(anyString(), anyString());

        assertThat("error message should be 'source uri is not valid: stream schema is required'", errors,
                is("source uri is not valid: stream schema is required"));
        assertThat("dialog should not be closed", closed::get, is(false));
    }

    @Test
    public void whenCreateWithANotValidDestinationSchemaErrorShouldBeShown() throws IOException {

        final File messages = folder.newFile("messages.txt");
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        lookup("#source").queryAs(JFXTextField.class).setText("stream:file?fileName=" + messages.getAbsolutePath());
        lookup("#destination").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("invalid:queue");

        clickOn("#createProducer");

        final ValidationFacade validationFacade = lookup("#destinationValidationFacade").query();
        final Supplier<String> errors = () -> validationFacade.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));

        then(action).should(never()).accept(anyString(), anyString());

        assertThat("error message should be 'destination uri is not valid: jms or activemq schema is required'", errors,
                is("destination uri is not valid: jms or activemq schema is required"));
        assertThat("dialog should not be closed", closed::get, is(false));
    }

    @Test
    public void whenCancelDialogShouldBeClosed() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        clickOn("#cancel");

        then(action).should(never()).accept(anyString(), anyString());

        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenCreateWithValidSourceAndDestinationActionShouldBeRun() throws IOException {

        final File queue = folder.newFile("queue.txt");
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        lookup("#source").queryAs(JFXTextField.class).setText("stream:file?fileName=" + queue.getAbsolutePath());
        lookup("#destination").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");

        clickOn("#createProducer");

        then(action).should().accept("stream:file?fileName=" + queue.getAbsolutePath(), "jms:queue");

        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenCreateOpeningFileChooserActionShouldBeRun() throws IOException {

        final File queue = folder.newFile("queue.txt");
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        clickOn("#source");

        if (IS_MACOSX) {
            push(RIGHT);
        }

        push(ENTER)
                .clickOn("#createProducer");


        then(action).should().accept("stream:file?fileName=" + queue.getAbsolutePath(), "jms:queue");

        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenCreateWithSourceAndOpeningFileChooserActionShouldBeRun() throws IOException {

        final File queue = folder.newFile("queue.txt");
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));
        lookup("#destination").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");

        clickOn("#source");

        if (IS_MACOSX) {
            push(RIGHT);
        }

        push(ENTER)
                .clickOn("#createProducer");

        then(action).should().accept("stream:file?fileName=" + queue.getAbsolutePath(), "jms:queue");

        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenClickOnDestinationAndItIsNotEmptyFileChooserShouldNotBeOpened() throws IOException {

        final File queue = folder.newFile("queue.txt");
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));
        lookup("#source").queryAs(JFXTextField.class).setText("sstream:file?fileName=" + queue.getAbsolutePath());
        lookup("#destination").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");

        clickOn("#source");
        push(HOME).push(DELETE);
        clickOn("#createProducer");

        then(action).should().accept("stream:file?fileName=" + queue.getAbsolutePath(), "jms:queue");

        assertThat("dialog should be closed", closed::get, is(true));
    }
}