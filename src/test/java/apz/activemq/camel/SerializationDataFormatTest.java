package apz.activemq.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class SerializationDataFormatTest {

    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @InjectMocks
    private SerializationDataFormat serializationDataFormat;

    @Test
    public void marshalJsonBytes() throws IOException {
        // given
        final byte[] o = "{\"test\": \"marshalBytes\"}".getBytes();
        final OutputStream outputStream = new StringOutputStream();
        final Map<String, Object> headers = new HashMap<>();
        headers.put("JMSTimestamp", new Date(1539179500213L));
        headers.put("JMSPriority", 4);
        headers.put("NullHeader", null);
        given(exchange.getOut()).willReturn(message);
        given(message.getHeaders()).willReturn(headers);

        serializationDataFormat.marshal(exchange, o, outputStream);

        assertThat("outputStream should contain the headers and body", outputStream.toString(), is("{\"headers\":{\"JMSPriority\":4,\"NullHeader\":null,\"JMSTimestamp\":1539179500213},\"body\":{\"test\":\"marshalBytes\"}}"));
    }

    @Test
    public void marshalInvalidJsonBytes() throws IOException {
        // given
        final byte[] o = "}\"test\": \"marshalBytes\"{".getBytes();
        final OutputStream outputStream = new StringOutputStream();
        final Map<String, Object> headers = new HashMap<>();
        headers.put("JMSTimestamp", new Date(1539179500213L));
        headers.put("JMSPriority", 4);
        headers.put("NullHeader", null);
        given(exchange.getOut()).willReturn(message);
        given(message.getHeaders()).willReturn(headers);

        serializationDataFormat.marshal(exchange, o, outputStream);

        assertThat("outputStream should contain the headers and body", outputStream.toString(), is("{\"headers\":{\"JMSPriority\":4,\"NullHeader\":null,\"JMSTimestamp\":1539179500213}}"));
    }

    @Test
    public void marshalStringBytes() throws IOException {
        // given
        final byte[] o = "marshalBytes".getBytes();
        final OutputStream outputStream = new StringOutputStream();
        final Map<String, Object> headers = new HashMap<>();
        headers.put("JMSTimestamp", new Date(1539179500213L));
        headers.put("JMSPriority", 4);
        headers.put("NullHeader", null);
        given(exchange.getOut()).willReturn(message);
        given(message.getHeaders()).willReturn(headers);

        serializationDataFormat.marshal(exchange, o, outputStream);

        assertThat("outputStream should contain the headers", outputStream.toString(), is("{\"headers\":{\"JMSPriority\":4,\"NullHeader\":null,\"JMSTimestamp\":1539179500213}}"));
    }

    @Test
    public void marshalJsonString() throws IOException {
        // given
        final String o = "{\"test\": \"marshalBytes\"}";
        final OutputStream outputStream = new StringOutputStream();
        final Map<String, Object> headers = new HashMap<>();
        headers.put("JMSTimestamp", new Date(1539179500213L));
        headers.put("JMSPriority", 4);
        headers.put("NullHeader", null);
        given(exchange.getOut()).willReturn(message);
        given(message.getHeaders()).willReturn(headers);

        serializationDataFormat.marshal(exchange, o, outputStream);

        assertThat("outputStream should contain the headers and body", outputStream.toString(), is("{\"headers\":{\"JMSPriority\":4,\"NullHeader\":null,\"JMSTimestamp\":1539179500213},\"body\":{\"test\":\"marshalBytes\"}}"));
    }

    @Test
    public void marshalString() throws IOException {
        // given
        final String o = "marshalBytes";
        final OutputStream outputStream = new StringOutputStream();
        final Map<String, Object> headers = new HashMap<>();
        headers.put("JMSTimestamp", new Date(1539179500213L));
        headers.put("JMSPriority", 4);
        headers.put("NullHeader", null);
        given(exchange.getOut()).willReturn(message);
        given(message.getHeaders()).willReturn(headers);

        serializationDataFormat.marshal(exchange, o, outputStream);

        assertThat("outputStream should contain the headers and body", outputStream.toString(), is("{\"headers\":{\"JMSPriority\":4,\"NullHeader\":null,\"JMSTimestamp\":1539179500213},\"body\":\"marshalBytes\"}"));
    }

    @Test
    public void unmarshal() throws Exception {
        // given
        final InputStream inputStream = new ByteArrayInputStream("{\"headers\":{\"booleanHeader\":true,\"doubleHeader\":0.1,\"JMSPriority\":4,\"nullHeader\":null,\"JMSTimestamp\":1539179500213,\"stringHeader\":\"value\"},\"body\":{\"test\":\"marshalBytes\"}}".getBytes());
        final Map<String, Object> headers = new HashMap<>();
        given(exchange.getOut()).willReturn(message);
        given(message.getHeaders()).willReturn(headers);

        final Object body = serializationDataFormat.unmarshal(exchange, inputStream);

        assertThat("headers should contain 6 header", headers.keySet(), hasSize(6));
        assertThat("headers should contain booleanHeader", headers.get("booleanHeader"), is(true));
        assertThat("headers should contain doubleHeader", headers.get("doubleHeader"), is(0.1));
        assertThat("headers should contain JMSPriority", headers.get("JMSPriority"), is(4));
        assertThat("headers should contain nullHeader", headers.get("nullHeader"), nullValue());
        assertThat("headers should contain JMSTimestamp", headers.get("JMSTimestamp"), is(1539179500213L));
        assertThat("headers should contain stringHeader", headers.get("stringHeader"), is("value"));
        assertThat("body should be '{\"test\":\"marshalBytes\"}'", body, is("{\"test\":\"marshalBytes\"}"));
    }

    private class StringOutputStream extends OutputStream {

        private final StringBuilder string = new StringBuilder();

        @Override
        public void write(int b) {
            this.string.append((char) b);

        }

        public String toString() {
            return this.string.toString();
        }
    }
}