package algos._memetico;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import memetico.Memetico;
import memetico.logging.PCLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

public class TestVisualisationController implements Initializable {
    @FXML private BorderPane root;
    @FXML private Pane frameContent;
    @FXML private Pane playBackControls;

    private PCLogger logger;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //set the frame content controller to a new
        try {
            PCLogger logger = new PCLogger();
            Thread theThread = new Thread(() -> Memetico.main(logger, new String[0]));
            theThread.start();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "test_pc_frame_content.fxml"
                    )
            );
            Pane content = loader.load();
            root.setTop(content);
            loader.<TestPCFrameController>getController().setView(logger.newView());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
