package apz.activemq.controller;

import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

import static com.jfoenix.effects.JFXDepthManager.setDepth;
import static de.jensd.fx.glyphs.octicons.OctIcon.MARK_GITHUB;

public class InfoController implements Initializable {

    @FXML
    public VBox root;

    @FXML
    private HBox infoCard;

    @FXML
    public Label icon;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        setDepth(infoCard, 1);
        final OctIconView github = new OctIconView(MARK_GITHUB);
        github.setSize("18");
        icon.setGraphic(github);
    }
}
