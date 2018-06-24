package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.WARNING;

public class JFXTextFieldRequiredValidator extends ValidatorBase {

    public JFXTextFieldRequiredValidator(final String msg) {
        super();

        setIcon(GlyphsBuilder.create(FontAwesomeIconView.class)
                .glyph(WARNING)
                .style("-fx-glyph-size: 1em; -fx-fill: #d34336;")
                .build());

        setMessage(msg);
    }

    @Override
    protected void eval() {

        final JFXTextField jfxTextField = (JFXTextField) srcControl.get();

        hasErrors.set(jfxTextField.getText() == null || jfxTextField.getText().isEmpty());
    }
}
