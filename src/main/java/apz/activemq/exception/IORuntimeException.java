package apz.activemq.exception;

import java.io.IOException;

public class IORuntimeException extends RuntimeException {

    public IORuntimeException(final IOException e) {
        super(e.getMessage(), e.getCause());
    }
}
