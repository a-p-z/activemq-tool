package apz.activemq.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

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
}
