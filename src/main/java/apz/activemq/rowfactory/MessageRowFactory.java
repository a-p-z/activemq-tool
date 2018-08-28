package apz.activemq.rowfactory;

import apz.activemq.contextmenu.MessageRowContextMenu;
import apz.activemq.controller.MessagesController;
import apz.activemq.model.Message;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

import javax.annotation.Nonnull;

public class MessageRowFactory implements Callback<TreeTableView<Message>, TreeTableRow<Message>> {

    private final MessagesController controller;
    private final MessageRowContextMenu messageRowContextMenu;

    public MessageRowFactory(final @Nonnull MessagesController controller) {
        this.controller = controller;
        this.messageRowContextMenu = new MessageRowContextMenu(controller);
    }

    @Override
    public TreeTableRow<Message> call(final TreeTableView<Message> param) {

        return new TreeTableRow<Message>() {
            @Override
            public void updateItem(final Message message, final boolean empty) {

                super.updateItem(message, empty);
                setContextMenu(!empty ? messageRowContextMenu : null);
                if (null != message) {
                    controller.addMessageUserColumns(message.messageUserProperties.getValue().keySet());
                }
            }
        };
    }
}

