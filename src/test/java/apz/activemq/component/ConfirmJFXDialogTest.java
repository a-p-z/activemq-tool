package apz.activemq.component;

import com.jfoenix.controls.JFXButton;
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

import static apz.activemq.utils.AssertUtils.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ConfirmJFXDialogTest extends ApplicationTest {

    final private StackPane container = new StackPane();

    @Mock
    private Runnable action;

    private ConfirmJFXDialog dialog;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(container, 800, 580);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();

        dialog = new ConfirmJFXDialog(container, action, "Heading text", "Are you sure?", "Confirm");
    }

    @Test
    public void headingBodyAndConfirmText() {

        final Label heading = lookup("#heading").query();
        final Label body = lookup("#body").query();
        final JFXButton button = lookup("#confirm").query();

        verifyZeroInteractions(action);
        assertThat("heading text should be 'Heading text'", heading::getText, is("Heading text"));
        assertThat("body text should be 'Are you sure?'", body::getText, is("Are you sure?"));
        assertThat("button text should be 'Confirm'", button::getText, is("Confirm"));
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
    public void whenConfirmActionShouldBeRunAndDialogShouldBeClosed() {
        // given
        final AtomicBoolean closed = new AtomicBoolean(false);
        dialog.setOnDialogClosed(event -> closed.set(true));

        // when
        clickOn("#confirm");

        // then
        verify(action).run();
        verifyNoMoreInteractions(action);
        assertThat("dialog should be closed", closed::get, is(true));
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
}
