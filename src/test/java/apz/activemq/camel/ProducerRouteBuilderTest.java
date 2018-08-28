package apz.activemq.camel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class ProducerRouteBuilderTest extends CamelTestSupport {

    @Spy
    final SerializationDataFormat serializationDataFormat = new SerializationDataFormat(new ObjectMapper());

    private ProducerRouteBuilder producerRouteBuilder;

    @Override
    public RouteBuilder createRouteBuilder() {

        producerRouteBuilder = new ProducerRouteBuilder(
                "test",
                "direct:file",
                "mock:queue",
                serializationDataFormat);

        return producerRouteBuilder;
    }

    @Test
    public void whenProduceTwoMessagesTheyShouldReceivedByDestination() throws InterruptedException {

        final MockEndpoint mockDestination = getMockEndpoint("mock:queue");
        mockDestination.expectedMessageCount(2);

        sendBody("direct:file", "{\"test\":1}");
        sendBody("direct:file", "{\"test\":2}");

        assertMockEndpointsSatisfied();

        assertThat("processed should be 2", producerRouteBuilder.getProcessed(), is(2L));
    }
}