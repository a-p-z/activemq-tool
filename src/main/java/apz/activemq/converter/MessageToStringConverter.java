package apz.activemq.converter;

import apz.activemq.controller.exception.JsonProcessingRuntimeException;
import apz.activemq.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.activemq.broker.jmx.CompositeDataConstants.BODY_LENGTH;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.JMSXGROUP_ID;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.JMSXGROUP_SEQ;

public class MessageToStringConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageToStringConverter.class);

    private final ObjectMapper objectMapper;

    public MessageToStringConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String convert(final Message message) {

        LOGGER.info("converting message {}", message.id);

        final ObjectNode object = objectMapper.createObjectNode();
        final Map<Object, Object> headers = new HashMap<>();

        headers.put("JMSDestination", message.destination.getValue());
        headers.put("JMSReplyTo", message.replyTo.getValue());
        headers.put("JMSType", message.type.getValue());
        headers.put("type", message.mode.getValue());
        headers.put("JMSPriority", message.priority.getValue());
        headers.put("JMSMessageID", message.id.getValue());
        headers.put("JMSTimestamp", message.timestamp.getValue());
        headers.put("JMSCorrelationID", message.correlationId.getValue());
        headers.put("JMSExpiration", message.expiration.getValue());
        headers.put("JMSRedelivered", message.redelivered.getValue());
        headers.put("JMSXDeliveryCount", message.deliveryCount.getValue());
        headers.put(JMSXGROUP_ID, message.groupId.getValue());
        headers.put(JMSXGROUP_SEQ, message.groupSequence.getValue());
        headers.put("JMSXProducerTXID", message.producerTxId.getValue());
        headers.put("JMSActiveMQBrokerInTime", message.activeMQBrokerInTime.getValue());
        headers.put("JMSActiveMQBrokerOutTime", message.activeMQBrokerOutTime.getValue());
        headers.put(BODY_LENGTH, message.size.getValue());
        headers.putAll(message.messageUserProperties.getValue());

        headers.values().removeIf(Objects::isNull);

        object.set("headers", objectMapper.convertValue(headers, ObjectNode.class));

        try {
            object.set("body", objectMapper.readTree(message.body.getValue()));
        } catch (final IOException e) {
            LOGGER.warn("read body as tree failed: {}", e.getMessage());
            object.put("body", message.body.getValue());
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            throw new JsonProcessingRuntimeException(e);
        }
    }
}
