package apz.activemq.rowfactory;

import apz.activemq.contextmenu.QueueRowContextMenu;
import apz.activemq.controller.QueuesController;
import apz.activemq.model.Queue;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

import javax.annotation.Nonnull;

public class QueueRowFactory implements Callback<TreeTableView<Queue>, TreeTableRow<Queue>> {

    private final QueueRowContextMenu queueRowContextMenu;

    public QueueRowFactory(final @Nonnull QueuesController controller) {
        this.queueRowContextMenu = new QueueRowContextMenu(controller);
    }

    @Override
    public TreeTableRow<Queue> call(final TreeTableView<Queue> param) {

        return new TreeTableRow<Queue>() {
            @Override
            public void updateItem(final Queue queue, final boolean empty) {

                super.updateItem(queue, empty);
                setContextMenu(!empty ? queueRowContextMenu : null);
            }
        };
    }
}
