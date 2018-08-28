package apz.activemq.validator;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.WARNING;
import static java.lang.String.format;

public class StreamURIExistValidator extends ValidatorBase {

    private final String msg;

    public StreamURIExistValidator(final @Nonnull String msg) {
        super();

        this.msg = msg;

        setIcon(GlyphsBuilder.create(FontAwesomeIconView.class)
                .glyph(WARNING)
                .style("-fx-glyph-size: 1em; -fx-fill: #d34336;")
                .build());
    }

    @Override
    protected void eval() {

        final JFXTextField textField = (JFXTextField) srcControl.get();
        final String text = textField.getText();

        final Optional<Path> path = getPath(text);

        if (!path.isPresent()) {
            hasErrors.set(true);
            message.setValue(format("%s: %s", msg, "fileName param is required"));
            return;
        }

        if (!Files.exists((path.get()))) {
            hasErrors.set(true);
            message.setValue(format("%s: %s", msg, path.get() + " not exists"));
            return;
        }

        hasErrors.set(false);
    }

    public static Optional<Path> getPath(final @Nonnull String uri) {

        final String[] splittedUri = uri.split("\\?");

        if (splittedUri.length != 2) {
            return Optional.empty();
        }

        final String queryString = splittedUri[1];
        final String[] params = queryString.split("&");

        for (final String param : params) {

            if (param.startsWith("fileName")) {

                final String[] paramSplitted = param.split("=");

                if (paramSplitted.length != 2) {
                    return Optional.empty();
                }

                return Optional.ofNullable(Paths.get(paramSplitted[1]));
            }
        }

        return Optional.empty();
    }
}
