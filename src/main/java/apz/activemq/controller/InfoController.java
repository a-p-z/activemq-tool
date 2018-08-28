package apz.activemq.controller;

import apz.activemq.injection.Inject;
import com.sun.javafx.application.HostServicesDelegate;
import de.jensd.fx.glyphs.octicons.OctIconView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.Optional;
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

    @FXML
    private Hyperlink repository;

    @Inject
    private HostServicesDelegate hostServices;

    @Override
    public void initialize(final @Nullable URL location, final @Nullable ResourceBundle resources) {

        setDepth(infoCard, 1);
        final OctIconView github = new OctIconView(MARK_GITHUB);
        github.setSize("18");
        icon.setGraphic(github);
    }

    @FXML
    public void showRepository(final @Nullable ActionEvent event) {

        Optional.ofNullable(event).ifPresent(ActionEvent::consume);

        hostServices.showDocument(repository.getText());
    }
}
