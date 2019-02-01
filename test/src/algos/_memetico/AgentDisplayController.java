package algos._memetico;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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

    @FXML
    private CheckBox bxTogglePocket, bxToggleCurrent;

    private IntegerProperty agentDisplayId;
    private ObjectProperty<PCAlgorithmState.AgentState> stateProperty;

    private Runnable togglePocket = ()->{}, toggleCurrent = () -> {};

    /**
     * Todo: maybe use events instead of passing in the notifySelection function
     * @param agentDisplayId
     * @param stateProperty
     */
    public void setup(IntegerProperty agentDisplayId, ObjectProperty<PCAlgorithmState.AgentState> stateProperty, BooleanProperty pocketSelection, BooleanProperty currentSelection) {
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
        if(pocketSelection != null) {
            pocketSelection.bind(bxTogglePocket.selectedProperty());
            bxTogglePocket.setVisible(true);
        } else {
            bxTogglePocket.setVisible(false);
        }
        if(currentSelection != null) {
            currentSelection.bind(bxToggleCurrent.selectedProperty());
            bxToggleCurrent.setVisible(true);
        } else {
            bxToggleCurrent.setVisible(false);
        }
    }

    public void setup(IntegerProperty agentDisplayId, ObjectProperty<PCAlgorithmState.AgentState> stateProperty) {
        setup(agentDisplayId, stateProperty, null, null);
    }
}
