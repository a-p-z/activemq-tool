package apz.activemq.listeners;

import apz.activemq.model.Queue;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.skins.JFXNestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueuesTableSkinListener implements ChangeListener<Skin<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuesTableSkinListener.class);

    private final double value;
    private final JFXTreeTableView<Queue> table;

    public QueuesTableSkinListener(final JFXTreeTableView<Queue> table, final double value) {
        this.value = value;
        this.table = table;
    }

    @Override
    public void changed(final ObservableValue<? extends Skin<?>> observable, final Skin<?> oldValue, Skin<?> newValue) {

        LOGGER.debug("table skin changed");

        final Node tableHeaderRowNode = table.lookup(".column-header-background");
        final Node jfxNestedTableColumnHeaderNode = table.lookup(".nested-column-header");

        if (tableHeaderRowNode instanceof TableHeaderRow) {
            LOGGER.debug("setting table header row height");
            final TableHeaderRow tableHeaderRow = (TableHeaderRow) tableHeaderRowNode;
            tableHeaderRow.setMinHeight(value);
            tableHeaderRow.setPrefHeight(value);
            tableHeaderRow.setMaxHeight(value);
        }

        if (jfxNestedTableColumnHeaderNode instanceof JFXNestedTableColumnHeader) {
            LOGGER.debug("setting JFX nested table column header height");
            final JFXNestedTableColumnHeader jfxNestedTableColumnHeader = (JFXNestedTableColumnHeader) jfxNestedTableColumnHeaderNode;
            jfxNestedTableColumnHeader.setMinHeight(value);
            jfxNestedTableColumnHeader.setPrefHeight(value);
            jfxNestedTableColumnHeader.setMaxHeight(value);
            jfxNestedTableColumnHeader.getChildrenUnmodifiable().addListener(new QueuesJFXNestedTableColumnHeaderListChangeListener(jfxNestedTableColumnHeader.getChildrenUnmodifiable(), value));
        }

        LOGGER.debug("removing {} from table skinProperty listeners", this);
        table.skinProperty().removeListener(this);
    }
}
