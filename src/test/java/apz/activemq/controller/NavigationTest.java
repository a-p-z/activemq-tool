package apz.activemq.controller;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NavigationTest extends ApplicationTest {

    @Override
    public void start(final Stage stage) throws IOException {

        final StackPane stackPane = new StackPane();
        final Scene scene = new Scene(stackPane, 800, 580);
        final NavigationController navigationController = ControllerFactory.newInstance(NavigationController.class);

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

        assertThat("title should be INFO", title.getText(), is("INFO"));
    }
}