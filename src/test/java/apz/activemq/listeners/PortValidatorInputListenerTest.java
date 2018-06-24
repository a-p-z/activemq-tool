package apz.activemq.listeners;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PortValidatorInputListenerTest extends ApplicationTest {

    @Mock
    private ObservableValue<? extends String> observable;

    private TextInputControl port;
    private PortValidatorInputListener portValidatorInputListener;

    @Override
    public void start(final Stage stage) {
        port = new JFXTextField();
        portValidatorInputListener = new PortValidatorInputListener(port);
    }

    @Test
    public void whenNewValueIsEmptyPortShouldBeEmpty() {
        // when
        portValidatorInputListener.changed(observable, "1099", "");

        // then
        assertThat("port should be equal to newValue", port.getText(), is(""));
    }

    @Test
    public void whenNewValueIsPositiveIntegerPortShouldEqualToNewValue() {
        // when
        portValidatorInputListener.changed(observable, "1099", "  2099  ");

        // then
        assertThat("port should be equal to newValue", port.getText(), is("2099"));
    }

    @Test
    public void whenNewValueIsNegativeIntegerPortShouldEqualToOldValue() {
        // when
        portValidatorInputListener.changed(observable, "1099", "  -2099  ");

        // then
        assertThat("port should be equal to oldValue", port.getText(), is("1099"));
    }

    @Test
    public void whenNewValueIsNotIntegerPortShouldEqualToOldValue() {
        // when
        portValidatorInputListener.changed(observable, "1099", "NOT_INTEGER");

        // then
        assertThat("port should be equal to oldValue", port.getText(), is("1099"));
    }
}