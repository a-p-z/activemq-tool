package apz.activemq.component;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import static java.util.Objects.requireNonNull;
import static javafx.scene.text.Font.font;
import static javafx.scene.text.FontWeight.BOLD;

public class ConfirmJFXDialog extends JFXDialog {

    public ConfirmJFXDialog(final StackPane container,
                                 final Runnable action,
                                 final String headingText,
                                 final String bodyText,
                                 final String buttonText) {

        super();

        requireNonNull(container, "container must be not null");
        requireNonNull(action, "action must be not null");
        requireNonNull(headingText, "headingText must be not null");
        requireNonNull(bodyText, "bodyText must be not null");
        requireNonNull(buttonText, "buttonText must be not null");

        final JFXDialogLayout content = new JFXDialogLayout();
        final Label heading = new Label(headingText);
        final Label body = new Label(bodyText);
        final JFXButton cancel = new JFXButton("I don't know what I'm doing");
        final JFXButton confirm = new JFXButton(buttonText);

        content.setHeading(heading);
        content.setBody(body);
        content.setActions(cancel, confirm);

        heading.setId("heading");

        body.setId("body");
        body.setFont(font("Verdana, Helvetica, Arial, sans-serif", BOLD, 16));

        cancel.setId("cancel");
        cancel.setOnAction(event -> {
            close();
        });

        confirm.setId("confirm");
        confirm.setOnAction(event -> {
            action.run();
            close();
        });

        setDialogContainer(container);
        setContent(content);
        show();
    }
}
