package apz.activemq.validator;

import com.jfoenix.controls.JFXComboBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JFXComboBoxRequiredValidatorTest extends ApplicationTest {

    private JFXComboBox<String> jfxComboBox;
    private JFXComboBoxRequiredValidator jfxComboBoxRequiredValidator;

    @Override
    public void start(final Stage stage) {
        jfxComboBox = new JFXComboBox<>();
        jfxComboBoxRequiredValidator = new JFXComboBoxRequiredValidator("field is required");
        jfxComboBoxRequiredValidator.setSrcControl(jfxComboBox);
    }

    @Test
    public void whenValueIsNullEditableIsFalseAndTextIsNull(){
        // given
        jfxComboBox.setValue(null);
        jfxComboBox.setEditable(false);
        jfxComboBox.getEditor().setText(null);

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(true));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNullEditableIsFalseAndTextIsEmpty(){
        // given
        jfxComboBox.setValue(null);
        jfxComboBox.setEditable(false);
        jfxComboBox.getEditor().setText("");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(true));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNullEditableIsFalseAndTextIsNotEmpty(){
        // given
        jfxComboBox.setValue(null);
        jfxComboBox.setEditable(false);
        jfxComboBox.getEditor().setText("value");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(true));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNullEditableIsTrueAndTextIsNull(){
        // given
        jfxComboBox.setValue(null);
        jfxComboBox.setEditable(true);
        jfxComboBox.getEditor().setText(null);

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(true));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNullEditableIsTrueAndTextIsEmpty(){
        // given
        jfxComboBox.setValue(null);
        jfxComboBox.setEditable(true);
        jfxComboBox.getEditor().setText("");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(true));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNullEditableIsTrueAndTextIsNotEmpty(){
        // given
        jfxComboBox.setValue(null);
        jfxComboBox.setEditable(true);
        jfxComboBox.getEditor().setText("value");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(false));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }


    @Test
    public void whenValueIsNotNullEditableIsFalseAndTextIsNull(){
        // given
        jfxComboBox.setValue("value");
        jfxComboBox.setEditable(false);
        jfxComboBox.getEditor().setText(null);

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(false));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNotNullEditableIsFalseAndTextIsEmpty(){
        // given
        jfxComboBox.setValue("value");
        jfxComboBox.setEditable(false);
        jfxComboBox.getEditor().setText("");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(false));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNotNullEditableIsFalseAndTextIsNotEmpty(){
        // given
        jfxComboBox.setValue("value");
        jfxComboBox.setEditable(false);
        jfxComboBox.getEditor().setText("value");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(false));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNotNullEditableIsTrueAndTextIsNull(){
        // given
        jfxComboBox.setValue("value");
        jfxComboBox.setEditable(true);
        jfxComboBox.getEditor().setText(null);

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(false));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNotNullEditableIsTrueAndTextIsEmpty(){
        // given
        jfxComboBox.setValue("value");
        jfxComboBox.setEditable(true);
        jfxComboBox.getEditor().setText("");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(false));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }

    @Test
    public void whenValueIsNotNullEditableIsTrueAndTextIsNotEmpty(){
        // given
        jfxComboBox.setValue("value");
        jfxComboBox.setEditable(true);
        jfxComboBox.getEditor().setText("value");

        // when
        jfxComboBoxRequiredValidator.eval();

        // then
        assertThat("validator should not have errors", jfxComboBoxRequiredValidator.getHasErrors(), is(false));
        assertThat( jfxComboBoxRequiredValidator.getMessage(), is("field is required"));
    }






















}