package algos._memetico;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import memetico.logging.PCAlgorithmState;


public class AgentDisplayController {
    @FXML
    private GridPane agentRoot;
    @FXML
    private Text agentIdText;
    @FXML
    private Text pocketCostText;
    @FXML
    private Text currentCostText;

    private IntegerProperty agentDisplayId;
    private ObjectProperty<PCAlgorithmState.AgentState> stateProperty;

    public void setup(IntegerProperty agentDisplayId, ObjectProperty<PCAlgorithmState.AgentState> stateProperty) {
        this.agentDisplayId = agentDisplayId;
        this.stateProperty = stateProperty;
        agentIdText.textProperty().bind(agentDisplayId.asString());
        pocketCostText.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(stateProperty.get().pocket.cost),
                stateProperty
        ));
        currentCostText.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(stateProperty.get().current.cost),
                stateProperty
        ));
    }
}
