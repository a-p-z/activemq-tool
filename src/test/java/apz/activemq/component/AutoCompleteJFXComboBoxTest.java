package apz.activemq.component;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import static apz.activemq.utils.AssertUtils.assertThat;
import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.hamcrest.CoreMatchers.is;

@RunWith(MockitoJUnitRunner.class)
public class AutoCompleteJFXComboBoxTest extends ApplicationTest {

    final private StackPane container = new StackPane();

    private AutoCompleteJFXComboBox autoCompleteJFXComboBox;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(container, 800, 580);

        autoCompleteJFXComboBox = new AutoCompleteJFXComboBox("source");

        autoCompleteJFXComboBox.setId("autoCompleteJFXComboBox");
        autoCompleteJFXComboBox.addSuggestion("other.source");
        autoCompleteJFXComboBox.addSuggestion("s");
        autoCompleteJFXComboBox.addSuggestion("so");
        autoCompleteJFXComboBox.addSuggestion("sou");
        autoCompleteJFXComboBox.addSuggestion("sour");
        autoCompleteJFXComboBox.addSuggestion("sourc");
        autoCompleteJFXComboBox.addSuggestion("source");

        container.getChildren().add(autoCompleteJFXComboBox);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void whenSelectTheFirstSuggestionValueShouldBeSet() {
        // when
        clickOn(".arrow-button")
                .moveBy(-120, 30)
                .press(PRIMARY).release(PRIMARY);

        // then
        assertThat("value should be 'source'", autoCompleteJFXComboBox.getEditor()::getText, is("source"));
    }

    @Test
    public void whenWriteAndSelectTheFirstSuggestionValueShouldBeSet() {
        // when
        clickOn("#autoCompleteJFXComboBox")
                .write("other")
                .moveBy(-60, 30)
                .press(PRIMARY).release(PRIMARY);

        // then
        assertThat("value should be 'other.source'", autoCompleteJFXComboBox.getEditor()::getText, is("other.source"));
    }

    @Test
    public void whenpushDownAndUpSuggestionValueShouldBeSet() {
        // when
        push(DOWN)
                .push(DOWN)
                .push(DOWN)
                .push(DOWN)
                .push(DOWN)
                .push(UP);

        // then
        assertThat("value should be 'sour'", autoCompleteJFXComboBox.getEditor()::getText, is("sour"));
    }

    @Test
    public void whenPushIngnoredKeySuggestionValueShouldBeSet() {
        // given
        autoCompleteJFXComboBox.getEditor().setText("sXourceX");

        // when
        push(END)
                .push(BACK_SPACE);

        push(HOME)
                .push(RIGHT)
                .push(DELETE);

        // then
        assertThat("value should be 'source'", autoCompleteJFXComboBox.getEditor()::getText, is("source"));
    }
}
