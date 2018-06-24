package apz.activemq.utils;

import org.apache.activemq.broker.jmx.BrokerViewMBean;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;

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

        given(brokerViewMBean.getBrokerId()).willReturn(id);
        given(brokerViewMBean.getBrokerName()).willReturn(name);
        given(brokerViewMBean.getBrokerVersion()).willReturn(version);
        given(brokerViewMBean.getUptime()).willReturn(uptime);
        given(brokerViewMBean.getStorePercentUsage()).willReturn(storePercentUsage);
        given(brokerViewMBean.getMemoryPercentUsage()).willReturn(memoryPercentUsage);
        given(brokerViewMBean.getTempPercentUsage()).willReturn(tempPercentUsage);

        return brokerViewMBean;
    }
}
