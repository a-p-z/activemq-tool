package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.WARNING;

public class JMXServiceURLValidator extends ValidatorBase {

    public JMXServiceURLValidator() {
        super();

        setIcon(GlyphsBuilder.create(FontAwesomeIconView.class)
                .glyph(WARNING)
                .style("-fx-glyph-size: 1em; -fx-fill: #d34336;")
                .build());
    }

    @Override
    protected void eval() {

        final JFXTextField textField = (JFXTextField) srcControl.get();

        hasErrors.set(false);

        try {
            new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + textField.getText());
        } catch (final MalformedURLException e) {
            hasErrors.set(true);
            message.setValue(e.getMessage());
        }
    }
}
