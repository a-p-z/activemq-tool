package apz.activemq.component;

import apz.activemq.validator.FileURIValidator;
import apz.activemq.validator.JFXComboBoxRequiredValidator;
import apz.activemq.validator.JFXTextFieldRequiredValidator;
import apz.activemq.validator.JmsURIValidator;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.ValidationFacade;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.jfoenix.controls.JFXDialog.DialogTransition.CENTER;
import static com.jfoenix.validation.ValidationFacade.validate;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;
import static javafx.scene.input.MouseButton.PRIMARY;

public class CreateConsumerJFXDialog extends JFXDialog {

    private static final String ERROR_LABEL = ".error-label";
    private static final String ERROR_STYLE = "-fx-font-size: 1em; -fx-text-fill: #d34336;";

    private final FileChooser fileChooser = new FileChooser();
    private final AutoCompleteJFXComboBox source = new AutoCompleteJFXComboBox("");
    private final JFXTextField destination = new JFXTextField();

    public CreateConsumerJFXDialog(final @Nonnull StackPane container, final  @Nonnull BiConsumer<String, String> action, final @Nullable File initialDirectory) {
        super(container, null, CENTER);
        fileChooser.setTitle("Save");
        fileChooser.setInitialDirectory(initialDirectory);
        initialize(action);
        show();
    }

    public void addQueueSuggestion(final @Nonnull String suggestion) {
        source.addSuggestion(suggestion);
    }

    private void initialize(final @Nonnull BiConsumer<String, String> action) {

        final JFXDialogLayout content = new JFXDialogLayout();
        final Label heading = new Label("New Consumer");
        final VBox vBox = new VBox();
        final JFXButton cancel = new JFXButton("I don't know what I'm doing");
        final JFXButton create = new JFXButton("Create");
        final ValidationFacade sourceValidationFacade = new ValidationFacade();
        final ValidationFacade destinationValidationFacade = new ValidationFacade();

        source.setId("source");
        source.setPromptText("select a queue");
        source.setPrefWidth(360);
        source.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && destination.getText().isEmpty()) {
                fileChooser.setInitialFileName(newValue.substring(newValue.lastIndexOf(":") + 1) + ".txt");
            }
        });

        destination.setId("destination");
        destination.setPromptText("select a file");
        destination.setPrefWidth(360);
        destination.setEditable(true);
        destination.setOnMouseClicked(this::openFileChooser);

        setContent(content);

        content.setHeading(heading);
        content.setBody(vBox);
        content.setActions(cancel, create);

        heading.setId("heading");

        vBox.getChildren().addAll(sourceValidationFacade, destinationValidationFacade);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10, 0, 0, 0));

        cancel.setId("cancel");
        cancel.setOnAction(event -> close());

        create.setId("createConsumer");
        create.setOnAction(event -> createConsumer(action));

        sourceValidationFacade.setId("sourceValidationFacade");
        sourceValidationFacade.getValidators().add(new JFXComboBoxRequiredValidator("source uri is required"));
        sourceValidationFacade.getValidators().add(new JmsURIValidator("source uri is not valid"));
        sourceValidationFacade.setControl(source);
        sourceValidationFacade.lookup(ERROR_LABEL).setStyle(ERROR_STYLE);

        destinationValidationFacade.setId("destinationValidationFacade");
        destinationValidationFacade.getValidators().add(new JFXTextFieldRequiredValidator("destination uri is required"));
        destinationValidationFacade.getValidators().add(new FileURIValidator("destination uri is not valid"));
        destinationValidationFacade.setControl(destination);
        destinationValidationFacade.lookup(ERROR_LABEL).setStyle(ERROR_STYLE);

        setId("createConsumerJFXDialog");
    }

    private void openFileChooser(final @Nonnull MouseEvent event) {

        final boolean showSaveDialog = event.getButton() == PRIMARY && destination.getText().isEmpty();

        if (!showSaveDialog) {
            return;
        }

        final File file = fileChooser.showSaveDialog(getDialogContainer().getScene().getWindow());

        if (null != file) {

            final Path path = file.toPath();
            final Map<String, String> options = exists(path) ? singletonMap("fileExist", "Append") : singletonMap("autoCreate", "true");

            destination.setText(fileToCamelUri(file, options));

            if (source.getEditor().getText().isEmpty()) {
                final String pathString = path.toString();
                final int beginIndex = pathString.lastIndexOf("/") + 1;
                final int endIndex = pathString.contains(".") ? pathString.lastIndexOf(".") : pathString.length();
                source.getEditor().setText("jms:" + pathString.substring(beginIndex, endIndex));
            }
        }
    }

    private void createConsumer(final @Nonnull BiConsumer<String, String> action) {

        if (validate(source) && validate(destination)) {
            final String source = this.source.getEditor().getText();
            final String destination = this.destination.getText();
            action.accept(source, destination);
            close();
        }
    }

    private String fileToCamelUri(final @Nonnull File file, final @Nonnull Map<String, String> options) {

        final String optionsString = options.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(joining("&"));

        final char delimiter = options.isEmpty() ? '\0' : '&';

        return format("file:%s?fileName=%s%c%s", file.getParent(), file.getName(), delimiter, optionsString);
    }
}
