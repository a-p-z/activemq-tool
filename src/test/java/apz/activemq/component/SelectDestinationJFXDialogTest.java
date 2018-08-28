package apz.activemq.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.validation.ValidationFacade;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static apz.activemq.utils.AssertUtils.assertThat;
import static java.util.stream.Collectors.joining;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class SelectDestinationJFXDialogTest extends ApplicationTest {

    final private StackPane container = new StackPane();

    @Mock
    private Consumer<String> action;

    private SelectDestinationJFXDialog dialog;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(container, 800, 580);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();

        dialog = new SelectDestinationJFXDialog(container, "Heading text", "source", action);
        dialog.addDestination("other.source");
        dialog.addDestination("s");
        dialog.addDestination("so");
        dialog.addDestination("sou");
        dialog.addDestination("sour");
        dialog.addDestination("sourc");
    }

    @Test
    public void headingBodyAndConfirmText() {

        final Label heading = lookup("#heading").query();
        final JFXButton button = lookup("#select").query();

        verifyZeroInteractions(action);
        assertThat("heading text should be 'Heading text'", heading::getText, is("Heading text"));
        assertThat("button text should be 'Select'", button::getText, is("Select"));
    }

    @Test
    public void whenCancelDialogShouldBeClosed() {
        // given
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        // when
        clickOn("#cancel");

        // then
        verifyZeroInteractions(action);
        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenSelectWithoutADestinationErrorShouldBeShown() {
        // when
        clickOn("#select");

        // then
        final ValidationFacade validationFacade = lookup("#validationFacade").query();
        final Supplier<String> errors = () -> validationFacade.getValidators().stream()
                .filter(ValidatorBase::getHasErrors)
                .map(ValidatorBase::getMessage)
                .collect(joining(", "));
        verifyZeroInteractions(action);
        assertThat("error message should be 'destination is required'", errors, is("destination is required"));
    }

    @Test
    public void whenClickOutsideDialogShouldBeClosed() {
        // given
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        // when
        moveTo(container)
                .moveBy(200, 200)
                .clickOn();

        // then
        verifyZeroInteractions(action);
        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenSelectTheFirstSuggestionDialogShouldBeClosed() {
        // given
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        // when
        clickOn(".arrow-button")
                .moveBy(-320, 30)
                .press(PRIMARY).release(PRIMARY);
        clickOn("#select");

        // then
        verify(action).accept("sourc");
        verifyNoMoreInteractions(action);
        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenWriteAndSelectTheFirstSuggestionDialogShouldBeClosed() {
        // given
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        // when
        clickOn("#autoCompleteJFXComboBox")
                .write("other")
                .moveBy(-120, 30)
                .press(PRIMARY).release(PRIMARY);
        clickOn("#select");

        // then
        verify(action).accept("other.source");
        verifyNoMoreInteractions(action);
        assertThat("dialog should be closed", closed::get, is(true));
    }

    @Test
    public void whenWriteAndNotSelectSuggestionDialogShouldBeClosed() {
        // given
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        // when
        clickOn("#autoCompleteJFXComboBox")
                .write("new-source");
        clickOn("#select");

        // then
        verify(action).accept("new-source");
        verifyNoMoreInteractions(action);
        assertThat("dialog should be closed", closed::get, is(true));
    }
}
