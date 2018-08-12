package apz.activemq.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.openmbean.CompositeData;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.BODY_LENGTH;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.BODY_PREVIEW;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.JMSXGROUP_ID;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.JMSXGROUP_SEQ;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.MESSAGE_TEXT;
import static org.apache.activemq.broker.jmx.CompositeDataHelper.getMessageUserProperties;

public class Message extends RecursiveTreeObject<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Message.class);

    public final StringProperty id;
    public final StringProperty correlationId;
    public final StringProperty mode;
    public final IntegerProperty priority;
    public final BooleanProperty redelivered;
    public final StringProperty replyTo;
    public final ObjectProperty<Date> timestamp;
    public final StringProperty type;

    public final SimpleStringProperty destination;
    public final LongProperty expiration;
    public final IntegerProperty deliveryCount;
    public final StringProperty groupId;
    public final IntegerProperty groupSequence;
    public final StringProperty producerTxId;
    public final LongProperty activeMQBrokerInTime;
    public final LongProperty activeMQBrokerOutTime;

    public final ObjectProperty<Map> messageUserProperties;
    public final StringProperty body;
    public final LongProperty size;

    public Message(final CompositeData cdata) {

        requireNonNull(cdata, "cdata must be not null");

        // Message Attributes Accessed as Properties:
        final String destination = get(cdata, "JMSDestination", String.class); // Destination used by the producer
        final String replyTo = get(cdata, "JMSReplyTo", String.class);
        final String type = get(cdata, "JMSType", String.class);
        final String mode = get(cdata, "JMSDeliveryMode", String.class); // Indicator if messages should be persisted
        final Integer priority = get(cdata, "JMSPriority", Integer.class); // Value from 0-9
        final String messageId = get(cdata, "JMSMessageID", String.class); // Unique identifier for the message
        final Date timestamp = get(cdata, "JMSTimestamp", Date.class); // Time in milliseconds
        final String correlationId = get(cdata, "JMSCorrelationID", String.class);
        final Long expiration = get(cdata, "JMSExpiration", Long.class); // Time in milliseconds to expire the message. A value of 0 means never expire
        final Boolean redelivered = get(cdata, "JMSRedelivered", Boolean.class); // true if the message is being resent to the consumer, persisted via persistJMSRedelivered

        // JMS Defined:
        final Integer deliveryCount = get(cdata, "JMSXDeliveryCount", Integer.class); // Number of attempts to send the message
        final String groupId = get(cdata, JMSXGROUP_ID, String.class); // Identity of the message group
        final Integer groupSequence = get(cdata, JMSXGROUP_SEQ, Integer.class); // Sequence number of the message
        final String producerTxId = get(cdata, "JMSXProducerTXID", String.class); // Transaction identifier

        // ActiveMQ Defined:
        final Long activeMQBrokerInTime = get(cdata, "JMSActiveMQBrokerInTime", Long.class); // Time stamp (in milliseconds) for when the message arrived at the broker
        final Long activeMQBrokerOutTime = get(cdata, "JMSActiveMQBrokerOutTime", Long.class); // Time stamp (in milliseconds) for when the message left the broker

        // Message User Properties:
        final Map messageUserProperties = getMessageUserProperties(cdata);

        final Long size = get(cdata, BODY_LENGTH, Long.class);
        final String bodyPreview = getBodyPreview(cdata);
        final String body = get(cdata, MESSAGE_TEXT, String.class);

        this.id = new SimpleStringProperty(messageId);
        this.correlationId = new SimpleStringProperty(correlationId);
        this.mode = new SimpleStringProperty(mode);
        this.priority = new SimpleIntegerProperty(null != priority ? priority : 0);
        this.redelivered = new SimpleBooleanProperty(null != redelivered);
        this.replyTo = new SimpleStringProperty(replyTo);
        this.timestamp = new SimpleObjectProperty<>(timestamp);
        this.type = new SimpleStringProperty(type);

        this.destination = new SimpleStringProperty(destination);
        this.expiration = new SimpleLongProperty(null != expiration ? expiration : 0L);
        this.deliveryCount = new SimpleIntegerProperty(null != deliveryCount ? deliveryCount : 0);
        this.groupId = new SimpleStringProperty(groupId);
        this.groupSequence = new SimpleIntegerProperty(null != groupSequence ? groupSequence : 0);
        this.producerTxId = new SimpleStringProperty(producerTxId);
        this.activeMQBrokerInTime = new SimpleLongProperty(null != activeMQBrokerInTime ? activeMQBrokerInTime : 0L);
        this.activeMQBrokerOutTime = new SimpleLongProperty(null != activeMQBrokerOutTime ? activeMQBrokerOutTime : 0L);

        this.messageUserProperties = new SimpleObjectProperty<>(messageUserProperties);
        this.body = new SimpleStringProperty(null != bodyPreview ? bodyPreview : body);
        this.size = new SimpleLongProperty(null != size ? size : this.body.getValue().length());
    }

    private static <T> T get(final CompositeData cdata, final String key, final Class<T> clazz) {

        requireNonNull(cdata, "cdata must be not null");
        requireNonNull(key, "key must be not null");
        requireNonNull(clazz, "clazz must be not null");

        if (cdata.containsKey(key) && clazz.isInstance(cdata.get(key))) {
            return clazz.cast(cdata.get(key));
        } else if (cdata.containsKey(key) && null != cdata.get(key)) {
            LOGGER.warn("cdata[{}] = {}, {} is not instance of {}", key, cdata.get(key), cdata.get(key).getClass(), clazz);
            return null;
        } else {
            return null;
        }
    }

    private String getBodyPreview(final CompositeData cdata) {

        requireNonNull(cdata, "cdata must be not null");

        final Byte[] a = get(cdata, BODY_PREVIEW, Byte[].class);

        if (null != a) {
            final byte[] b = new byte[a.length];
            for (int i = 0; i < b.length; i++) {
                b[i] = a[i];
            }

            return new String(b);
        }

        return null;
    }

    public boolean contains(final Collection<String> keys, final String s) {

        requireNonNull(keys, "keys must be not null");
        requireNonNull(s, "s must be not null");

        return keys.stream()
                .map(this::getValue)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .anyMatch(v -> v.contains(s));
    }

    private String getValue(final String key) {

        requireNonNull(key, "key must be not null");

        final String value;

        switch (key) {
            case "Message id":
                value = id.getValue();
                break;

            case "Correlation id":
                value = correlationId.getValue();
                break;

            case "Mode":
                value = mode.getValue();
                break;

            case "Priority":
                value = priority.getValue().toString();
                break;
            case "Redelivered":
                value = redelivered.getValue().toString();
                break;

            case "Reply to":
                value = replyTo.getValue();
                break;

            case "Timestamp":
                value = timestamp.getValue().toString();
                break;

            case "Type":
                value = type.getValue();
                break;

            case "Destination":
                value = destination.getValue();
                break;

            case "Expiration":
                value = expiration.getValue().toString();
                break;

            case "Delivery count":
                value = deliveryCount.getValue().toString();
                break;

            case "Group id":
                value = groupId.getValue();
                break;

            case "Group sequence":
                value = groupSequence.getValue().toString();
                break;

            case "Producer TX id":
                value = producerTxId.getValue();
                break;

            case "ActiveMQ broker in time":
                value = activeMQBrokerInTime.getValue().toString();
                break;

            case "ActiveMQ broker out time":
                value = activeMQBrokerOutTime.getValue().toString();
                break;

            case "Size":
                value = size.getValue().toString();
                break;

            case "Body":
                value = body.getValue();
                break;

            default:
                value = messageUserProperties.getValue().containsKey(key) && null != messageUserProperties.getValue().get(key) ?
                        messageUserProperties.getValue().get(key).toString() :
                        null;
                break;
        }

        return value;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Message message = (Message) o;

        return Objects.equals(id.getValue(), message.id.getValue()) &&
                Objects.equals(correlationId.getValue(), message.correlationId.getValue()) &&
                Objects.equals(mode.getValue(), message.mode.getValue()) &&
                Objects.equals(priority.getValue(), message.priority.getValue()) &&
                Objects.equals(redelivered.getValue(), message.redelivered.getValue()) &&
                Objects.equals(replyTo.getValue(), message.replyTo.getValue()) &&
                Objects.equals(timestamp.getValue(), message.timestamp.getValue()) &&
                Objects.equals(type.getValue(), message.type.getValue()) &&
                Objects.equals(destination.getValue(), message.destination.getValue()) &&
                Objects.equals(expiration.getValue(), message.expiration.getValue()) &&
                Objects.equals(deliveryCount.getValue(), message.deliveryCount.getValue()) &&
                Objects.equals(groupId.getValue(), message.groupId.getValue()) &&
                Objects.equals(groupSequence.getValue(), message.groupSequence.getValue()) &&
                Objects.equals(producerTxId.getValue(), message.producerTxId.getValue()) &&
                Objects.equals(activeMQBrokerInTime.getValue(), message.activeMQBrokerInTime.getValue()) &&
                Objects.equals(activeMQBrokerOutTime.getValue(), message.activeMQBrokerOutTime.getValue()) &&
                Objects.equals(messageUserProperties.getValue(), message.messageUserProperties.getValue()) &&
                Objects.equals(size.getValue(), message.size.getValue()) &&
                Objects.equals(body.getValue(), message.body.getValue());
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                id.getValue(),
                correlationId.getValue(),
                mode.getValue(),
                priority.getValue(),
                redelivered.getValue(),
                replyTo.getValue(),
                timestamp.getValue(),
                type.getValue(),
                destination.getValue(),
                expiration.getValue(),
                deliveryCount.getValue(),
                groupId.getValue(),
                groupSequence.getValue(),
                producerTxId.getValue(),
                activeMQBrokerInTime.getValue(),
                activeMQBrokerOutTime.getValue(),
                messageUserProperties.getValue(),
                size.getValue(),
                body.getValue());
    }
}
