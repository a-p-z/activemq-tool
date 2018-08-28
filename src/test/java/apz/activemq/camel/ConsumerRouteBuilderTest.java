package apz.activemq.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerRouteBuilderTest extends CamelTestSupport {

    @Spy
    final SerializationDataFormat serializationDataFormat = new SerializationDataFormat(new ObjectMapper());

    private ConsumerRouteBuilder consumerRouteBuilder;

    @Override
    public RouteBuilder createRouteBuilder() {

        consumerRouteBuilder = new ConsumerRouteBuilder(
                "test",
                "seda:queue",
                "mock:file",
                serializationDataFormat);

        return consumerRouteBuilder;
    }

    @Test
    public void whenConsumeTwoMessagesTheyShouldReceivedByDestination() throws InterruptedException {

        final MockEndpoint mockDestination = getMockEndpoint("mock:file");
        mockDestination.expectedMessageCount(2);

        sendBody("seda:queue", "{\"test\":1}");
        sendBody("seda:queue", "{\"test\":2}");

        assertMockEndpointsSatisfied();

        assertThat("processed should be 2", consumerRouteBuilder.getProcessed(), is(2L));
    }

    @Test
    public void whenConsumeAMessageAndAnExceptionOccurredItShouldBeSentToDeadLetter() throws InterruptedException, IOException {

        doThrow(new IOException("simulated exception during marshal")).when(serializationDataFormat).marshal(any(), any(), any());
        
        final MockEndpoint mockDestination = getMockEndpoint("mock:seda:queue.DLQ");
        mockDestination.expectedMessageCount(1);

        sendBody("seda:queue", "{\"test\":1}");

        assertMockEndpointsSatisfied();

        assertThat("processed should be 1", consumerRouteBuilder.getProcessed(), is(1L));
    }

    @Override
    public String isMockEndpoints() {
        // mock all endpoints
        return "*";
    }
}