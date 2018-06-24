package apz.activemq;

import apz.activemq.injection.Injector;
import apz.activemq.jmx.JmxClient;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;

import static apz.activemq.Configuration.configureHostServices;
import static apz.activemq.Configuration.configureJmxClient;
import static apz.activemq.Configuration.configureScheduledExecutorService;
import static apz.activemq.injection.Injector.clearRegistry;
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
        final HostServicesDelegate registeredHostServices = Injector.get("hostServices", HostServicesDelegate.class);
        assertThat("hostService should not be null", hostServices, notNullValue());
        assertThat("hostService should be equal to registered hostServices", hostServices, is(registeredHostServices));
    }

    @Test
    public void whenConfigureJmxClientItShouldBeRegistered() {
        // when
        final JmxClient jmxClient = configureJmxClient();

        // then
        final JmxClient registeredJmxClient = Injector.get("jmxClient", JmxClient.class);
        assertThat("jmxClient should not be null", jmxClient, notNullValue());
        assertThat("jmxClient should be equal to registered jmxClient", jmxClient, is(registeredJmxClient));
    }

    @Test
    public void whenConfigureScheduledExecutorServiceItShouldBeRegistered() {
        // when
        final ScheduledExecutorService scheduledExecutorService = configureScheduledExecutorService();

        // then
        final ScheduledExecutorService registeredScheduledExecutorService = Injector.get("scheduledExecutorService", ScheduledExecutorService.class);
        assertThat("scheduledExecutorService should not be null", scheduledExecutorService, notNullValue());
        assertThat("scheduledExecutorService should be equal to registered scheduledExecutorService", scheduledExecutorService, is(registeredScheduledExecutorService));
    }
}