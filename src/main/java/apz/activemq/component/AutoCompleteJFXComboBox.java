package apz.activemq.component;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;
import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;
import static javafx.scene.input.KeyCode.*;

public class AutoCompleteJFXComboBox extends JFXComboBox<String> {

    private static final List<KeyCode> IGNORED_KEY_CODES = unmodifiableList(asList(RIGHT, LEFT, HOME, END, TAB, ENTER));

    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    private final ObservableList<String> suggestions = observableArrayList();
    private final String source;

    public AutoCompleteJFXComboBox(final String source) {

        requireNonNull(source, "source must be not null");

        this.source = source;
        setEditable(true);
        setItems(suggestions);

        setOnKeyPressed(t -> hide());
        setOnKeyReleased(this::handle);
    }

    public void addSuggestion(final String suggestion) {

        requireNonNull(suggestion, "suggestion must be not null");

        suggestions.add(suggestion);
        suggestions.sort(comparingInt(o -> levenshteinDistance.apply(source, o)));
    }

    private void handle(final KeyEvent event) {

        requireNonNull(event, "event must be not null");

        final String text = getEditor().getText();
        final int position = getEditor().getCaretPosition();
        final KeyCode code = event.getCode();

        if (code == UP || code == DOWN) {
            runLater(() -> getEditor().positionCaret(getEditor().getText().length()));
            show();
            return;
        }

        if (IGNORED_KEY_CODES.contains(code) || event.isControlDown()) {
            if (getItems().isEmpty() || text.isEmpty()) {
                hide();
            } else {
                show();
            }
            return;
        }

        runLater(() -> {
            setItems(suggestions.filtered(destination -> destination.toLowerCase()
                    .contains(text.trim().toLowerCase())));
            getEditor().setText(text);
            getEditor().positionCaret(position);
            if (getItems().isEmpty() || text.isEmpty()) {
                hide();
            } else {
                show();
            }
        });
    }
}
