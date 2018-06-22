package apz.activemq;

import apz.activemq.injection.Injector;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import org.junit.Before;
import org.junit.Test;

import static apz.activemq.Configuration.configureHostServices;
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
}