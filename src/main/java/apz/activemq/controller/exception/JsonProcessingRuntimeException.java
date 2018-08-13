package apz.activemq.controller.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class JsonProcessingRuntimeException extends RuntimeException {

    public JsonProcessingRuntimeException(final JsonProcessingException e) {
        super(e.getMessage(), e.getCause());
    }
}
