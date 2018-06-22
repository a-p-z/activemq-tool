package apz.activemq.controller;

import com.sun.javafx.application.HostServicesDelegate;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static apz.activemq.injection.Injector.clearRegistry;
import static apz.activemq.injection.Injector.register;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class NavigationTest extends ApplicationTest {

    @Mock
    private HostServicesDelegate hostServices;

    @Override
    public void start(final Stage stage) {

        clearRegistry();
        register("hostServices", hostServices);

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);
        final NavigationController navigationController = newInstance(NavigationController.class);

        stackPane.getChildren().add(navigationController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void whenClickOnInfoTitleShouldBeInfo() {
        // when
        clickOn("#info");

        // then
        final Label title = lookup("#title").query();

        verifyZeroInteractions(hostServices);

        assertThat("title should be INFO", title.getText(), is("INFO"));
    }
}