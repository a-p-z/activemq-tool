package apz.activemq.utils;

import org.apache.activemq.broker.jmx.BrokerViewMBean;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MockUtils {

    public static BrokerViewMBean spyBrokerViewMBean(
            final String id,
            final String name,
            final String version,
            final String uptime,
            final int storePercentUsage,
            final int memoryPercentUsage,
            final int tempPercentUsage) {

        final BrokerViewMBean brokerViewMBean = spy(BrokerViewMBean.class);

        when(brokerViewMBean.getBrokerId()).thenReturn(id);
        when(brokerViewMBean.getBrokerName()).thenReturn(name);
        when(brokerViewMBean.getBrokerVersion()).thenReturn(version);
        when(brokerViewMBean.getUptime()).thenReturn(uptime);
        when(brokerViewMBean.getStorePercentUsage()).thenReturn(storePercentUsage);
        when(brokerViewMBean.getMemoryPercentUsage()).thenReturn(memoryPercentUsage);
        when(brokerViewMBean.getTempPercentUsage()).thenReturn(tempPercentUsage);

        return brokerViewMBean;
    }
}
