package apz.activemq.utils;

import org.apache.activemq.ConnectionClosedException;
import org.apache.activemq.ConnectionFailedException;
import org.apache.activemq.broker.jmx.BrokerViewMBean;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import sun.net.ConnectionResetException;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.BODY_LENGTH;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.JMSXGROUP_SEQ;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.MESSAGE_TEXT;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.STRING_PROPERTIES;

public class ActiveMQJMXService {

    private BrokerViewMBeanMock brokerViewMBeanMock = new BrokerViewMBeanMock();
    private List<QueueViewMBean> queueViewMBeans = US_STATE_CAPITALS.stream()
            .map(QueueViewMBeanMock::new)
            .collect(toList());

    public BrokerViewMBean getBroker() {
        return brokerViewMBeanMock;
    }

    public BrokerViewMBean getNotWorkingBrokerViewMBean() {

        return new BrokerViewMBeanMock() {
            @Override
            public String getBrokerName() {
                throw new RuntimeException("simulating exception getting broker name");
            }
        };
    }

    public List<QueueViewMBean> getQueues() {
        return unmodifiableList(queueViewMBeans);
    }

    public void createQuery(String name) {
        queueViewMBeans.add(new QueueViewMBeanMock(name));
    }

    public void deleteQuery(String name) {
        final List<QueueViewMBean> a = queueViewMBeans.stream()
                .filter(q -> name.equals(q.getName()))
                .collect(Collectors.toList());
        queueViewMBeans.removeAll(a);
    }


    private class BrokerViewMBeanMock implements BrokerViewMBean {

        private int storagePercenteUsage = ThreadLocalRandom.current().nextInt(0, 100);
        private int memoryPercentUsage = ThreadLocalRandom.current().nextInt(0, 100);
        private int tempPercentUsage = ThreadLocalRandom.current().nextInt(0, 100);
        private String uptime = format("%d days %d hours", ThreadLocalRandom.current().nextInt(1, 200), ThreadLocalRandom.current().nextInt(0, 24));
        private final Map<String, String> transportConnectorByType = singletonMap("tcp", "activemq.test.com");

        @Override
        public String getBrokerId() {
            return "ID:activemq.test.com-64874-2439984034094-1:1";
        }

        @Override
        public String getBrokerName() {
            return "localhost";
        }

        @Override
        public String getBrokerVersion() {
            return "5.14.5";
        }

        @Override
        public String getUptime() {
            return uptime;
        }

        @Override
        public long getUptimeMillis() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCurrentConnectionsCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTotalConnectionsCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void gc() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resetStatistics() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void enableStatistics() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void disableStatistics() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isStatisticsEnabled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTotalEnqueueCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTotalDequeueCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTotalConsumerCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTotalProducerCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTotalMessageCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getAverageMessageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMaxMessageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMinMessageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMemoryPercentUsage() {
            return memoryPercentUsage;
        }

        @Override
        public long getMemoryLimit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMemoryLimit(long limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStorePercentUsage() {
            return storagePercenteUsage;
        }

        @Override
        public long getStoreLimit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStoreLimit(long limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTempPercentUsage() {
            return tempPercentUsage;
        }

        @Override
        public long getTempLimit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTempLimit(long limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getJobSchedulerStorePercentUsage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getJobSchedulerStoreLimit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setJobSchedulerStoreLimit(long limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPersistent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSlave() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void terminateJVM(int exitCode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void start() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void restart() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stopGracefully(String connectorName, String queueName, long timeout, long pollInterval) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTopics() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getQueues() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String queryQueues(String filter, int page, int pageSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String queryTopics(String filter, int page, int pageSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompositeData[] browseQueue(String queueName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTemporaryTopics() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTemporaryQueues() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTopicSubscribers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getDurableTopicSubscribers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getInactiveDurableTopicSubscribers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getQueueSubscribers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTemporaryTopicSubscribers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTemporaryQueueSubscribers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTopicProducers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getQueueProducers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTemporaryTopicProducers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getTemporaryQueueProducers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getDynamicDestinationProducers() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String addConnector(String discoveryAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String addNetworkConnector(String discoveryAddress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeConnector(String connectorName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeNetworkConnector(String connectorName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addTopic(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addQueue(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeTopic(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeQueue(String name) {
            queueViewMBeans.remove(this);
        }

        @Override
        public ObjectName createDurableSubscriber(String clientId, String subscriberName, String topicName, String selector) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void destroyDurableSubscriber(String clientId, String subscriberName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reloadLog4jProperties() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getVMURL() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> getTransportConnectors() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTransportConnectorByType(String type) {
            return transportConnectorByType.get(type);
        }

        @Override
        public String getDataDirectory() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName getJMSJobScheduler() {
            throw new UnsupportedOperationException();
        }
    }

    static List<String> US_STATE_CAPITALS = unmodifiableList(asList(
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

    private class QueueViewMBeanMock implements QueueViewMBean {

        private final String name;
        private long queueSize = ThreadLocalRandom.current().nextLong(0, 1000);
        private long consumerCount = ThreadLocalRandom.current().nextLong(0, 50);
        private long enqueueCount = ThreadLocalRandom.current().nextLong(queueSize, queueSize + 1_000_000);
        private long dequeueCount = enqueueCount - queueSize;
        private boolean status = false;

        QueueViewMBeanMock(String name) {
            this.name = name;
        }

        @Override
        public CompositeData getMessage(String messageId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeMessage(String messageId) {
            return status = !status;
        }

        @Override
        public int removeMatchingMessages(String selector) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int removeMatchingMessages(String selector, int maximumMessages) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void purge() {
            queueSize = 0;
        }

        @Override
        public boolean copyMessageTo(String messageId, String destinationName) {
            return status = !status;
        }

        @Override
        public int copyMatchingMessagesTo(String selector, String destinationName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int copyMatchingMessagesTo(String selector, String destinationName, int maximumMessages) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean moveMessageTo(String messageId, String destinationName) {
            return status = !status;
        }

        @Override
        public boolean retryMessage(String messageId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int moveMatchingMessagesTo(String selector, String destinationName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int moveMatchingMessagesTo(String selector, String destinationName, int maximumMessages) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int retryMessages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean doesCursorHaveSpace() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCursorFull() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean doesCursorHaveMessagesBuffered() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getCursorMemoryUsage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCursorPercentUsage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int cursorSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCacheEnabled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String> getMessageGroups() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMessageGroupType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeMessageGroup(String groupName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeAllMessageGroups() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void pause() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resume() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPaused() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void resetStatistics() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getEnqueueCount() {
            return enqueueCount;
        }

        @Override
        public long getDispatchCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getDequeueCount() {
            return dequeueCount;
        }

        @Override
        public long getForwardCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getInFlightCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getExpiredCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getConsumerCount() {
            return consumerCount;
        }

        @Override
        public long getProducerCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getQueueSize() {
            return queueSize;
        }

        @Override
        public long getStoreMessageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompositeData[] browse() {
            long size = queueSize <= 400 ? queueSize : 400;
            final CompositeData[] cdata = new CompositeData[(int) size];
            for (int i = 0; i < cdata.length; i++) {
                cdata[i] = new MessageCompositeData();
            }
            return cdata;
        }

        @Override
        public TabularData browseAsTable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompositeData[] browse(String selector) {
            return new CompositeData[0];
        }

        @Override
        public TabularData browseAsTable(String selector) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String sendTextMessage(String body) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String sendTextMessageWithProperties(String properties) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String sendTextMessage(Map<?, ?> headers, String body) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String sendTextMessage(String body, String user, String password) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String sendTextMessage(Map<String, String> headers, String body, String user, String password) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMemoryPercentUsage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMemoryUsageByteCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMemoryLimit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMemoryLimit(long limit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public float getMemoryUsagePortion() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMemoryUsagePortion(float value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<?> browseMessages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<?> browseMessages(String selector) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMaxEnqueueTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMinEnqueueTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getAverageEnqueueTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getAverageMessageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMaxMessageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getMinMessageSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isProducerFlowControl() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setProducerFlowControl(boolean producerFlowControl) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAlwaysRetroactive() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAlwaysRetroactive(boolean alwaysRetroactive) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBlockedProducerWarningInterval(long blockedProducerWarningInterval) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getBlockedProducerWarningInterval() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMaxProducersToAudit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMaxProducersToAudit(int maxProducersToAudit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMaxAuditDepth() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMaxAuditDepth(int maxAuditDepth) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMaxPageSize() {
            return 400;
        }

        @Override
        public void setMaxPageSize(int pageSize) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isUseCache() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPrioritizedMessages() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setUseCache(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectName[] getSubscriptions() {
            return new ObjectName[0];
        }

        @Override
        public ObjectName getSlowConsumerStrategy() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getOptions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDLQ() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDLQ(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getBlockedSends() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getAverageBlockedTime() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTotalBlockedTime() {
            throw new UnsupportedOperationException();
        }
    }

    private class MessageCompositeData implements CompositeData {

        private final Map<String, Object> cdata = new HashMap<>();

        public MessageCompositeData() {
            cdata.put("JMSMessageID", format("ID:producer.test.com-%s-%s-1:%s:1:1:1",
                    format("%04d", ThreadLocalRandom.current().nextInt(0, 9999)),
                    format("%013d", ThreadLocalRandom.current().nextLong(0, 9999999999999L)),
                    format("%04d", ThreadLocalRandom.current().nextInt(0, 9999))));
            cdata.put("JMSTimestamp", new Date());
            cdata.put("JMSType", "type");
            cdata.put(MESSAGE_TEXT, format("{\"id\": %d}", ThreadLocalRandom.current().nextLong(0, 1000)));
            cdata.put(BODY_LENGTH, ((String) cdata.get(MESSAGE_TEXT)).length());
            cdata.put("JMSDeliveryMode", ThreadLocalRandom.current().nextBoolean() ? "PERSISTENT" : "NON-PERSISTENT");
            cdata.put("JMSPriority", ThreadLocalRandom.current().nextInt(0, 10));
            cdata.put("JMSExpiration", 0L);
            cdata.put("JMSRedelivered", false);
            cdata.put("JMSXDeliveryCount", 0);
            cdata.put(JMSXGROUP_SEQ, 0);
            cdata.put("JMSActiveMQBrokerInTime", 0L);
            cdata.put("JMSActiveMQBrokerOutTime", 0L);
            cdata.put(STRING_PROPERTIES, new StringPropertiesTabularData());
        }

        @Override
        public CompositeType getCompositeType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(String key) {
            return cdata.get(key);
        }

        @Override
        public Object[] getAll(String[] keys) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(String key) {
            return cdata.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return cdata.containsValue(value);
        }

        @Override
        public Collection<?> values() {
            return cdata.values();
        }
    }

    private class StringPropertiesTabularData implements TabularData {

        private final List<String> EXCEPTION_TYPES = unmodifiableList(asList(
                RuntimeException.class.getSimpleName(),
                ConnectionResetException.class.getSimpleName(),
                ConnectionClosedException.class.getSimpleName(),
                ConnectionFailedException.class.getSimpleName(),
                MalformedURLException.class.getSimpleName()
        ));

        private Collection<CompositeData> values = new ArrayList<>(2);

        public StringPropertiesTabularData() {
            values.add(new KeyValueCompositeData("jobId", randomUUID().toString()));
            if (ThreadLocalRandom.current().nextBoolean()) {
                final String exceptionType = EXCEPTION_TYPES.get(ThreadLocalRandom.current().nextInt(0, EXCEPTION_TYPES.size()));
                values.add(new KeyValueCompositeData("exceptionType", exceptionType));
            }
        }

        @Override
        public TabularType getTabularType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] calculateIndex(CompositeData value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object[] key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsValue(CompositeData value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompositeData get(Object[] key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void put(CompositeData value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompositeData remove(Object[] key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(CompositeData[] values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<?> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<?> values() {
            return values;
        }
    }

    private class KeyValueCompositeData implements CompositeData {

        private final Map<String, Object> map = new HashMap<>();

        public KeyValueCompositeData(final String key, final String value) {
            map.put("key", key);
            map.put("value", value);
        }

        @Override
        public CompositeType getCompositeType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get(String key) {
            return map.get(key);
        }

        @Override
        public Object[] getAll(String[] keys) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(String key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public Collection<?> values() {
            return map.values();
        }
    }
}
