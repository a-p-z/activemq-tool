package apz.activemq.validator;

import com.jfoenix.controls.JFXComboBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JmsURIValidatorTest extends ApplicationTest {

    private JFXComboBox jfxComboBox;
    private JmsURIValidator jmsURIValidator;

    @Override
    public void start(final Stage stage) {
        jfxComboBox = new JFXComboBox();
        jmsURIValidator = new JmsURIValidator("uri is not valid");
        jmsURIValidator.setSrcControl(jfxComboBox);
    }

    @Test
    public void whenStreamURIIsNotValidValidatorShouldHaveErrors() {
        // given
        jfxComboBox.getEditor().setText("jjms:queue");

        // when
        jmsURIValidator.eval();

        // then
        assertThat("validator should have errors", jmsURIValidator.getHasErrors(), is(true));
        assertThat(jmsURIValidator.getMessage(), is("uri is not valid: jms or activemq schema is required"));
    }

    @Test
    public void whenJmsURIIsValidValidatorShouldNotHaveErrors() {
        // given
        jfxComboBox.getEditor().setText("jms:queue");

        // when
        jmsURIValidator.eval();

        // then
        assertThat("validator should not have errors", jmsURIValidator.getHasErrors(), is(false));
    }

    @Test
    public void whenActivemqURIIsValidValidatorShouldNotHaveErrors() {
        // given
        jfxComboBox.getEditor().setText("activemq:queue");

        // when
        jmsURIValidator.eval();

        // then
        assertThat("validator should not have errors", jmsURIValidator.getHasErrors(), is(false));
    }

    @Test
    public void whenStreamURIHasIllegalCharacterValidatorShouldHaveErrors() {
        // given
        jfxComboBox.getEditor().setText("\\activemq:queue");

        // when
        jmsURIValidator.eval();

        // then
        assertThat("validator should not have errors", jmsURIValidator.getHasErrors(), is(true));
        assertThat(jmsURIValidator.getMessage(), is("uri is not valid: jms:{queueName}[?options] or activemq:{queueName}[?options]"));
    }
}