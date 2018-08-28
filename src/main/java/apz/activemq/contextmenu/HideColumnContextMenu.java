package apz.activemq.contextmenu;

import com.jfoenix.controls.JFXTreeTableColumn;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import javax.annotation.Nonnull;

public class HideColumnContextMenu extends ContextMenu {

    public HideColumnContextMenu(final @Nonnull JFXTreeTableColumn<?, ?> column) {
        super();

        final MenuItem hide = new MenuItem("hide");
        hide.setId("hide");

        getItems().add(hide);
        hide.setOnAction(event -> column.setVisible(false));
    }
}
