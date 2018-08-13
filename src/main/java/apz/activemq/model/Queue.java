package apz.activemq.model;

import apz.activemq.model.exception.OpenDataRuntimeException;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.openmbean.OpenDataException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class Queue extends RecursiveTreeObject<Queue> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Queue.class);

    public final StringProperty name = new SimpleStringProperty();
    public final LongProperty pending = new SimpleLongProperty();
    public final LongProperty consumers = new SimpleLongProperty();
    public final LongProperty enqueued = new SimpleLongProperty();
    public final LongProperty dequeued = new SimpleLongProperty();

    private final QueueViewMBean queueView;

    public Queue(final QueueViewMBean queueView) {

        requireNonNull(queueView, "queueView must be not null");

        this.name.setValue(queueView.getName());
        this.queueView = queueView;
    }

    public void refresh() {

        LOGGER.info("refreshing {}", name);

        pending.setValue(queueView.getQueueSize());
        consumers.setValue(queueView.getConsumerCount());
        enqueued.setValue(queueView.getEnqueueCount());
        dequeued.setValue(queueView.getDequeueCount());
    }

    public void purge() {

        Optional.ofNullable(queueView).ifPresent(queue -> {
            try {
                LOGGER.info("purging {}", name);
                queue.purge();
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            }
        });
    }

    public List<Message> browse() {

        return Optional.ofNullable(queueView).map(queue -> {
            try {
                LOGGER.info("browsing {}", name);
                return stream(queueView.browse())
                        .map(Message::new)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
            } catch (final OpenDataException e) {
                throw new OpenDataRuntimeException(e);
            }
        }).orElse(emptyList());
    }

    public int getMaxPageSize() {

        LOGGER.info("getting max page size of {}", name);

        return Optional.ofNullable(queueView)
                .map(DestinationViewMBean::getMaxPageSize)
                .orElse(-1);
    }

    public boolean removeMessage(final String messageId) {

        requireNonNull(messageId, "messageId must be not null");

        LOGGER.info("removing message {} in {}", messageId, name);

        boolean result;

        try {
            result = queueView.removeMessage(messageId);

        } catch (final Exception e) {
            LOGGER.error("error removing message " + messageId + " in " + name + ": " + e.getMessage(), e);
            return false;
        }

        if (!result) {
            LOGGER.error("error removing message " + messageId + " in " + name);
        }

        return result;
    }

    public boolean copyMessageTo(final String messageId, final String destination) {

        requireNonNull(messageId, "messageId must be not null");
        requireNonNull(destination, "destination must be not null");

        LOGGER.info("coping message {} from {} to {}", messageId, name, destination);

        boolean result;

        try {
            result = queueView.copyMessageTo(messageId, destination);

        } catch (final Exception e) {
            LOGGER.error("error coping message " + messageId + " from " + name + " to " + destination + ": " + e.getMessage(), e);
            return false;
        }

        if (!result) {
            LOGGER.error("error coping message " + messageId + " from " + name + " to " + destination);
        }

        return result;
    }

    public boolean moveMessageTo(final String messageId, final String destination) {

        requireNonNull(messageId, "messageId must be not null");
        requireNonNull(destination, "destination must be not null");

        LOGGER.info("moving message {} from {} to {}", messageId, name, destination);

        boolean result;

        try {
            result = queueView.moveMessageTo(messageId, destination);

        } catch (final Exception e) {
            LOGGER.error("error moving message " + messageId + " from " + name + " to " + destination + ": " + e.getMessage(), e);
            return false;
        }

        if (!result) {
            LOGGER.error("error moving message " + messageId + " from " + name + " to " + destination);
        }

        return result;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof Queue)) {
            return false;
        }

        final Queue queue = (Queue) o;

        return Objects.equals(name.getValue(), queue.name.getValue()) &&
                Objects.equals(pending.getValue(), queue.pending.getValue()) &&
                Objects.equals(consumers.getValue(), queue.consumers.getValue()) &&
                Objects.equals(enqueued.getValue(), queue.enqueued.getValue()) &&
                Objects.equals(dequeued.getValue(), queue.dequeued.getValue());
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                name.getValue(),
                pending.getValue(),
                consumers.getValue(),
                enqueued.getValue(),
                dequeued.getValue());
    }

    @Override
    public String toString() {
        return "Queue{" +
                "name=" + name.getValue() +
                ", pending=" + pending.getValue() +
                ", consumers=" + consumers.getValue() +
                ", enqueued=" + enqueued.getValue() +
                ", dequeued=" + dequeued.getValue() +
                '}';
    }
}
