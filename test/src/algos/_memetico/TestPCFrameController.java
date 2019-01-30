package algos._memetico;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
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
    @FXML private BorderPane contentRoot;

    private IntegerProperty generationValue = new SimpleIntegerProperty();
    @FXML private Text generationText;

    private BasicLogger<PCAlgorithmState>.View logView;
    private IntegerProperty frameIndex;
    public void setup(BasicLogger<PCAlgorithmState>.View logView, IntegerProperty frameIndex) {
        this.logView = logView;
        this.frameIndex = frameIndex;
        generationValue.bind(
                Bindings.createIntegerBinding(
                    () -> {
                        logView.update();
                        return logView.isEmpty() ? 0 : logView.get(frameIndex.get()).generation;
                    },
                    frameIndex
                )
        );
        generationText.textProperty()
                .bind(generationValue.asString());

    }
}
