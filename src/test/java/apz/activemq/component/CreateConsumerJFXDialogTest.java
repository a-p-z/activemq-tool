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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static apz.activemq.utils.AssertUtils.assertThat;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static javafx.scene.input.KeyCode.DELETE;
import static javafx.scene.input.KeyCode.E;
import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.PERIOD;
import static javafx.scene.input.KeyCode.Q;
import static javafx.scene.input.KeyCode.T;
import static javafx.scene.input.KeyCode.U;
import static javafx.scene.input.KeyCode.X;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class CreateConsumerJFXDialogTest extends ApplicationTest {

    private final static boolean IS_MACOSX = System.getProperty("os.name").startsWith("Mac");

    @Rule
    public TemporaryFolder folder =  new TemporaryFolder(IS_MACOSX
            ? new File("/private/" + System.getProperty("java.io.tmpdir"))
            : new File(System.getProperty("java.io.tmpdir")));

    final private StackPane container = new StackPane();

    @Mock
    private BiConsumer<String, String> action;

    private CreateConsumerJFXDialog dialog;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(container, 800, 580);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();

        dialog = new CreateConsumerJFXDialog(container, action, folder.getRoot());
    }

    @Test
    public void whenCreateWithoutASourceErrorShouldBeShown() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        clickOn("#createConsumer");

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
    public void whenCreateWithoutDestinationErrorShouldBeShown() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");

        clickOn("#createConsumer");

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

        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("invalid:queue");

        clickOn("#createConsumer");

        final ValidationFacade validationFacade = lookup("#sourceValidationFacade").query();
        final Supplier<String> errors = () -> validationFacade.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));

        then(action).should(never()).accept(anyString(), anyString());

        assertThat("error message should be 'source uri is not valid: jms or activemq schema is required'", errors,
                is("source uri is not valid: jms or activemq schema is required"));
        assertThat("dialog should not be closed", closed::get, is(false));
    }

    @Test
    public void whenCreateWithANotValidDestinationSchemaErrorShouldBeShown() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");
        lookup("#destination").queryAs(JFXTextField.class).setText("stream?fileName=/tmp/messages.txt");

        clickOn("#createConsumer");

        final ValidationFacade validationFacade = lookup("#destinationValidationFacade").query();
        final Supplier<String> errors = () -> validationFacade.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));

        then(action).should(never()).accept(anyString(), anyString());

        assertThat("error message should be 'destination uri is not valid: file schema is required'", errors,
                is("destination uri is not valid: file schema is required"));
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
    public void whenCreateWithValidSourceAndDestinationActionShouldBeRun() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");
        lookup("#destination").queryAs(JFXTextField.class).setText("file:/tmp/messages.txt");

        clickOn("#createConsumer");

        then(action).should().accept("jms:queue", "file:/tmp/messages.txt");

        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenCreateOpeningFileChooserActionShouldBeRun() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        clickOn("#destination");
        push(Q).push(U).push(E).push(U).push(E).push(PERIOD).push(T).push(X).push(T)
                .push(ENTER);
        clickOn("#createConsumer");

        then(action).should().accept("jms:queue", format("file:%s?fileName=queue.txt&autoCreate=true", folder.getRoot()));

        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenCreateWithSourceAndOpeningFileChooserActionShouldBeRun() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));
        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");

        clickOn("#destination")
                .push(ENTER)
                .clickOn("#createConsumer");

        then(action).should().accept("jms:queue", format("file:%s?fileName=queue.txt&autoCreate=true", folder.getRoot()));

        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenClickOnDestinationAndItIsNotEmptyFileChooserShouldNotBeOpened() {

        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));
        lookup("#source").queryAs(AutoCompleteJFXComboBox.class).getEditor().setText("jms:queue");
        lookup("#destination").queryAs(JFXTextField.class).setText(format("ffile:%s?fileName=queue.txt&autoCreate=true", folder.getRoot()));

        clickOn("#destination");
        push(HOME).push(DELETE);
        clickOn("#createConsumer");

        then(action).should().accept("jms:queue", format("file:%s?fileName=queue.txt&autoCreate=true", folder.getRoot()));

        assertThat("dialog should be closed", closed::get, is(true));
    }
}