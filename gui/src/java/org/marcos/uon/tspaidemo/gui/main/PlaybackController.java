package org.marcos.uon.tspaidemo.gui.main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Note importantly that the logview is currently only updated by main visualisation controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class PlaybackController implements Initializable {
    private static final String PAUSE_TEXT = "❚❚";
    private static final String PLAY_TEXT = "▶";

    @FXML
    private Slider sldrFrameIndex;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnPlayPause;
    @FXML
    private Text txtCurFrame, txtMinFrame, txtMaxFrame;

    private boolean wasPlaying = false;

    /**
     * Values are measured as FPS
     */
    @FXML
    private ChoiceBox<Double> cbSpeed;

    private transient IntegerProperty frameCount = new SimpleIntegerProperty(0);
    private final transient BooleanProperty isPlaying = new SimpleBooleanProperty(true);
    private final transient ReadOnlyIntegerWrapper frameIndex = new ReadOnlyIntegerWrapper(0);
    private final transient EventHandler<ActionEvent> frameUpdater = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if(isPlaying.get()) {
                int curIndex = frameIndex.get();
                if (curIndex < frameCount.get() - 1) {
                    frameIndex.set(curIndex + 1);
                }
            }
        }
    };
    private transient Timeline playbackTimeline = new Timeline();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sldrFrameIndex.setMin(0);
        sldrFrameIndex.maxProperty().bind(Bindings.max(0, frameCount.subtract(1)));


        txtCurFrame.textProperty().bind(Bindings.createIntegerBinding(() -> (int)sldrFrameIndex.valueProperty().get(), sldrFrameIndex.valueProperty()).asString());
        txtMinFrame.textProperty().bind(Bindings.createIntegerBinding(() -> (int)sldrFrameIndex.minProperty().get(), sldrFrameIndex.minProperty()).asString());
        txtMaxFrame.textProperty().bind(Bindings.createIntegerBinding(() -> (int)sldrFrameIndex.maxProperty().get(), sldrFrameIndex.maxProperty()).asString());

        sldrFrameIndex.valueProperty().bindBidirectional(frameIndex);

        frameIndex.addListener((a,b,c) -> {
            int i=0;
        });

        sldrFrameIndex.setOnMousePressed((event) -> {
            wasPlaying = isPlaying.get();
            isPlaying.set(false);
        });

        sldrFrameIndex.setOnMouseReleased((event) -> {
            if(wasPlaying) {
                isPlaying.set(true);
            }
        });

        btnPlayPause.textProperty()
                .bind(
                        Bindings.createStringBinding(
                                () -> isPlaying.get() ? PAUSE_TEXT : PLAY_TEXT,
                                isPlaying
                        )
                );
        cbSpeed.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    playbackTimeline.stop();
                    ObservableList<KeyFrame> frames = playbackTimeline.getKeyFrames();
                    frames.clear();
                    frames.add(new KeyFrame(Duration.millis(100/ newValue), frameUpdater));
                    playbackTimeline.play();
                }
        );
        playbackTimeline.setCycleCount(Animation.INDEFINITE);
        cbSpeed.setValue(1.0);
    }

    public ReadOnlyIntegerProperty frameIndexProperty() {
        return frameIndex.getReadOnlyProperty();
    }

    public void stopPlayback() {
        isPlaying.set(false);
        frameIndex.set(0);
    }

    public void togglePlayState() {
        isPlaying.set(!isPlaying.get());
    }

    /**
     * Allows something else (i.e. the playback controller) to control which frame to show.
     */
    public void bindframeCount(ObservableValue<Number> source) {
        frameCount.bind(source);
    }
}
