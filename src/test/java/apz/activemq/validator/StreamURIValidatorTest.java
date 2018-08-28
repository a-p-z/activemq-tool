package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StreamURIValidatorTest extends ApplicationTest {

    private JFXTextField jfxTextField;
    private StreamURIValidator streamURIValidator;

    @Override
    public void start(final Stage stage) {
        jfxTextField = new JFXTextField();
        streamURIValidator = new StreamURIValidator("uri is not valid");
        streamURIValidator.setSrcControl(jfxTextField);
    }

    @Test
    public void whenStreamURIIsMalformedValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("sstream:file:/tmp?fileName=messages.txt");

        // when
        streamURIValidator.eval();

        // then
        assertThat("validator should have errors", streamURIValidator.getHasErrors(), is(true));
        assertThat(streamURIValidator.getMessage(), is("uri is not valid: stream schema is required"));
    }

    @Test
    public void whenStreamURIIsValidValidatorShouldNotHaveErrors() {
        // given
        jfxTextField.setText("stream:file:/tmp?fileName=messages.txt");

        // when
        streamURIValidator.eval();

        // then
        assertThat("validator should not have errors", streamURIValidator.getHasErrors(), is(false));
    }

    @Test
    public void whenStreamURIHasIllegalCharacterValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("\\stream:file:/tmp?fileName=messages.txt");

        // when
        streamURIValidator.eval();

        // then
        assertThat("validator should not have errors", streamURIValidator.getHasErrors(), is(true));
        assertThat(streamURIValidator.getMessage(), is("uri is not valid: stream:file?fileName={fileName}[&options]"));
    }

    @Test
    public void whenStreamURIIsNotValidValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("stream:in:/tmp?fileName=messages.txt");

        // when
        streamURIValidator.eval();

        // then
        assertThat("validator should not have errors", streamURIValidator.getHasErrors(), is(true));
        assertThat(streamURIValidator.getMessage(), is("uri is not valid: stream:file is required"));
    }
}