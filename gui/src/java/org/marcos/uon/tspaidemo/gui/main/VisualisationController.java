package org.marcos.uon.tspaidemo.gui.main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Note importantly that the logview is currently only updated by this main controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class VisualisationController implements Initializable {
    @FXML
    private VBox root;

    private ContentController contentController;

    public static final ContentController PLACEHOLDER_CONTENT = new ContentController() {
        ReadOnlyIntegerProperty numberOfFrames = new ReadOnlyIntegerWrapper(0);
        ReadOnlyObjectProperty<Optional<java.time.Duration>> frameDuration = new ReadOnlyObjectWrapper<>(Optional.empty());
        Parent emptyBox = new Pane();
        @Override
        public ReadOnlyIntegerProperty numberOfFramesProperty() {
            return numberOfFrames;
        }

        @Override
        public void bindSelectedFrameIndex(ObservableValue<Number> source) {
        }

        @Override
        public Parent getRoot() {
            return emptyBox;
        }

        @Override
        public void frameCountUpdate() {

        }

        @Override
        public void contentUpdate() {

        }

        @Override
        public ReadOnlyObjectProperty<Optional<java.time.Duration>> currentFrameDurationProperty() {
            return frameDuration;
        }


        @Override
        public void initialize(URL location, ResourceBundle resources) {
        }
    };

    private PlaybackController playbackController;

    private final ObjectProperty<Duration> frameInterval = new SimpleObjectProperty<>(Duration.millis(1000/60.0));
    private final transient Timeline redrawTimeline = new Timeline();

    /**
     * Assign the content to display and link the frame count and interval between the content controller and the playback controller
     * @param contentController
     *
     */
    public void setup(ContentController contentController) {
        if(this.contentController != null) {
            root.getChildren().remove(this.contentController.getRoot());
        }
        this.contentController = contentController;
        Parent contentRoot = contentController.getRoot();
        root.getChildren().add(0, contentRoot);
        VBox.setVgrow(contentRoot, Priority.ALWAYS);
        contentController.bindSelectedFrameIndex(playbackController.frameIndexProperty());
        playbackController.bindFrameCount(contentController.numberOfFramesProperty());
        playbackController.bindCurrentFrameDuration(contentController.currentFrameDurationProperty());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        root.getStylesheets().add(getClass().getResource("visualisation.css").toExternalForm());
        root.getStylesheets().add(getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/common.css").toExternalForm());
        try {
            //setup the playback controls, giving them an observable reference to the total frame count.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/playback_controls.fxml"));
            Pane playbackControls = loader.load();
            root.getChildren().add(playbackControls);
            playbackController = loader.getController();

            //initially set it up to the placeholder
            setup(PLACEHOLDER_CONTENT);

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
            //make sure the timeline is playing now
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
