package apz.activemq.controller;

import apz.activemq.ActiveMQTool;
import apz.activemq.exception.IORuntimeException;
import javafx.fxml.FXMLLoader;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;

import static apz.activemq.injection.Injector.resolveDependencies;
import static java.util.Objects.requireNonNull;

public class ControllerFactory {

    public static <T> T newInstance(final @Nonnull Class<T> clazz) {

        requireNonNull(clazz, "clazz must not be null");

        final String view = ("." + ActiveMQTool.class.getPackage().getName() + ".view." + clazz.getSimpleName())
                .replace(".", "/")
                .replace("Controller", ".fxml");

        final URL fxmlResource = ActiveMQTool.class.getResource(view);

        return load(fxmlResource);
    }

    /**
     * Loads an object hierarchy from a FXML document, instance the controller associated with the root object,
     * inject its dependencies and returns the controller
     *
     * @param resource the location used to resolve relative path attribute values
     */
    private static <T> T load(final @Nonnull URL resource) {

        requireNonNull(resource, "resource must not be null");

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(resource);
        try {
            loader.load();
        } catch (final IOException e) {
            throw new IORuntimeException(e);
        }
        final T controller = loader.getController();
        resolveDependencies(controller);
        return controller;
    }
}
