package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.WARNING;
import static java.lang.String.format;

public class FileURIValidator extends ValidatorBase {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileURIValidator.class);

    private final String msg;

    public FileURIValidator(final String msg) {
        super();

        this.msg = msg;

        message.setValue(format("%s: %s", msg, "file schema is required"));

        setIcon(GlyphsBuilder.create(FontAwesomeIconView.class)
                .glyph(WARNING)
                .style("-fx-glyph-size: 1em; -fx-fill: #d34336;")
                .build());
    }

    @Override
    protected void eval() {

        final JFXTextField textField = (JFXTextField) srcControl.get();
        final String text = textField.getText();

        final URI uri;

        try {
            uri = URI.create(text);

        } catch (final IllegalArgumentException e) {
            hasErrors.set(true);
            message.setValue(format("%s: file:{directoryName}?fileName={filename}[&options]", msg));
            LOGGER.debug("error parsing uri", e);
            return;
        }

        if (!"file".equals(uri.getScheme())) {
            hasErrors.set(true);
            message.setValue(format("%s: %s", msg, "file schema is required"));
            return;
        }

        hasErrors.set(false);
    }
}
