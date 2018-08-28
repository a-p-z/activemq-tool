package apz.activemq;

import apz.activemq.camel.SerializationDataFormat;
import apz.activemq.converter.MessageToStringConverter;
import apz.activemq.jmx.JmxClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import org.apache.camel.CamelContext;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.Configuration.configureCamelContext;
import static apz.activemq.Configuration.configureHostServices;
import static apz.activemq.Configuration.configureJmxClient;
import static apz.activemq.Configuration.configureMessageToStringConverter;
import static apz.activemq.Configuration.configureObjectMapper;
import static apz.activemq.Configuration.configureScheduledExecutorService;
import static apz.activemq.Configuration.configureSerializationDataFormat;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.get;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ConfigurationTest {

    @Before
    public void before() {
        clearRegistry();
    }

    @Test
    public void whenConfigureHostServiceItShouldBeRegistered() {
        // when
        final HostServicesDelegate hostServices = configureHostServices(mock(Application.class));

        // then
        final HostServicesDelegate registeredHostServices = get("hostServices", HostServicesDelegate.class);
        assertThat("hostService should not be null", hostServices, notNullValue());
        assertThat("hostService should be equal to registered hostServices", hostServices, is(registeredHostServices));
    }

    @Test
    public void whenConfigureJmxClientItShouldBeRegistered() {
        // when
        final JmxClient jmxClient = configureJmxClient();

        // then
        final JmxClient registeredJmxClient = get("jmxClient", JmxClient.class);
        assertThat("jmxClient should not be null", jmxClient, notNullValue());
        assertThat("jmxClient should be equal to registered jmxClient", jmxClient, is(registeredJmxClient));
    }

    @Test
    public void whenConfigureScheduledExecutorServiceItShouldBeRegistered() {
        // when
        final ScheduledExecutorService scheduledExecutorService = configureScheduledExecutorService();

        // then
        final ScheduledExecutorService registeredScheduledExecutorService = get("scheduledExecutorService", ScheduledExecutorService.class);
        assertThat("scheduledExecutorService should not be null", scheduledExecutorService, notNullValue());
        assertThat("scheduledExecutorService should be equal to registered scheduledExecutorService", scheduledExecutorService, is(registeredScheduledExecutorService));
    }

    @Test
    public void whenConfigureObjectMapperItShouldBeRegistered() {
        // when
        final ObjectMapper objectMapper = configureObjectMapper();

        // then
        final ObjectMapper registeredObjectMapper = get("objectMapper", ObjectMapper.class);
        assertThat("registered objectMapper should not be null", registeredObjectMapper, notNullValue());
        assertThat("registeredObjectMapper should be equal to objectMapper", registeredObjectMapper, is(objectMapper));
    }

    @Test
    public void whenConfigureMessageToStringConverterItShouldBeRegistered() {
        // given
        final ObjectMapper objectMapper = configureObjectMapper();

        // when
        final MessageToStringConverter messageToStringConverter = configureMessageToStringConverter(objectMapper);

        // then
        final MessageToStringConverter registeredMessageToStringConverter = get("messageToStringConverter", MessageToStringConverter.class);
        assertThat("registeredMessageToStringConverter should not be null", registeredMessageToStringConverter, notNullValue());
        assertThat("registeredMessageToStringConverter should be equal to messageToStringConverter", registeredMessageToStringConverter, is(messageToStringConverter));
    }

    @Test
    public void whenConfigureCamelContextItShouldBeRegistered() throws Exception {
        // when
        final CamelContext camelContext = configureCamelContext();

        // then
        final CamelContext registeredCamelContext = get("camelContext", CamelContext.class);
        assertThat("registeredCamelContext should has file component", registeredCamelContext.hasComponent("file"), notNullValue());
        assertThat("registeredCamelContext should has stream component", registeredCamelContext.hasComponent("stream"), notNullValue());
        assertThat("registeredCamelContext should not be null", registeredCamelContext, notNullValue());
        assertThat("registeredCamelContext should be equal to camelContext", registeredCamelContext, is(camelContext));
    }

    @Test
    public void whenConfigureSerializationDataFormatItShouldBeRegistered() {
        // given
        final ObjectMapper objectMapper = configureObjectMapper();

        // when
        final SerializationDataFormat serializationDataFormat = configureSerializationDataFormat(objectMapper);

        // then
        final SerializationDataFormat registeredSerializationDataFormat = get("serializationDataFormat", SerializationDataFormat.class);
        assertThat("registeredSerializationDataFormat should not be null", registeredSerializationDataFormat, notNullValue());
        assertThat("registeredSerializationDataFormat should be equal to messageToStringConverter", registeredSerializationDataFormat, is(serializationDataFormat));
    }
}