package apz.activemq.util;

import apz.activemq.model.Message;
import com.jfoenix.controls.JFXTreeTableColumn;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;

import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Utils {

    public static <T, U> void setupCellValueFactory(final JFXTreeTableColumn<T, U> column, final Function<T, ObservableValue<U>> mapper) {

        requireNonNull(column, "column must be not null");
        requireNonNull(mapper, "mapper must be not null");

        column.setCellValueFactory((CellDataFeatures<T, U> c) -> mapper.apply(c.getValue().getValue()));
    }

    public static void setupCellValueFactory(final JFXTreeTableColumn<Message, Object> column) {

        requireNonNull(column, "column must be not null");

        column.setCellValueFactory(c -> {

            final Message message = c.getValue().getValue();
            final Map properties = message.messageUserProperties.getValue();
            final String key = column.getText();
            final Object value = properties.get(key);

            return new SimpleObjectProperty<>(value);
        });
    }
}
