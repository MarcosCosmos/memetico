package algos._memetico;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import memetico.Memetico;
import memetico.logging.PCLogger;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TestVisualisationController implements Initializable {
    @FXML private BorderPane root;
    @FXML private Pane frameContent;
    @FXML private Pane playBackControls;

    private PCLogger logger;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set the frame content controller to a new
        try {
            logger = new PCLogger();
            Thread theThread = new Thread(() -> Memetico.main(logger, new String[0]));
            theThread.start();

//            BasicLogger<PCLogger>.View view = logger.newView();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("test_playback_controls.fxml"));
            Pane playbackControls = loader.load();
            root.setBottom(playbackControls);
            TestPlaybackController playbackController = loader.<TestPlaybackController>getController();
            playbackController.setup(logger.newView());

            loader = new FXMLLoader(
                    getClass().getResource(
                            "test_pc_frame_content.fxml"
                    )
            );
            Pane content = loader.load();
            root.setTop(content);
            loader.<TestPCFrameController>getController().setup(logger.newView(), playbackController.frameIndexProperty());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
