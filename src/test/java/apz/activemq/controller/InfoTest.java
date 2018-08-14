package apz.activemq.controller;

import com.sun.javafx.application.HostServicesDelegate;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class InfoTest extends ApplicationTest {

    @Mock
    private HostServicesDelegate hostServices;

    @Override
    public void start(final Stage stage) {

        clearRegistry();
        register("hostServices", hostServices);

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);
        final InfoController infoController = newInstance(InfoController.class);

        stackPane.getChildren().add(infoController.root);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void infoDeveloperAndRepositoryInfoShouldBePresent() {
        // then
        final Label developer = lookup("#developer").query();
        final Hyperlink repository = lookup("#repository").query();
        then(hostServices).shouldHaveZeroInteractions();
        assertThat("developer should be present", developer, notNullValue());
        assertThat("repository should be present", repository, notNullValue());
    }

    @Test
    public void whenClickOnRepositoryHostServicesShowDocumentShouldBeCalled() {
        // when
        clickOn("#repository");

        // then
        then(hostServices).should().showDocument("https://github.com/a-p-z/activemq-tool");
        then(hostServices).shouldHaveNoMoreInteractions();
    }
}