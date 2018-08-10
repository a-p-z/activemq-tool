package apz.activemq.contextmenu;

import apz.activemq.controller.QueuesController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static java.util.Objects.requireNonNull;

public class QueueRowContextMenu extends ContextMenu {

    public QueueRowContextMenu(final QueuesController controller) {

        super();

        requireNonNull(controller, "controller must be not null");

        final MenuItem purgeItem = new MenuItem("Purge queue");

        getItems().addAll(purgeItem);

        purgeItem.setId("purge");
        purgeItem.setOnAction(controller::purgeSelectedQueue);
    }
}
