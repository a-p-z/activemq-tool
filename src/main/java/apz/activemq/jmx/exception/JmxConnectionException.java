package apz.activemq.jmx.exception;

import java.io.IOException;

public class JmxConnectionException extends Exception {

    private final String host;
    private final Integer port;

    public JmxConnectionException(final String host, final Integer port, final IOException cause) {
        super(cause);
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }
}
