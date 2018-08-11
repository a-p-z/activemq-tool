package apz.activemq.model.exception;

import javax.management.openmbean.OpenDataException;

public class OpenDataRuntimeException extends RuntimeException {

    public OpenDataRuntimeException(final OpenDataException e) {
        super(e.getMessage(), e.getCause());
    }
}
