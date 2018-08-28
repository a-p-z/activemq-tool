package apz.activemq.listeners;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;

import javax.annotation.Nonnull;

public class PortValidatorInputListener implements ChangeListener<String> {

    private final TextInputControl port;

    public PortValidatorInputListener(final @Nonnull TextInputControl port) {
        this.port = port;
    }

    @Override
    public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {

        try {
            if (newValue.isEmpty() || Integer.parseInt(newValue.trim()) > 0) {
                port.setText(newValue.trim());
            } else {
                port.setText(oldValue);
            }
        } catch (final NumberFormatException e) {
            port.setText(oldValue);
        }
    }
}
