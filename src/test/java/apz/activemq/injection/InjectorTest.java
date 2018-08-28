package apz.activemq.injection;

import apz.activemq.injection.error.QualifierAlreadyExistError;
import apz.activemq.injection.error.QualifierNotFoundError;
import apz.activemq.jmx.JmxClient;
import apz.activemq.model.Message;
import apz.activemq.model.Queue;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.get;
import static apz.activemq.injection.Injector.register;
import static apz.activemq.injection.Injector.resolveDependencies;
import static apz.activemq.injection.Injector.shutdownExecutorServices;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

public class InjectorTest {

    @Before
    public void before() {
        clearRegistry();
    }

    @Test
    public void whenRegisterAnObjectItShouldBeRegistered() {

        final Object object = new Object();

        register("qualifier", object);

        assertThat("singleton should be registered", get("qualifier", Object.class), is(object));
    }

    @Test(expected = QualifierAlreadyExistError.class)
    public void whenRegisterAnObjectTwiceQualifierAlreadyExistErrorIsExpected() {

        final Object object = new Object();

        register("qualifier", object);
        register("qualifier", object);
    }

    @Test(expected = QualifierNotFoundError.class)
    public void whenGetAnUnregisteredQualifierAQualifierNotFoundErrorIsExpected() {

        final Object object = new Object();

        register("qualifier", object);

        get("this-qualifier-not-exist", Object.class);
    }

    @Test(expected = QualifierNotFoundError.class)
    public void whenGetAQualifierAfterCleanRegistryAQualifierNotFoundErrorIsExpected() {
        final Object object = new Object();

        register("qualifier", object);
        clearRegistry();

        get("qualifier", Object.class);
    }

    @Test
    public void whenResolveDependenciesAllAnnotatedFieldShouldBeSet() {

        final Queue queue = mock(Queue.class);
        final Message message = mock(Message.class);
        register("jmxClient", mock(JmxClient.class));

        final A a = new A();
        resolveDependencies(a, queue, message);

        assertThat(a.getJmxClient(), is(get("jmxClient", JmxClient.class)));
        assertThat(a.getQueue(), is(queue));
        assertThat(a.getMessage(), is(message));
    }

    @Test
    public void whenShutdownExecutorServicesAllRegisteredExecutorServicesShouldBeTerminated() throws InterruptedException {

        final ExecutorService esecutorService1 = mock(ExecutorService.class);
        final ExecutorService esecutorService2 = mock(ExecutorService.class);
        given(esecutorService2.awaitTermination(30, SECONDS)).willThrow(new InterruptedException());
        register("esecutorService1", esecutorService1);
        register("esecutorService2", esecutorService2);

        shutdownExecutorServices();

        then(esecutorService1).should().shutdown();
        then(esecutorService1).should().awaitTermination(30, SECONDS);
        then(esecutorService2).should().shutdown();
        then(esecutorService2).should().awaitTermination(30, SECONDS);

        then(esecutorService1).shouldHaveNoMoreInteractions();
        then(esecutorService2).shouldHaveNoMoreInteractions();
    }

    private static class A {

        @Inject
        private JmxClient jmxClient;

        @Inject
        private Queue queue;

        @Inject
        private Message message;

        public JmxClient getJmxClient() {
            return jmxClient;
        }

        public Queue getQueue() {
            return queue;
        }

        public Message getMessage() {
            return message;
        }
    }
}