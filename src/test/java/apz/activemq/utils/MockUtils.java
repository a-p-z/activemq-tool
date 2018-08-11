package apz.activemq.utils;

import apz.activemq.model.exception.OpenDataRuntimeException;
import org.apache.activemq.ConnectionClosedException;
import org.apache.activemq.ConnectionFailedException;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.mockito.stubbing.Answer;
import sun.net.ConnectionResetException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndwillAnswer;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class MockUtils {

    private static List<String> EXCEPTION_TYPES = unmodifiableList(asList(
            RuntimeException.class.getSimpleName(),
            ConnectionResetException.class.getSimpleName(),
            ConnectionClosedException.class.getSimpleName(),
            ConnectionFailedException.class.getSimpleName(),
            MalformedURLException.class.getSimpleName()
    ));
    private static final Answer<String> ANSWER_WITH_A_RANDOM_MESSAGE_ID = answer -> format("ID:producer.test.com-%s-%s-1:%s:1:1:1",
            format("%04d", ThreadLocalRandom.current().nextInt(0, 9999)),
            format("%013d", ThreadLocalRandom.current().nextLong(0, 9999999999999L)),
            format("%04d", ThreadLocalRandom.current().nextInt(0, 9999)));
    private static final Answer<Date> ANSWER_WITH_CURRENT_DATE = answer -> new Date();
    private static final Answer<Long> ANSWER_WITH_A_RANDOM_BODY_LENGTH = answer -> ThreadLocalRandom.current().nextLong(0, 1000);
    private static final Answer<String> ANSWER_WITH_A_RANDOM_BODY = answer -> format("{\"id\": %d}", ThreadLocalRandom.current().nextLong(0, 1000));
    private static final Answer<String> ANSWER_WITH_A_RANDOM_MODE = answer -> ThreadLocalRandom.current().nextBoolean() ? "PERSISTENT" : "NON-PERSISTENT";
    private static final Answer<Integer> ANSWER_WITH_A_RANDOM_PRIORITY = answer -> ThreadLocalRandom.current().nextInt(0, 10);
    private static final Answer<String> ANSWER_WITH_A_RANDOM_JOB_ID = answer -> randomUUID().toString();
    private static final Answer<String> ANSWER_WITH_A_RANDOM_EXCEPTION_TYPE = answer -> EXCEPTION_TYPES.get(ThreadLocalRandom.current().nextInt(0, EXCEPTION_TYPES.size()));

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
                   
                    try {
                        given(queue.browse()).willAnswer(invocation -> spyCompositeDataMessages(queueSize));
                    } catch (final OpenDataException e) {
                        throw new OpenDataRuntimeException(e);
                    }
                    return queue;
                })
                .collect(collectingAndwillAnswer(toList(), Collections::unmodifiableList));
    }

    @SuppressWarnings("unchecked")
    public static CompositeData[] spyCompositeDataMessages(final Long num) {

        final CompositeData[] cdataMessages = new CompositeData[num.intValue()];
        final CompositeData message = spy(CompositeData.class);
        final TabularData tabularData = spy(TabularData.class);
        final CompositeData jobId = spy(CompositeData.class);
        final CompositeData exceptionType = spy(CompositeData.class);
        final Collection values = asList(jobId, exceptionType);

        given(message.containsKey(any())).willReturn(true);
        given(message.get("JMSMessageID")).willAnswer(ANSWER_WITH_A_RANDOM_MESSAGE_ID);
        given(message.get("JMSTimestamp")).willAnswer(ANSWER_WITH_CURRENT_DATE);
        given(message.get("JMSType")).willReturn("type");
        given(message.get(BODY_LENGTH)).willAnswer(ANSWER_WITH_A_RANDOM_BODY_LENGTH);
        given(message.get(MESSAGE_TEXT)).willAnswer(ANSWER_WITH_A_RANDOM_BODY);
        given(message.get("JMSDeliveryMode")).willAnswer(ANSWER_WITH_A_RANDOM_MODE);
        given(message.get("JMSPriority")).willAnswer(ANSWER_WITH_A_RANDOM_PRIORITY);
        given(message.get("JMSExpiration")).willReturn(0L);
        given(message.get("JMSRedelivered")).willReturn(false);
        given(message.get("JMSXDeliveryCount")).willReturn(0);
        given(message.get(JMSXGROUP_SEQ)).willReturn(0);
        given(message.get("JMSActiveMQBrokerInTime")).willReturn(0L);
        given(message.get("JMSActiveMQBrokerOutTime")).willReturn(0L);
        given(message.get(STRING_PROPERTIES)).willReturn(tabularData);

        given(tabularData.values()).willReturn(values);
        given(jobId.get("key")).willReturn("jobId");
        given(jobId.get("value")).willAnswer(ANSWER_WITH_A_RANDOM_JOB_ID);
        given(exceptionType.get("key")).willReturn("exceptionType");
        given(exceptionType.get("value")).willAnswer(ANSWER_WITH_A_RANDOM_EXCEPTION_TYPE);

        fill(cdataMessages, message);

        return cdataMessages;
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
