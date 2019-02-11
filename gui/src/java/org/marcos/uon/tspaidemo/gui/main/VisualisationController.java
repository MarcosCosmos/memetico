package org.marcos.uon.tspaidemo.gui.main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Note importantly that the logview is currently only updated by this main controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class VisualisationController implements Initializable {
    @FXML
    private VBox root;

    private ContentController contentController;

    private PlaybackController playbackController;
    private transient IntegerProperty selectedFrameIndex;

    private final ObjectProperty<Duration> frameInterval = new SimpleObjectProperty<>(Duration.millis(100/60.0));
    private final transient Timeline redrawTimeline = new Timeline();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        root.getStylesheets().add(getClass().getResource("visualisation.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("common.css").toExternalForm());
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
            contentController = loader.getController();

            //setup the playback controls, giving them an observable reference to the total frame count.
            loader = new FXMLLoader(getClass().getResource("playback_controls.fxml"));
            Pane playbackControls = loader.load();
            root.getChildren().add(playbackControls);
            playbackController = loader.getController();

            contentController.bindSelectedFrameIndex(playbackController.frameIndexProperty());
            playbackController.bindFrameCount(contentController.numberOfFramesProperty());

            //trigger subpanes to update at our target framerate
            //todo: possibly collect this into common base class or leave frameupdate to only be called externally by a containing controller?
            frameInterval.addListener(
                    (observable, oldValue, newValue) -> {
                        redrawTimeline.stop();
                        ObservableList<KeyFrame> frames = redrawTimeline.getKeyFrames();
                        frames.clear();
                        frames.add(new KeyFrame(frameInterval.get(), (e) -> frameUpdate()));
                        redrawTimeline.play();
                    }
            );

            //setup a timeline to poll for log updates, and update the number of frames accordingly

            redrawTimeline.getKeyFrames().add(new KeyFrame(frameInterval.get(), (e) -> frameUpdate()));
            redrawTimeline.setCycleCount(Animation.INDEFINITE);
            redrawTimeline.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void frameUpdate() {
        //note: the order is important as it ensures that the displayed frame number corresponds to the correct frame content
        contentController.frameCountUpdate();
        playbackController.frameUpdate();
        contentController.contentUpdate();
    }
}
