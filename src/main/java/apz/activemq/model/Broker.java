package apz.activemq.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.activemq.broker.jmx.BrokerViewMBean;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Broker {

    public final StringProperty name = new SimpleStringProperty();
    public final StringProperty version = new SimpleStringProperty();
    public final StringProperty id = new SimpleStringProperty();
    public final StringProperty uptime = new SimpleStringProperty();
    public final DoubleProperty store = new SimpleDoubleProperty();
    public final DoubleProperty memory = new SimpleDoubleProperty();
    public final DoubleProperty temp = new SimpleDoubleProperty();

    public void refresh(final BrokerViewMBean brokerView) {

        requireNonNull(brokerView, "brokerView must be not null");

        name.setValue(brokerView.getBrokerName());
        version.setValue(brokerView.getBrokerVersion());
        id.setValue(brokerView.getBrokerId());
        uptime.setValue(brokerView.getUptime());
        store.setValue(brokerView.getStorePercentUsage() / 100.0);
        memory.setValue(brokerView.getMemoryPercentUsage() / 100.0);
        temp.setValue(brokerView.getTempPercentUsage() / 100.0);
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Broker broker = (Broker) o;

        return Objects.equals(name.getValue(), broker.name.getValue()) &&
                Objects.equals(version.getValue(), broker.version.getValue()) &&
                Objects.equals(id.getValue(), broker.id.getValue()) &&
                Objects.equals(uptime.getValue(), broker.uptime.getValue()) &&
                Objects.equals(store.getValue(), broker.store.getValue()) &&
                Objects.equals(memory.getValue(), broker.memory.getValue()) &&
                Objects.equals(temp.getValue(), broker.temp.getValue());
    }

    @Override
    public int hashCode() {

        return Objects.hash(
                name.getValue(),
                version.getValue(),
                id.getValue(),
                uptime.getValue(),
                store.getValue(),
                memory.getValue(),
                temp.getValue());
    }
}
