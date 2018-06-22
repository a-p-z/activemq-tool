package apz.activemq.injection.error;

public class QualifierNotFoundError extends Error {

    public QualifierNotFoundError(final String qualifier) {
        super(qualifier + " not found");
    }
}
