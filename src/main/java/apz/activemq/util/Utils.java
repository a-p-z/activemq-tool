package apz.activemq.util;

import apz.activemq.model.Message;
import com.jfoenix.controls.JFXTreeTableColumn;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

public class Utils {

    public static <T, U> void setupCellValueFactory(final @Nonnull JFXTreeTableColumn<T, U> column, final @Nonnull Function<T, ObservableValue<U>> mapper) {
        column.setCellValueFactory((CellDataFeatures<T, U> c) -> mapper.apply(c.getValue().getValue()));
    }

    public static void setupCellValueFactory(final @Nonnull JFXTreeTableColumn<Message, Object> column) {

        column.setCellValueFactory(c -> {

            final Message message = c.getValue().getValue();
            final Map properties = message.messageUserProperties.getValue();
            final String key = column.getText();
            final Object value = properties.get(key);

            return new SimpleObjectProperty<>(value);
        });
    }
}
