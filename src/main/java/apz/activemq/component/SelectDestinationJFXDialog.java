package apz.activemq.component;

import apz.activemq.validator.JFXComboBoxRequiredValidator;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.validation.ValidationFacade;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

import static com.jfoenix.validation.ValidationFacade.validate;
import static java.util.Objects.requireNonNull;

public class SelectDestinationJFXDialog extends JFXDialog {

    private final AutoCompleteJFXComboBox autoCompleteJFXComboBox;

    public SelectDestinationJFXDialog(final StackPane container,
                                      final String headingText,
                                      final String source,
                                      final Consumer<String> action) {

        super();

        requireNonNull(container, "container must be not null");
        requireNonNull(headingText, "headingText must be not null");
        requireNonNull(source, "source must be not null");
        requireNonNull(action, "action must be not null");

        final JFXDialogLayout content = new JFXDialogLayout();
        final Label heading = new Label(headingText);
        final JFXButton cancel = new JFXButton("I don't know what I'm doing");
        final JFXButton select = new JFXButton("Select");
        final ValidationFacade validationFacade = new ValidationFacade();
        autoCompleteJFXComboBox = new AutoCompleteJFXComboBox(source);

        validationFacade.setId("validationFacade");
        validationFacade.setControl(autoCompleteJFXComboBox);
        validationFacade.getValidators().add(new JFXComboBoxRequiredValidator("destination si required"));
        validationFacade.lookup(".error-label").setStyle("-fx-font-size: 1em; -fx-text-fill: #d34336;");

        content.setHeading(heading);
        content.setBody(validationFacade);
        content.setActions(cancel, select);

        heading.setId("heading");

        autoCompleteJFXComboBox.setId("autoCompleteJFXComboBox");
        autoCompleteJFXComboBox.setPromptText("Select a destination");

        cancel.setId("cancel");
        cancel.setOnAction(event -> close());

        select.setId("select");
        select.setOnAction(event -> {
            if (validate(autoCompleteJFXComboBox)) {
                close();
                action.accept(autoCompleteJFXComboBox.getEditor().getText());
            }
        });

        autoCompleteJFXComboBox.setId("autoCompleteJFXComboBox");
        autoCompleteJFXComboBox.setPrefWidth(360);

        setDialogContainer(container);
        setContent(content);
        show();
    }

    public void addDestination(final String destination) {

        requireNonNull(destination, "destination must be not null");

        autoCompleteJFXComboBox.addSuggestion(destination);
    }
}
