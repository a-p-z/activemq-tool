package apz.activemq.injection.error;

public class QualifierAlreadyExistError extends Error {

    public QualifierAlreadyExistError(final String qualifier) {
        super(qualifier + " already exist");
    }
}
