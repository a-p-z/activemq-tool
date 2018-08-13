package apz.activemq.contextmenu;

import apz.activemq.controller.MessagesController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static java.util.Objects.requireNonNull;

public class MessageRowContextMenu extends ContextMenu {

    public MessageRowContextMenu(final MessagesController controller) {

        super();

        requireNonNull(controller, "controller must be not null");

        final MenuItem copyItem = new MenuItem("Copy");
        final MenuItem moveItem = new MenuItem("Move");
        final MenuItem deleteItem = new MenuItem("Delete");

        getItems().addAll(copyItem, moveItem, deleteItem);

        copyItem.setId("copy");
        copyItem.setOnAction(action -> controller.copySelectedMessagesTo());

        moveItem.setId("move");
        moveItem.setOnAction(action -> controller.moveSelectedMessagesTo());

        deleteItem.setId("delete");
        deleteItem.setOnAction(action -> controller.deleteSelectedMessages());
    }
}
