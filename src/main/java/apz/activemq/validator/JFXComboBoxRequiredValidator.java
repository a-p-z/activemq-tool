package apz.activemq.validator;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import javax.annotation.Nonnull;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.WARNING;

public class JFXComboBoxRequiredValidator extends ValidatorBase {

    public JFXComboBoxRequiredValidator(final @Nonnull String msg) {
        super();

        setIcon(GlyphsBuilder.create(FontAwesomeIconView.class)
                .glyph(WARNING)
                .style("-fx-glyph-size: 1em; -fx-fill: #d34336;")
                .build());

        setMessage(msg);
    }

    @Override
    protected void eval() {

        final JFXComboBox<?> comboField = (JFXComboBox<?>) srcControl.get();

        boolean hasValue = comboField.getValue() != null;
        boolean editorHasNonEmptyText = comboField.isEditable() &&
                comboField.getEditor().getText() != null &&
                !comboField.getEditor().getText().isEmpty();

        hasErrors.set(!hasValue && !editorHasNonEmptyText);
    }
}
