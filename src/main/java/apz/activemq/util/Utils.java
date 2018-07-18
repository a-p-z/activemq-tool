package apz.activemq.util;

import com.jfoenix.controls.JFXTreeTableColumn;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Utils {

    public static <T, U> void setupCellValueFactory(final JFXTreeTableColumn<T, U> column, final Function<T, ObservableValue<U>> mapper) {

        requireNonNull(column, "column must be not null");
        requireNonNull(mapper, "mapper must be not null");

        column.setCellValueFactory((CellDataFeatures<T, U> c) -> mapper.apply(c.getValue().getValue()));
    }
}
