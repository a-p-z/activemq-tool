package apz.activemq;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;

import static apz.activemq.injection.Injector.register;
import static java.util.Objects.requireNonNull;

public class Configuration {

    public static HostServicesDelegate configureHostServices(final Application application) {

        requireNonNull(application, "application must be not null");

        final HostServicesDelegate hostServices = HostServicesFactory.getInstance(application);
        register("hostServices", hostServices);
        return hostServices;
    }
}
