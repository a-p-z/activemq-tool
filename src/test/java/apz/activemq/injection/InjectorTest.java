package apz.activemq.injection;

import apz.activemq.injection.error.QualifierAlreadyExistError;
import apz.activemq.injection.error.QualifierNotFoundError;
import org.junit.Before;
import org.junit.Test;

import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.get;
import static apz.activemq.injection.Injector.register;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
}