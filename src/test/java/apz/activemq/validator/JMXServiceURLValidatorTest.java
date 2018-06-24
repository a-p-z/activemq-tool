package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JMXServiceURLValidatorTest  extends ApplicationTest {

    private JFXTextField jfxTextField;
    private JMXServiceURLValidator jfxTextFieldRequiredValidator;

    @Override
    public void start(final Stage stage) {
        jfxTextField = new JFXTextField();
        jfxTextFieldRequiredValidator = new JMXServiceURLValidator();
        jfxTextFieldRequiredValidator.setSrcControl(jfxTextField);
    }

    @Test
    public void whenJMXServiceURLIsMalformedValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("activemq.test.com/§nval§d");

        // when
        jfxTextFieldRequiredValidator.eval();

        // then
        assertThat("validator should have errors", jfxTextFieldRequiredValidator.getHasErrors(), is(true));
        assertThat("message should be 'field is required'", jfxTextFieldRequiredValidator.getMessage(), is("Service URL contains non-ASCII character 0xa7"));
    }

    @Test
    public void whenJMXServiceURLIsValidValidatorShouldNotHaveErrors() {
        // given
        jfxTextField.setText("activemq.test.com");

        // when
        jfxTextFieldRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxTextFieldRequiredValidator.getHasErrors(), is(false));
    }
}