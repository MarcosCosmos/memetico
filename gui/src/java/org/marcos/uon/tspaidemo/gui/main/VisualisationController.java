package org.marcos.uon.tspaidemo.gui.main;

import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Note importantly that the logview is currently only updated by this main controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class VisualisationController implements Initializable {
    @FXML
    private VBox root;
    @FXML
    private Pane frameContent;
    @FXML
    private Pane playBackControls;
    private transient IntegerProperty selectedFrameIndex;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        root.getStylesheets().add(getClass().getResource("visualisation.css").toExternalForm());
        //todo: perhaps make the type of the logger and frame content to use generic/parameters given to this controller.?
        //set the frame content controller to a new
        try {
            //set up the content display with an observable reference to the current state to display.
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "../memetico/content.fxml"
                    )
            );
            Pane content = loader.load();
            root.getChildren().add(0, content);
            VBox.setVgrow(content, Priority.ALWAYS);
            ContentController theController = loader.getController();

            //setup the playback controls, giving them an observable reference to the total frame count.
            loader = new FXMLLoader(getClass().getResource("playback_controls.fxml"));
            Pane playbackControls = loader.load();
            root.getChildren().add(playbackControls);
            PlaybackController playbackController = loader.getController();

            theController.bindSelectedFrameIndex(playbackController.frameIndexProperty());
            playbackController.bindframeCount(theController.numberOfFramesProperty());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
