package apz.activemq.camel;

import org.apache.camel.builder.RouteBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

public class ProducerRouteBuilder extends RouteBuilder {

    private final String routeId;
    private final String source;
    private final String destination;
    private final SerializationDataFormat serializationDataFormat;
    private final AtomicLong processed = new AtomicLong(0);

    public ProducerRouteBuilder(
            final @Nonnull String routeId,
            final @Nonnull String source,
            final @Nonnull String destination,
            final @Nonnull SerializationDataFormat serializationDataFormat) {
        this.routeId = routeId;
        this.source = source;
        this.destination = destination;
        this.serializationDataFormat = serializationDataFormat;
    }

    @Override
    public void configure() {

        from(source)
                .routeId(routeId)
                .unmarshal(serializationDataFormat)
                .to(destination)
                .process(exchange -> processed.incrementAndGet());
    }

    public Long getProcessed() {
        return processed.get();
    }
}
