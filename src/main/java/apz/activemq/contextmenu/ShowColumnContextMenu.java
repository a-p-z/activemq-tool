package apz.activemq.contextmenu;

import com.jfoenix.controls.JFXTreeTableView;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static javafx.geometry.Side.BOTTOM;

public class ShowColumnContextMenu extends ContextMenu {

    private final JFXTreeTableView<?> table;

    public ShowColumnContextMenu(final @Nonnull JFXTreeTableView<?> table) {

        super();

        this.table = table;
    }

    @Override
    public void show(final @Nonnull Node anchor, double screenX, double screenY) {

        final List<MenuItem> menuItems = table.getColumns().stream()
                .filter(column -> !column.isVisible())
                .map(hiddenColumn -> {
                    final MenuItem menuItem = new MenuItem(hiddenColumn.getText());
                    menuItem.setOnAction(event -> hiddenColumn.setVisible(true));
                    return menuItem;
                })
                .sorted(comparing(MenuItem::getText))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        getItems().setAll(menuItems);

        if (menuItems.size() > 1) {
            final MenuItem menuItem = new MenuItem("Show all");
            menuItem.setId("showAll");
            menuItem.setOnAction(event -> table.getColumns().forEach(column -> column.setVisible(true)));
            getItems().add(menuItem);

        } else if (menuItems.isEmpty()) {
            final MenuItem menuItem = new MenuItem("Hide all");
            menuItem.setId("hideAll");
            menuItem.setOnAction(event -> table.getColumns().stream()
                    .filter(column -> !"Message id".equals(column.getText()))
                    .filter(column -> !"+".equals(column.getText()))
                    .forEach(column -> column.setVisible(false)));
            getItems().add(menuItem);
        }

        show(anchor, BOTTOM, 0, 0);
    }
}
