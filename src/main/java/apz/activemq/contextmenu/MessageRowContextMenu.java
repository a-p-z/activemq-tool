package apz.activemq.contextmenu;

import apz.activemq.controller.MessagesController;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static java.util.Objects.requireNonNull;

public class MessageRowContextMenu extends ContextMenu {

    public MessageRowContextMenu(final MessagesController controller) {

        super();

        requireNonNull(controller, "controller must be not null");

        final MenuItem copyItem = new MenuItem("Copy to clipboard");
        final MenuItem copyToItem = new MenuItem("Copy to");
        final MenuItem moveToItem = new MenuItem("Move to");
        final MenuItem deleteItem = new MenuItem("Delete");

        getItems().addAll(copyItem, copyToItem, moveToItem, deleteItem);

        copyItem.setId("copyToClipboard");
        copyItem.setOnAction(action -> controller.copySelectedMessagesToClipboard());

        copyToItem.setId("copyTo");
        copyToItem.setOnAction(action -> controller.copySelectedMessagesTo());

        moveToItem.setId("moveTo");
        moveToItem.setOnAction(action -> controller.moveSelectedMessagesTo());

        deleteItem.setId("delete");
        deleteItem.setOnAction(action -> controller.deleteSelectedMessages());
    }
}
