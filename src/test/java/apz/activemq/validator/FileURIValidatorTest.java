package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileURIValidatorTest extends ApplicationTest {

    private JFXTextField jfxTextField;
    private FileURIValidator fileURIValidator;

    @Override
    public void start(final Stage stage) {
        jfxTextField = new JFXTextField();
        fileURIValidator = new FileURIValidator("uri is not valid");
        fileURIValidator.setSrcControl(jfxTextField);
    }

    @Test
    public void whenFileURIIsNotValidValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("ffile:tmp?fileName=messages.txt");

        // when
        fileURIValidator.eval();

        // then
        assertThat("validator should have errors", fileURIValidator.getHasErrors(), is(true));
        assertThat(fileURIValidator.getMessage(), is("uri is not valid: file schema is required"));
    }

    @Test
    public void whenFileURIIsMalformedValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("\\file:tmp?fileName=messages.txt");

        // when
        fileURIValidator.eval();

        // then
        assertThat("validator should have errors", fileURIValidator.getHasErrors(), is(true));
        assertThat(fileURIValidator.getMessage(), is("uri is not valid: file:{directoryName}?fileName={filename}[&options]"));
    }

    @Test
    public void whenFileURIIsValidValidatorShouldNotHaveErrors() {
        // given
        jfxTextField.setText("file:/tmp?fileName=messages.txt");

        // when
        fileURIValidator.eval();

        // then
        assertThat("validator should not have errors", fileURIValidator.getHasErrors(), is(false));
    }
}