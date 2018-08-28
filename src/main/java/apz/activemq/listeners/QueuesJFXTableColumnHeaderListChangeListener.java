package apz.activemq.listeners;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javafx.geometry.Insets.EMPTY;
import static javafx.scene.text.TextAlignment.CENTER;

public class QueuesJFXTableColumnHeaderListChangeListener implements ListChangeListener<Node> {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueuesJFXTableColumnHeaderListChangeListener.class);

    private final double value;

    public QueuesJFXTableColumnHeaderListChangeListener(double value) {
        this.value = value;
    }

    @Override
    public void onChanged(final Change<? extends Node> c) {

        LOGGER.debug("JFXTableColumnHeader changed");

        c.next();
        c.getAddedSubList().stream()
                .filter(StackPane.class::isInstance)
                .map(StackPane.class::cast)
                .flatMap(sp -> {
                    LOGGER.debug("JFXTableColumnHeader added, setting internal StackPane height");
                    sp.minHeight(value);
                    sp.setPrefHeight(value);
                    sp.maxHeight(value);
                    return sp.getChildren().stream();
                })
                .filter(Label.class::isInstance)
                .map(Label.class::cast)
                .forEach(label -> {
                    LOGGER.debug("JFXTableColumnHeader added, setting internal Label height");
                    label.minHeight(value);
                    label.setPrefHeight(value);
                    label.maxHeight(value);
                    label.setPadding(EMPTY);
                    label.setTextAlignment(CENTER);
                });
    }
}
