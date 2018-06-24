package apz.activemq;

import apz.activemq.jmx.JmxClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static apz.activemq.injection.Injector.register;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class Configuration {

    public static HostServicesDelegate configureHostServices(final Application application) {

        requireNonNull(application, "application must be not null");

        final HostServicesDelegate hostServices = HostServicesFactory.getInstance(application);
        register("hostServices", hostServices);
        return hostServices;
    }

    public static JmxClient configureJmxClient() {

        final JmxClient jmxClient = new JmxClient();
        register("jmxClient", jmxClient);
        return jmxClient;
    }

    public static ScheduledExecutorService configureScheduledExecutorService() {

        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("activemq-tool-thread-%d").build();
        final ScheduledExecutorService scheduledExecutorService = newScheduledThreadPool(10, threadFactory);
        register("scheduledExecutorService", scheduledExecutorService);
        return scheduledExecutorService;
    }
}
