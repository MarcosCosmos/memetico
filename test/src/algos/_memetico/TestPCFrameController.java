package algos._memetico;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

import java.net.URL;
import java.util.ResourceBundle;

public class TestPCFrameController {
    private @FXML VBox frameContent;
    private @FXML Text simpleText;

    private BasicLogger<PCAlgorithmState>.View logView;

    private Timeline frameUpdater;
    public void setView(BasicLogger<PCAlgorithmState>.View logView) {
        this.logView = logView;
        frameUpdater = new Timeline(new KeyFrame(Duration.millis(16), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    logView.update();
                    simpleText.setText(logView.isEmpty() ? "Nada" : String.valueOf(logView.get(logView.size()-1).generation));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        frameUpdater.setCycleCount(Animation.INDEFINITE);
        frameUpdater.play();

    }
}
