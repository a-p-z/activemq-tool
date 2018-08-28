package apz.activemq.validator;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.WARNING;
import static java.lang.String.format;

public class JmsURIValidator extends ValidatorBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(JmsURIValidator.class);

    private final String msg;

    public JmsURIValidator(final String msg) {
        super();

        this.msg = msg;

        message.setValue(format("%s: %s", msg, "jms or activemq schema is required"));

        setIcon(GlyphsBuilder.create(FontAwesomeIconView.class)
                .glyph(WARNING)
                .style("-fx-glyph-size: 1em; -fx-fill: #d34336;")
                .build());
    }

    @Override
    protected void eval() {

        final JFXComboBox<?> comboField = (JFXComboBox<?>) srcControl.get();
        final String text = comboField.getEditor().getText();

        try {
            final String schema = URI.create(text).getScheme();
            hasErrors.set(!"jms".equals(schema) && !"activemq".equals(schema));

        } catch (final IllegalArgumentException e) {
            hasErrors.setValue(true);
            message.setValue(format("%s: jms:{queueName}[?options] or activemq:{queueName}[?options]", msg));
            LOGGER.debug("error parsing uri", e);
        }
    }
}
