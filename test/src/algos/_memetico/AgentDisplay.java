package algos._memetico;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import memetico.logging.PCAlgorithmState;

import java.util.stream.Stream;


public class AgentDisplay extends GridPane {
    private final transient Label lblAgentId = new Label("Agent ");
    private final transient Text txtAgentId = new Text();
    private final transient Label lblPocketCost = new Label("Pocket Cost:");
    private final transient Text txtPocketCost = new Text();
    private final transient Label lblCurrentCost = new Label("Current Cost:");
    private final transient Text txtCurrentCost = new Text();
    private final transient CheckBox cbTogglePocket = new CheckBox(), cbToggleCurrent = new CheckBox();


    private IntegerProperty agentDisplayId;
    private ObjectProperty<PCAlgorithmState.AgentState> stateProperty;

    private Runnable togglePocket = ()->{}, toggleCurrent = () -> {};

    public AgentDisplay(IntegerProperty agentDisplayId, ObjectProperty<PCAlgorithmState.AgentState> stateProperty, BooleanProperty pocketSelection, BooleanProperty currentSelection) {
        getStyleClass().add("agentInfo");
        lblAgentId.getStyleClass().add("lblAgentId");
        txtAgentId.getStyleClass().add("txtAgentId");
        txtPocketCost.getStyleClass().add("txtPocketCost");
        txtCurrentCost.getStyleClass().add("txtCurrentCost");

        Stream.of(lblAgentId, txtAgentId, lblPocketCost, txtPocketCost, lblCurrentCost, txtCurrentCost, cbTogglePocket, cbToggleCurrent).forEach(getChildren()::add);
        Stream.of(lblAgentId, lblPocketCost, lblCurrentCost).forEach(each -> GridPane.setColumnIndex(each, 0));
        Stream.of(txtAgentId, txtPocketCost, txtCurrentCost).forEach(each -> GridPane.setColumnIndex(each, 1));
        Stream.of(cbTogglePocket, cbToggleCurrent).forEach(each -> GridPane.setColumnIndex(each, 2));
        Stream.of(lblAgentId, txtAgentId).forEach(each -> GridPane.setRowIndex(each, 0));
        Stream.of(lblPocketCost, txtPocketCost, cbTogglePocket).forEach(each -> GridPane.setRowIndex(each, 1));
        Stream.of(lblCurrentCost, txtCurrentCost, cbToggleCurrent).forEach(each -> GridPane.setRowIndex(each, 2));

        this.agentDisplayId = agentDisplayId;
        this.stateProperty = stateProperty;
        txtAgentId.textProperty().bind(agentDisplayId.asString());
        txtPocketCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(stateProperty.get().pocket.cost),
                stateProperty
        ));
        txtCurrentCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(stateProperty.get().current.cost),
                stateProperty
        ));
        if(pocketSelection != null) {
            pocketSelection.bind(cbTogglePocket.selectedProperty());
            cbTogglePocket.setVisible(true);
        } else {
            cbTogglePocket.setVisible(false);
        }
        if(currentSelection != null) {
            currentSelection.bind(cbToggleCurrent.selectedProperty());
            cbToggleCurrent.setVisible(true);
        } else {
            cbToggleCurrent.setVisible(false);
        }
    }

    public AgentDisplay(IntegerProperty agentDisplayId, ObjectProperty<PCAlgorithmState.AgentState> stateProperty) {
        this(agentDisplayId, stateProperty, null, null);
    }
}
