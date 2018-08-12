package apz.activemq.rowfactory;

import apz.activemq.controller.MessagesController;
import apz.activemq.model.Message;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

import static java.util.Objects.requireNonNull;

public class MessageRowFactory implements Callback<TreeTableView<Message>, TreeTableRow<Message>> {

    private final MessagesController controller;

    public MessageRowFactory(final MessagesController controller) {

        requireNonNull(controller, "controller must be not null");

        this.controller = controller;
    }

    @Override
    public TreeTableRow<Message> call(final TreeTableView<Message> param) {

        return new TreeTableRow<Message>() {
            @Override
            public void updateItem(final Message message, final boolean empty) {

                super.updateItem(message, empty);

                if (null != message) {
                    controller.addMessageUserColumns(message.messageUserProperties.getValue().keySet());
                }
            }
        };
    }
}

