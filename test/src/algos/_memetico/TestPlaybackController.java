package algos._memetico;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
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
import memetico.logging.PCAlgorithmState;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Note importantly that the logview is currently only updated by main visualisation controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class TestPlaybackController {
    private static final String PAUSE_TEXT = "❚❚";
    private static final String PLAY_TEXT = "▶";

    @FXML
    private Slider sldrFrameIndex;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnPlayPause;

    /**
     * Values are measured as FPS
     */
    @FXML
    private ChoiceBox<Double> speedChoice;

    private transient IntegerProperty numberOfFrames;
    private final transient BooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private final transient IntegerProperty frameIndex = new SimpleIntegerProperty(0);
    private final transient EventHandler<ActionEvent> frameUpdater = new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if(isPlaying.get()) {
                int curIndex = frameIndex.get();
                if (curIndex < numberOfFrames.get() - 1) {
                    frameIndex.set(curIndex + 1);
                }
            }
        }
    };
    private transient Timeline playbackTimeline = new Timeline();

    public void setup(IntegerProperty numberOfFramesProp) {
        this.numberOfFrames = numberOfFramesProp;
        sldrFrameIndex.majorTickUnitProperty().bind(Bindings.max(numberOfFrames, 1));
        sldrFrameIndex.setShowTickLabels(true);
        sldrFrameIndex.setMin(0);
        sldrFrameIndex.maxProperty().bind(numberOfFrames);
        sldrFrameIndex.valueProperty().bindBidirectional(frameIndex);
        btnPlayPause.textProperty()
                .bind(
                        Bindings.createStringBinding(
                                () -> isPlaying.get() ? PAUSE_TEXT : PLAY_TEXT,
                                isPlaying
                        )
                );
        speedChoice.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    ObservableList<KeyFrame> frames = playbackTimeline.getKeyFrames();
                    frames.add(new KeyFrame(Duration.millis(100/ newValue), frameUpdater));
                }
        );
        speedChoice.setValue(1.0);
//        playbackTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(100/speedChoice.getValue().doubleValue()), frameUpdater));
        playbackTimeline.setCycleCount(Animation.INDEFINITE);
        playbackTimeline.play();
    }

    public IntegerProperty frameIndexProperty() {
        return frameIndex;
    }

    public void stopPlayback() {
        isPlaying.set(false);
        frameIndex.set(0);
    }

    public void togglePlayState() {
        isPlaying.set(!isPlaying.get());
    }
}
