package apz.activemq.utils;

import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
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

    public static List<QueueViewMBean> spyQueueViewMBean(final Long maxSize, final Long minimumMessage, final Long maximumMessage) {

        return US_STATE_CAPITALS.stream()
                .limit(maxSize)
                .map(name -> {
                    final QueueViewMBean queue = spy(QueueViewMBean.class);
                    final Long queueSize = ThreadLocalRandom.current().nextLong(minimumMessage, maximumMessage);

                    given(queue.getName()).willReturn(name);
                    given(queue.getQueueSize()).willReturn(queueSize);
                    given(queue.getConsumerCount()).willAnswer(invocation -> ThreadLocalRandom.current().nextLong(0, 50));
                    given(queue.getEnqueueCount()).willAnswer(invocation -> ThreadLocalRandom.current().nextLong(0, 100));
                    given(queue.getDequeueCount()).willAnswer(invocation -> ThreadLocalRandom.current().nextLong(0, 100));
                    given(queue.getMaxPageSize()).willReturn(400);

                    return queue;
                })
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private static List<String> US_STATE_CAPITALS = unmodifiableList(asList(
            "queue.test.alabama",
            "queue.test.alaska",
            "queue.test.arizona",
            "queue.test.arkansas",
            "queue.test.california",
            "queue.test.colorado",
            "queue.test.connecticut",
            "queue.test.delaware",
            "queue.test.florida",
            "queue.test.georgia",
            "queue.test.hawaii",
            "queue.test.idaho",
            "queue.test.illinois",
            "queue.test.indiana",
            "queue.test.iowa",
            "queue.test.kansas",
            "queue.test.kentucky",
            "queue.test.louisiana",
            "queue.test.maine",
            "queue.test.maryland",
            "queue.test.massachusetts",
            "queue.test.michigan",
            "queue.test.minnesota",
            "queue.test.mississippi",
            "queue.test.missouri",
            "queue.test.montana",
            "queue.test.nebraska",
            "queue.test.nevada",
            "queue.test.new.hampshire",
            "queue.test.new.jersey",
            "queue.test.new.mexico",
            "queue.test.new.york",
            "queue.test.north.carolina",
            "queue.test.north.dakota",
            "queue.test.ohio",
            "queue.test.oklahoma",
            "queue.test.oregon",
            "queue.test.pennsylvania",
            "queue.test.rhode.island",
            "queue.test.south.carolina",
            "queue.test.south.dakota",
            "queue.test.tennessee",
            "queue.test.texas",
            "queue.test.utah",
            "queue.test.vermont",
            "queue.test.virginia",
            "queue.test.washington",
            "queue.test.west.virginia",
            "queue.test.wisconsin",
            "queue.test.wyoming"));
}
