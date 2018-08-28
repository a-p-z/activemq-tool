package apz.activemq.component;

import apz.activemq.validator.*;
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
import java.util.function.BiConsumer;

import static com.jfoenix.controls.JFXDialog.DialogTransition.CENTER;
import static com.jfoenix.validation.ValidationFacade.validate;
import static java.lang.String.format;
import static javafx.scene.input.MouseButton.PRIMARY;

public class CreateProducerJFXDialog extends JFXDialog {

    private static final String ERROR_LABEL = ".error-label";
    private static final String ERROR_STYLE = "-fx-font-size: 1em; -fx-text-fill: #d34336;";

    private final FileChooser fileChooser = new FileChooser();
    private final JFXTextField source = new JFXTextField();
    private final AutoCompleteJFXComboBox destination = new AutoCompleteJFXComboBox("");

    public CreateProducerJFXDialog(final @Nonnull StackPane container, final @Nonnull BiConsumer<String, String> action, final @Nullable File initialDirectory) {
        super(container, null, CENTER);
        fileChooser.setTitle("Open");
        fileChooser.setInitialDirectory(initialDirectory);
        initialize(action);
        show();
    }

    private void initialize(final @Nonnull BiConsumer<String, String> action) {

        final JFXDialogLayout content = new JFXDialogLayout();
        final Label heading = new Label("New Producer");
        final VBox vBox = new VBox();
        final JFXButton cancel = new JFXButton("I don't know what I'm doing");
        final JFXButton create = new JFXButton("Create");
        final ValidationFacade sourceValidationFacade = new ValidationFacade();
        final ValidationFacade destinationValidationFacade = new ValidationFacade();

        source.setId("source");
        source.setPromptText("select a file");
        source.setPrefWidth(360);
        source.setEditable(true);
        source.setOnMouseClicked(this::openFileChooser);

        destination.setId("destination");
        destination.setPromptText("select a queue");
        destination.setPrefWidth(360);
        destination.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && source.getText().isEmpty()) {
                fileChooser.setInitialFileName(newValue.substring(newValue.lastIndexOf(":") + 1) + ".txt");
            }
        });

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

        create.setId("createProducer");
        create.setOnAction(event -> createProducer(action));

        sourceValidationFacade.setId("sourceValidationFacade");
        sourceValidationFacade.getValidators().add(new JFXTextFieldRequiredValidator("source uri is required"));
        sourceValidationFacade.getValidators().add(new StreamURIValidator("source uri is not valid"));
        sourceValidationFacade.getValidators().add(new StreamURIExistValidator("source uri is not valid"));
        sourceValidationFacade.setControl(source);
        sourceValidationFacade.lookup(ERROR_LABEL).setStyle(ERROR_STYLE);

        destinationValidationFacade.setId("destinationValidationFacade");
        destinationValidationFacade.getValidators().add(new JFXComboBoxRequiredValidator("destination uri is required"));
        destinationValidationFacade.getValidators().add(new JmsURIValidator("destination uri is not valid"));
        destinationValidationFacade.setControl(destination);
        destinationValidationFacade.lookup(ERROR_LABEL).setStyle(ERROR_STYLE);

        setId("createProducerJFXDialog");
    }

    private void openFileChooser(final @Nonnull MouseEvent event) {

        final boolean showSaveDialog = event.getButton() == PRIMARY && source.getText().isEmpty();

        if (!showSaveDialog) {
            return;
        }

        final File file = fileChooser.showOpenDialog(getDialogContainer().getScene().getWindow());

        if (null != file) {
            source.setText(fileToCamelUri(file));
        }

        if (null != file && destination.getEditor().getText().isEmpty()) {
            final String fileName = file.getName();
            final int beginIndex = fileName.lastIndexOf("/") + 1;
            final int endIndex = fileName.contains(".") ? fileName.lastIndexOf(".") : fileName.length();
            destination.getEditor().setText("jms:" + fileName.substring(beginIndex, endIndex));
        }
    }

    private void createProducer(final @Nonnull BiConsumer<String, String> action) {

        if (validate(source) && validate(destination)) {
            final String source = this.source.getText();
            final String destination = this.destination.getEditor().getText();
            action.accept(source, destination);
            close();
        }
    }

    private String fileToCamelUri(final @Nonnull File file) {
        return format("stream:file?fileName=%s", file.getAbsolutePath());
    }
}