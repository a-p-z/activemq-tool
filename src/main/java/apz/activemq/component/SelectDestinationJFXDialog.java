package apz.activemq.component;

import apz.activemq.validator.JFXComboBoxRequiredValidator;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.validation.ValidationFacade;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static com.jfoenix.validation.ValidationFacade.validate;

public class SelectDestinationJFXDialog extends JFXDialog {

    private final AutoCompleteJFXComboBox autoCompleteJFXComboBox;

    public SelectDestinationJFXDialog(final @Nonnull StackPane container,
                                      final @Nonnull String headingText,
                                      final @Nonnull String source,
                                      final @Nonnull Consumer<String> action) {

        super();

        final JFXDialogLayout content = new JFXDialogLayout();
        final Label heading = new Label(headingText);
        final JFXButton cancel = new JFXButton("I don't know what I'm doing");
        final JFXButton select = new JFXButton("Select");
        final ValidationFacade validationFacade = new ValidationFacade();

        autoCompleteJFXComboBox = new AutoCompleteJFXComboBox(source);
        autoCompleteJFXComboBox.setId("autoCompleteJFXComboBox");
        autoCompleteJFXComboBox.setPromptText("Select a destination");
        autoCompleteJFXComboBox.setId("autoCompleteJFXComboBox");
        autoCompleteJFXComboBox.setPrefWidth(360);

        validationFacade.setId("validationFacade");
        validationFacade.setControl(autoCompleteJFXComboBox);
        validationFacade.getValidators().add(new JFXComboBoxRequiredValidator("destination is required"));
        validationFacade.lookup(".error-label").setStyle("-fx-font-size: 1em; -fx-text-fill: #d34336;");

        heading.setId("heading");

        content.setHeading(heading);
        content.setBody(validationFacade);
        content.setActions(cancel, select);

        cancel.setId("cancel");
        cancel.setOnAction(event -> close());

        select.setId("select");
        select.setOnAction(event -> {
            if (validate(autoCompleteJFXComboBox)) {
                close();
                action.accept(autoCompleteJFXComboBox.getEditor().getText());
            }
        });

        setDialogContainer(container);
        setContent(content);
        show();
    }

    public void addDestination(final @Nonnull String destination) {
        autoCompleteJFXComboBox.addSuggestion(destination);
    }
}
