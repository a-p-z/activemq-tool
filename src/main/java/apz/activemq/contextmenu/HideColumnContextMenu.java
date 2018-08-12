package apz.activemq.contextmenu;

import com.jfoenix.controls.JFXTreeTableColumn;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static java.util.Objects.requireNonNull;

public class HideColumnContextMenu extends ContextMenu {

    public HideColumnContextMenu(final JFXTreeTableColumn<?, ?> column) {
        super();

        requireNonNull(column, "column must be not null");

        final MenuItem hide = new MenuItem("hide");
        hide.setId("hide");

        getItems().add(hide);
        hide.setOnAction(event -> column.setVisible(false));
    }
}
