package apz.activemq.controller;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import static apz.activemq.controller.ControllerFactory.newInstance;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InfoTest extends ApplicationTest {

    @Override
    public void start(final Stage stage) {

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
        final Label repository = lookup("#repository").query();
        assertThat("developer should be present", developer, notNullValue());
        assertThat("repository should be present", repository, notNullValue());
    }
}