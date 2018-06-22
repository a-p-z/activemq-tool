package apz.activemq.injection;

import apz.activemq.injection.error.QualifierAlreadyExistError;
import apz.activemq.injection.error.QualifierNotFoundError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

public class Injector {

    private final static Logger LOGGER = LoggerFactory.getLogger(Injector.class);
    private final static Map<String, Object> REGISTRY = new ConcurrentHashMap<>();

    private Injector() {
    }

    /**
     * Inject annotated dependencies using registered singletons and external objects
     *
     * @param object  the object
     * @param objects external objects
     */
    public static void resolveDependencies(final Object object, final Object... objects) {

        requireNonNull(object, "object must be not null");
        requireNonNull(objects, "objects must be not null");

        LOGGER.debug("injecting dependencies in {}", object);

        Stream.of(object.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .peek(field -> field.setAccessible(true))
                .forEach(field -> injectAnnotatedField(field, object, objects));
    }

    /**
     * Register the instance of a class as a singleton using a qualifier
     *
     * @param qualifier the qualifier
     * @param object    the object to register
     */
    public static void register(final String qualifier, final Object object) {

        requireNonNull(qualifier, "qualifier must be not null");
        requireNonNull(object, "object must be not null");

        LOGGER.debug("registering {} with qualifier {}", object, qualifier);

        if (REGISTRY.containsKey(qualifier)) {
            throw new QualifierAlreadyExistError(qualifier);
        }

        REGISTRY.put(qualifier, object);
    }

    /**
     * Removes all objects from registry
     * The registry will be empty after this call returns.
     */
    public static void clearRegistry() {
        REGISTRY.clear();
    }

    /**
     * Return an object registered using a qualifier
     *
     * @param qualifier the qualifier
     * @param clazz     the class of the object
     * @return the registered object
     */
    public static <T> T get(final String qualifier, final Class<T> clazz) {

        requireNonNull(qualifier, "qualifier must be not null");
        requireNonNull(clazz, "clazz must be not null");

        LOGGER.debug("getting {} with qualifier {}", clazz, qualifier);

        if (!REGISTRY.containsKey(qualifier)) {
            throw new QualifierNotFoundError(qualifier);
        }

        return clazz.cast(REGISTRY.get(qualifier));
    }

    /**
     * Inject annotated dependency using registered singletons and external objects
     *
     * @param field   the field
     * @param object  the object
     * @param objects external objects
     */
    private static void injectAnnotatedField(final Field field, final Object object, final Object... objects) {

        requireNonNull(field, "field must be not null");
        requireNonNull(object, "object must be not null");
        requireNonNull(objects, "objects must be not null");

        final Object dependency = stream(objects)
                .filter(o -> field.getClass().isInstance(o))
                .findFirst()
                .orElseGet(() -> {
                    final Inject inject = field.getAnnotation(Inject.class);
                    final String qualifier = inject.qualifier().isEmpty() ? field.getName() : inject.qualifier();
                    return field.getType().cast(REGISTRY.get(qualifier));
                });

        try {
            field.set(object, dependency);
        } catch (final IllegalAccessException e) {
            throw new Error(e);
        }
    }
}
