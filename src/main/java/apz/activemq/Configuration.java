package apz.activemq;

import apz.activemq.camel.SerializationDataFormat;
import apz.activemq.converter.MessageToStringConverter;
import apz.activemq.jmx.JmxClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import org.apache.camel.CamelContext;
import org.apache.camel.component.file.FileComponent;
import org.apache.camel.component.stream.StreamComponent;
import org.apache.camel.impl.DefaultCamelContext;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static apz.activemq.injection.Injector.register;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class Configuration {

    public static HostServicesDelegate configureHostServices(final @Nonnull Application application) {

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

    public static ObjectMapper configureObjectMapper() {

        final ObjectMapper objectMapper = new ObjectMapper();
        register("objectMapper", objectMapper);
        return objectMapper;
    }

    public static MessageToStringConverter configureMessageToStringConverter(final @Nonnull ObjectMapper objectMapper) {

        final MessageToStringConverter messageToStringConverter = new MessageToStringConverter(objectMapper);
        register("messageToStringConverter", messageToStringConverter);
        return messageToStringConverter;
    }

    public static CamelContext configureCamelContext() throws Exception {

        final DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setName("activemq-tool-camel");
        camelContext.addComponent("file", new FileComponent());
        camelContext.addComponent("stream", new StreamComponent());
        camelContext.start();
        register("camelContext", camelContext);
        return camelContext;
    }

    public static SerializationDataFormat configureSerializationDataFormat(final @Nonnull ObjectMapper objectMapper)  {

        final SerializationDataFormat serializationDataFormat = new SerializationDataFormat(objectMapper);

        register("serializationDataFormat", serializationDataFormat);
        return serializationDataFormat;
    }
}
