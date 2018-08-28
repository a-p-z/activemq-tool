package apz.activemq.listeners;

import com.jfoenix.skins.JFXTableColumnHeader;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class QueuesJFXNestedTableColumnHeaderListChangeListener implements ListChangeListener<Node> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuesJFXNestedTableColumnHeaderListChangeListener.class);

    private final ObservableList<Node> childrenUnmodifiable;
    private final double value;

    public QueuesJFXNestedTableColumnHeaderListChangeListener(final @Nonnull ObservableList<Node> childrenUnmodifiable, double value) {
        this.childrenUnmodifiable = childrenUnmodifiable;
        this.value = value;
    }

    @Override
    public void onChanged(final @Nonnull Change<? extends Node> c) {

        LOGGER.debug("jfx nested table column header changed");

        c.next();
        c.getAddedSubList().stream()
                .filter(JFXTableColumnHeader.class::isInstance)
                .map(JFXTableColumnHeader.class::cast)
                .forEach(jfxTableColumnHeader -> {
                    LOGGER.debug("jfx nested table column header added, setting height");
                    jfxTableColumnHeader.minHeight(value);
                    jfxTableColumnHeader.setPrefHeight(value);
                    jfxTableColumnHeader.maxHeight(value);
                    jfxTableColumnHeader.getChildrenUnmodifiable().addListener(new QueuesJFXTableColumnHeaderListChangeListener(value));
                });
        LOGGER.debug("removing {} from JFXNestedTableColumnHeader listeners", this);
        childrenUnmodifiable.removeListener(this);
    }
}
