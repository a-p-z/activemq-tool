package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import javafx.stage.Stage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.testfx.framework.junit.ApplicationTest;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class StreamURIExistValidatorTest extends ApplicationTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private JFXTextField jfxTextField;
    private StreamURIExistValidator streamURIExistValidator;

    @Override
    public void start(final Stage stage) {
        jfxTextField = new JFXTextField();
        streamURIExistValidator = new StreamURIExistValidator("uri is not valid");
        streamURIExistValidator.setSrcControl(jfxTextField);
    }

    @Test
    public void whenFileNotExistsValidatorShouldHaveErrors() {
        // given
        final String absolutePath = format("%s/%s", folder.getRoot(), "messages.txt");
        jfxTextField.setText(format("stream:file?fileName=%s", absolutePath));

        // when
        streamURIExistValidator.eval();

        // then
        assertThat("validator should have errors", streamURIExistValidator.getHasErrors(), is(true));
        assertThat(streamURIExistValidator.getMessage(), is(format("uri is not valid: %s not exists", absolutePath)));
    }

    @Test
    public void whenFileExistsValidValidatorShouldNotHaveErrors() throws IOException {
        // given
        final File file = folder.newFile("messages.txt");
        jfxTextField.setText(format("stream:file?fileName=%s", file.getAbsolutePath()));

        // when
        streamURIExistValidator.eval();

        // then
        assertThat("validator should not have errors", streamURIExistValidator.getHasErrors(), is(false));
    }

    @Test
    public void whenURINotContainsParamsValidValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("stream:file");

        // when
        streamURIExistValidator.eval();

        // then
        assertThat("validator should not have errors", streamURIExistValidator.getHasErrors(), is(true));
        assertThat(streamURIExistValidator.getMessage(), is("uri is not valid: fileName param is required"));
    }

    @Test
    public void whenURINotContainsFilenameValueValidValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("stream:file?fileName");

        // when
        streamURIExistValidator.eval();

        // then
        assertThat("validator should not have errors", streamURIExistValidator.getHasErrors(), is(true));
        assertThat(streamURIExistValidator.getMessage(), is("uri is not valid: fileName param is required"));
    }

    @Test
    public void whenURINotContainsFilenameParamValueValidValidatorShouldHaveErrors() {
        // given
        jfxTextField.setText("stream:file?");

        // when
        streamURIExistValidator.eval();

        // then
        assertThat("validator should not have errors", streamURIExistValidator.getHasErrors(), is(true));
        assertThat(streamURIExistValidator.getMessage(), is("uri is not valid: fileName param is required"));
    }
}