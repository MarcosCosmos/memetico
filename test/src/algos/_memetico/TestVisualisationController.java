package algos._memetico;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import memetico.Memetico;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Note importantly that the logview is currently only updated by this main controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class TestVisualisationController implements Initializable {

    //todo: possibly make this a parameter instead
    /**
     * Measured as checks per second
     */
    public static double LOG_POLL_RATE = 60;

    @FXML
    private VBox root;
    @FXML
    private Pane frameContent;
    @FXML
    private Pane playBackControls;

    private transient PCLogger logger;
    private transient BasicLogger<PCAlgorithmState>.View view;
    private transient IntegerProperty numberOfFrames = new SimpleIntegerProperty(0);
    private transient IntegerProperty selectedFrameIndex;
    private transient ObjectProperty<PCAlgorithmState> currentState;
    private final transient Timeline updateCheckingTimeline = new Timeline();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //todo: perhaps make the type of the logger and frame content to use generic/parameters given to this controller.?
        //set the frame content controller to a new
        try {
            logger = new PCLogger(10);

//            PrintStream originalStdout = System.out;
//            System.setOut(new PrintStream(new OutputStream() {
//                public void write(int b) {
//                    //DO NOTHING
//                }
//            }));

            Thread theThread = new Thread(() -> Memetico.main(logger, new String[0]));
            theThread.start();

            view = logger.newView();

            currentState = new SimpleObjectProperty<>();
            //setup the playback controls, giving them an observable reference to the total frame count.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("test_playback_controls.fxml"));
            Pane playbackControls = loader.load();
            root.getChildren().add(playbackControls);
            TestPlaybackController playbackController = loader.<TestPlaybackController>getController();
            playbackController.setup(numberOfFrames);

            //bind the current state to the selected index given by the playback controller.
            selectedFrameIndex = playbackController.frameIndexProperty();
            currentState.bind(
                    Bindings.createObjectBinding(
                            () -> view.isEmpty() ? null : view.get(selectedFrameIndex.get()),
                            selectedFrameIndex
                    )
            );


            //set up the content display with an observable reference to the current state to display.
            loader = new FXMLLoader(
                    getClass().getResource(
                            "test_pc_frame_content.fxml"
                    )
            );
            Pane content = loader.load();
            root.getChildren().add(0, content);
            VBox.setVgrow(content, Priority.ALWAYS);
            loader.<TestPCFrameController>getController().setup(currentState);

            //setup a timeline to poll for log updates, and update the number of frames accordingly
            updateCheckingTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(100/LOG_POLL_RATE), (event) -> {
                        try {
                            view.update();
                            numberOfFrames.set(view.size());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    })
            );
            updateCheckingTimeline.setCycleCount(Animation.INDEFINITE);
            updateCheckingTimeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
