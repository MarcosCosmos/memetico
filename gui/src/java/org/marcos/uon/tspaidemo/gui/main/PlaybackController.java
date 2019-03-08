package org.marcos.uon.tspaidemo.gui.main;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * Note importantly that the logview is currently only updated by main visualisation controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class PlaybackController implements Initializable {
    private static final String PAUSE_TEXT = "❚❚";
    private static final String PLAY_TEXT = "▶";

    @FXML
    private HBox playbackControlsRoot;

    @FXML
    private Slider sldrFrameIndex;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnPlayPause;
    @FXML
    //txtMinFrame,
    private Text txtCurFrame, txtMaxFrame, txtTest;

    private boolean wasPlaying = false;

    private long lastUpdateTime;
    private long totalTime = 0;

//    private final DoubleProperty speedCoefficient = new SimpleDoubleProperty(1.0);
    private final ObjectProperty<Optional<java.time.Duration>> currentFrameDuration = new SimpleObjectProperty<>(null);
    private java.time.Duration leftOverNano = java.time.Duration.ZERO;

    /**
     * Values are used a multiplier against the CPU time between generations etc
     */
    @FXML
    private ChoiceBox<Double> cbSpeed;

    private transient IntegerProperty frameCount = new SimpleIntegerProperty(0);
    private final transient BooleanProperty isPlaying = new SimpleBooleanProperty(true);
    private final transient ReadOnlyIntegerWrapper frameIndex = new ReadOnlyIntegerWrapper(0);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        playbackControlsRoot.getStylesheets().addAll(
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/common.css").toExternalForm(),
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/playback_controls.css").toExternalForm()
        );
        sldrFrameIndex.setMin(0);
        sldrFrameIndex.maxProperty().bind(Bindings.max(0, frameCount.subtract(1)));


        txtCurFrame.textProperty().bind(Bindings.createIntegerBinding(() -> (int)sldrFrameIndex.valueProperty().get(), sldrFrameIndex.valueProperty()).asString());
//        txtMinFrame.textProperty().bind(Bindings.createIntegerBinding(() -> (int)sldrFrameIndex.minProperty().get(), sldrFrameIndex.minProperty()).asString());
        txtMaxFrame.textProperty().bind(Bindings.createIntegerBinding(() -> (int)sldrFrameIndex.maxProperty().get(), sldrFrameIndex.maxProperty()).asString());

        sldrFrameIndex.valueProperty().bindBidirectional(frameIndex);


        sldrFrameIndex.setOnMousePressed((event) -> {
            wasPlaying = isPlaying.get();
            isPlaying.set(false);
        });

        sldrFrameIndex.setOnMouseReleased((event) -> {
            if(wasPlaying) {
                isPlaying.set(true);
                leftOverNano = java.time.Duration.ZERO;
            }
        });

        btnPlayPause.textProperty()
                .bind(
                        Bindings.createStringBinding(
                                () -> isPlaying.get() ? PAUSE_TEXT : PLAY_TEXT,
                                isPlaying
                        )
                );

        isPlaying.addListener((obvs, old, newVal) -> {
            if(!old && newVal) {
                lastUpdateTime = System.nanoTime();
            }
        });


        cbSpeed.setValue(1.0);

        lastUpdateTime = System.nanoTime();
    }

    public ReadOnlyIntegerProperty frameIndexProperty() {
        return frameIndex.getReadOnlyProperty();
    }

    public void stopPlayback() {
        leftOverNano = java.time.Duration.ZERO;
        isPlaying.set(false);
        frameIndex.set(0);
        totalTime = 0;
    }

    public void togglePlayState() {
        isPlaying.set(!isPlaying.get());
    }

    /**
     * Allows something else (i.e. the playback controller) to control which frame to show.
     */
    public void bindFrameCount(ObservableValue<Number> source) {
        frameCount.bind(source);
    }

    public void unbindFrameCount() {
        frameCount.unbind();
    }

    public void bindCurrentFrameDuration(ObservableValue<Optional<java.time.Duration>> source) {
        currentFrameDuration.bind(source);
    }

    public void unbindCurrentFrameDuration() {
        currentFrameDuration.unbind();
    }

    public void frameUpdate() {
        if(isPlaying.get()) {
            long currentUpdateTime = System.nanoTime();
            int curIndex = frameIndex.get();

            Optional<java.time.Duration> frameDuration = currentFrameDuration.get();
            if(frameDuration.isPresent()) {
                java.time.Duration elapsed;
                elapsed = java.time.Duration.ofNanos(currentUpdateTime-lastUpdateTime);
                double multiplier = cbSpeed.getValue();
                if(multiplier > 1) {
                    elapsed = elapsed.multipliedBy((long)multiplier);
                } else {
                    elapsed = elapsed.dividedBy((long)(1/multiplier));
                }
                elapsed = elapsed.plus(leftOverNano);
                while (frameDuration.isPresent() && frameDuration.get().compareTo(elapsed) < 0) {
                    elapsed = elapsed.minus(frameDuration.get());
                    curIndex = Math.min(curIndex + 1, frameCount.get() - 1);
                    System.out.println(frameDuration.get().toString());
                    frameIndex.set(curIndex);
                    frameDuration = currentFrameDuration.get();
                }

                totalTime += currentUpdateTime-lastUpdateTime;
                System.out.println(java.time.Duration.ofNanos(totalTime).toString());
                leftOverNano = elapsed;
                lastUpdateTime = currentUpdateTime;

            }
        }
    }

    public Parent getRoot() {
        return playbackControlsRoot;
    }
}
