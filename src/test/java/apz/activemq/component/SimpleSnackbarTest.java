package apz.activemq.component;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static apz.activemq.utils.AssertUtils.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class SimpleSnackbarTest extends ApplicationTest {

    final private StackPane container = new StackPane();

    private SimpleSnackbar simpleSnackBar;

    @Override
    public void start(final Stage stage) {

        final Scene scene = new Scene(container, 800, 580);

        stage.setTitle(getClass().getSimpleName());
        stage.setScene(scene);
        stage.show();

        simpleSnackBar = new SimpleSnackbar(container);
    }

    @Test
    public void info() {
        // when
        simpleSnackBar.info("info");

        // then
        assertThat("info-toast should be visible", lookup(".jfx-snackbar-content:info-toast")::queryAll, hasSize(1));
    }

    @Test
    public void warn() {
        // when
        simpleSnackBar.warn("warn");

        // then
        assertThat("warn-toast should be visible", lookup(".jfx-snackbar-content:warn-toast")::queryAll, hasSize(1));
    }

    @Test
    public void error() {
        // when
        simpleSnackBar.error("error");

        // then
        assertThat("error-toast should be visible", lookup(".jfx-snackbar-content:error-toast")::queryAll, hasSize(1));
    }
}