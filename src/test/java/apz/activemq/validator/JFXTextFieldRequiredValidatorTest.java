package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JFXTextFieldRequiredValidatorTest extends ApplicationTest {

    private JFXTextField jfxTextField;
    private JFXTextFieldRequiredValidator jfxTextFieldRequiredValidator;

    @Override
    public void start(final Stage stage) {
        jfxTextField = new JFXTextField();
        jfxTextFieldRequiredValidator = new JFXTextFieldRequiredValidator("field is required");
        jfxTextFieldRequiredValidator.setSrcControl(jfxTextField);
    }

    @Test
    public void whenJFXTextFieldIsNullValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText(null);

        // when
        jfxTextFieldRequiredValidator.eval();

        // then
        assertThat("validator should have errors", jfxTextFieldRequiredValidator.getHasErrors(), is(true));
        assertThat("message should be 'field is required'", jfxTextFieldRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenJFXTextFieldIsEmptyValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("");

        // when
        jfxTextFieldRequiredValidator.eval();

        // then
        assertThat("validator should have errors", jfxTextFieldRequiredValidator.getHasErrors(), is(true));
        assertThat("message should be 'field is required'", jfxTextFieldRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenJFXTextFieldIsNotEmptyValidatorShouldNotHaveErrors() {
        // given
        jfxTextField.setText("value");

        // when
        jfxTextFieldRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxTextFieldRequiredValidator.getHasErrors(), is(false));
    }
}