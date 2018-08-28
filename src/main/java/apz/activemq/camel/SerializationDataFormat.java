package apz.activemq.camel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

public class SerializationDataFormat implements DataFormat {

    private final static Logger LOGGER = LoggerFactory.getLogger(SerializationDataFormat.class);
    private static final String BODY = "body";

    private final ObjectMapper objectMapper;

    public SerializationDataFormat(final @Nonnull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void marshal(final @Nonnull Exchange exchange, final @Nonnull Object o, final @Nonnull OutputStream outputStream) throws IOException {

        final ObjectNode object = objectMapper.createObjectNode();
        final ObjectNode headers = object.putObject("headers");

        exchange.getOut().getHeaders().forEach((key, value) -> {
            try {
                headers.set(key, objectToJsonNode(value));
            } catch (final IllegalArgumentException e) {
                LOGGER.debug("no serializer is for {}", value);
            }
        });

        if (o instanceof byte[]) {
            try {
                object.set(BODY, objectMapper.readTree((byte[]) o));
            } catch (final JsonProcessingException e) {
                LOGGER.error("error reading tree", e);
            }

        } else if (o instanceof String) {

            try {
                object.set("body", objectMapper.readTree((String) o));
            } catch (final JsonProcessingException e) {
                object.put(BODY, (String) o);
            }
        }

        outputStream.write(object.toString().getBytes());
    }

    @Override
    public Object unmarshal(final @Nonnull Exchange exchange, final @Nonnull InputStream inputStream) throws Exception {

        final JsonNode jsonNode = objectMapper.readTree(inputStream).path("body");
        final JsonNode headers = jsonNode.path("headers");
        final String body = jsonNode.isTextual() ? jsonNode.asText() : jsonNode.toString();

        stream(spliteratorUnknownSize(headers.fields(), ORDERED), false)
                .map(field -> new SimpleEntry<>(field.getKey(), jsonNodeToObject(field.getValue())))
                .forEach(e -> exchange.getOut().getHeaders().put(e.getKey(), e.getValue()));

        return body;
    }

    private JsonNode objectToJsonNode(final @Nullable Object o) {

        if (o == null) {
            return null;

        } else {
            return objectMapper.convertValue(o, JsonNode.class);
        }
    }

    private Object jsonNodeToObject(final @Nullable JsonNode json) {

        if (json == null || json.isNull()) {
            return null;

        } else if (json.isBoolean()) {
            return json.asBoolean();

        } else if (json.isInt()) {
            return json.asInt();

        } else if (json.isLong()) {
            return json.asLong();

        } else if (json.isDouble()) {
            return json.asDouble();

        } else {
            return json.asText();
        }
    }
}