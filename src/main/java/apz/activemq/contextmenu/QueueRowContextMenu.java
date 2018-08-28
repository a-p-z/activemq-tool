package apz.activemq.contextmenu;

import apz.activemq.controller.QueuesController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import javax.annotation.Nonnull;

public class QueueRowContextMenu extends ContextMenu {

    public QueueRowContextMenu(final @Nonnull QueuesController controller) {

        super();

        final MenuItem browseItem = new MenuItem("Browse queue");
        final MenuItem purgeItem = new MenuItem("Purge queue");
        final MenuItem deleteItem = new MenuItem("Delete queue");

        getItems().addAll(browseItem, purgeItem, deleteItem);

        browseItem.setId("browse");
        browseItem.setOnAction(controller::browseQueue);

        purgeItem.setId("purge");
        purgeItem.setOnAction(controller::purgeSelectedQueue);

        deleteItem.setId("delete");
        deleteItem.setOnAction(controller::deleteSelectedQueue);
    }
}
