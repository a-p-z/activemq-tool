package apz.activemq.converter;

import apz.activemq.controller.exception.JsonProcessingRuntimeException;
import apz.activemq.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nonnull;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.apache.activemq.broker.jmx.CompositeDataConstants.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MessageToStringConverterTest {

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private MessageToStringConverter messageToStringConverter;

    @Test
    public void convert() {
        // given
        final Message message = spyMessage("{\"id\":691}");

        // when
        final String s = messageToStringConverter.convert(message);

        // then
        assertThat("", s, is("{\"headers\":{\"JMSType\":\"type\",\"JMSMessageID\":\"ID:producer.test.com-6424-7472907250119-1:4502:1:1:1\",\"JMSExpiration\":0,\"JMSRedelivered\":true,\"JMSXGroupSeq\":0,\"type\":\"PERSISTENT\",\"JMSPriority\":4,\"jobId\":\"0593844d-e9e3-4473-b3ae-d7b2204c75c0\",\"JMSXDeliveryCount\":0,\"JMSActiveMQBrokerInTime\":0,\"BodyLength\":10,\"JMSActiveMQBrokerOutTime\":0},\"body\":{\"id\":691}}"));
    }

    @Test
    public void convertWithIOException() {
        // given
        final Message message = spyMessage("}\"id\":691{");

        // when
        final String s = messageToStringConverter.convert(message);

        // then
        assertThat("", s, is("{\"headers\":{\"JMSType\":\"type\",\"JMSMessageID\":\"ID:producer.test.com-6424-7472907250119-1:4502:1:1:1\",\"JMSExpiration\":0,\"JMSRedelivered\":true,\"JMSXGroupSeq\":0,\"type\":\"PERSISTENT\",\"JMSPriority\":4,\"jobId\":\"0593844d-e9e3-4473-b3ae-d7b2204c75c0\",\"JMSXDeliveryCount\":0,\"JMSActiveMQBrokerInTime\":0,\"BodyLength\":10,\"JMSActiveMQBrokerOutTime\":0},\"body\":\"}\\\"id\\\":691{\"}"));
    }

    @Test(expected = JsonProcessingRuntimeException.class)
    public void convertWithJsonProcessingException() throws JsonProcessingException {
        // given
        final Message message = spyMessage("{\"id\":691}");
        JsonProcessingException jsonProcessingException = mock(JsonProcessingException.class);
        given(objectMapper.writeValueAsString(any())).willThrow(jsonProcessingException);

        // when
        messageToStringConverter.convert(message);
    }

    @SuppressWarnings("unchecked")
    private Message spyMessage(final @Nonnull String body) {

        final CompositeData cdata = spy(CompositeData.class);
        final TabularData tabularData = spy(TabularData.class);
        final CompositeData jobId = spy(CompositeData.class);
        final CompositeData exceptionType = spy(CompositeData.class);
        final Collection values = asList(jobId, exceptionType);

        given(cdata.containsKey(any())).willReturn(true);
        given(cdata.get("JMSMessageID")).willReturn("ID:producer.test.com-6424-7472907250119-1:4502:1:1:1");
        given(cdata.get("JMSTimestamp")).willReturn(1539274347993L);
        given(cdata.get("JMSType")).willReturn("type");
        given(cdata.get(BODY_LENGTH)).willReturn(26);
        given(cdata.get(MESSAGE_TEXT)).willReturn(body);
        given(cdata.get("JMSDeliveryMode")).willReturn("PERSISTENT");
        given(cdata.get("JMSPriority")).willReturn(4);
        given(cdata.get("JMSExpiration")).willReturn(0L);
        given(cdata.get("JMSRedelivered")).willReturn(false);
        given(cdata.get("JMSXDeliveryCount")).willReturn(0);
        given(cdata.get(JMSXGROUP_SEQ)).willReturn(0);
        given(cdata.get("JMSActiveMQBrokerInTime")).willReturn(0L);
        given(cdata.get("JMSActiveMQBrokerOutTime")).willReturn(0L);
        given(cdata.get(STRING_PROPERTIES)).willReturn(tabularData);
        given(tabularData.values()).willReturn(values);
        given(jobId.get("key")).willReturn("jobId");
        given(jobId.get("value")).willReturn("0593844d-e9e3-4473-b3ae-d7b2204c75c0");

        return new Message(cdata);
    }
}