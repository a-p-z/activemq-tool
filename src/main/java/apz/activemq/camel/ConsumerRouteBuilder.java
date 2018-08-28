package apz.activemq.camel;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.camel.LoggingLevel.ERROR;

public class ConsumerRouteBuilder extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRouteBuilder.class);

    private final String routeId;
    private final String source;
    private final String destination;
    private final SerializationDataFormat serializationDataFormat;
    private final String deadLetter;
    private final AtomicLong processed = new AtomicLong(0);

    public ConsumerRouteBuilder(
            final @Nonnull String routeId,
            final @Nonnull String source,
            final @Nonnull String destination,
            final @Nonnull SerializationDataFormat serializationDataFormat) {
        this.routeId = routeId;
        this.source = source;
        this.destination = destination;
        this.serializationDataFormat = serializationDataFormat;
        this.deadLetter = source.split("\\?")[0] + ".DLQ";
    }

    @Override
    public void configure() {

        onException(Exception.class)
                .handled(true)
                .useOriginalMessage()
                .log(ERROR, LOGGER, "${exception.class.canonicalName} consuming message: ${exception.message}")
                .setHeader("ExceptionType", simple("${exception.class.canonicalName}"))
                .setHeader("ExceptionMessage", simple("${exception.message}"))
                .to(deadLetter);

        from(source)
                .routeId(routeId)
                .process(exchange -> processed.incrementAndGet())
                .marshal(serializationDataFormat)
                .transform(body().append("\n"))
                .to(destination);
    }

    public Long getProcessed() {
        return processed.get();
    }
}
