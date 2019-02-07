package org.marcos.uon.tspaidemo.gui.memetico;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import memetico.logging.PCAlgorithmState;

import java.io.IOException;


public class AgentDisplay extends GridPane {
//    private final transient Label lblAgentId;
//    private final transient Label lblPocketCost;
//private final transient Label lblCurrentCost;
    private final transient Text txtAgentId;
    private final transient Text txtPocketCost;
    private final transient Text txtCurrentCost;
//    private final transient CheckBox cbTogglePocket, cbToggleCurrent;


    private final IntegerProperty agentDisplayId = new SimpleIntegerProperty(0);
    private final ObjectProperty<PCAlgorithmState.AgentState> state = new SimpleObjectProperty<>();


    public AgentDisplay(IntegerProperty agentDisplayId, ObjectProperty<PCAlgorithmState.AgentState> stateProperty/*, BooleanProperty pocketSelection, BooleanProperty currentSelection*/) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "agent_display.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

//        lblAgentId = (Label) lookup(".lblAgentId");
//        lblPocketCost = (Label) lookup(".lblPocketCost");
//        lblCurrentCost = (Label) lookup(".lblCurrentCost");
        txtAgentId = (Text) lookup(".txtAgentId");
        txtPocketCost = (Text) lookup(".txtPocketCost");
        txtCurrentCost = (Text) lookup(".txtCurrentCost");
//        cbTogglePocket = (CheckBox) lookup(".cbTogglePocket");
//        cbToggleCurrent = (CheckBox) lookup(".cbToggleCurrent");


        this.agentDisplayId.bind(agentDisplayId);
        this.state.bind(stateProperty);
        txtAgentId.textProperty().bind(this.agentDisplayId.asString());
        txtPocketCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(state.get() == null ? "Uknown" : state.get().pocket.cost),
                state
        ));
        txtCurrentCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(state.get() == null ? "Uknown" : state.get().current.cost),
                state
        ));
//        if(pocketSelection != null) {
//            cbTogglePocket.setSelected(pocketSelection.get());
//            pocketSelection.bind(cbTogglePocket.selectedProperty());
//            cbTogglePocket.setVisible(true);
//        } else {
//            cbTogglePocket.setVisible(false);
//        }
//        if(currentSelection != null) {
//            cbToggleCurrent.setSelected(currentSelection.get());
//            currentSelection.bind(cbToggleCurrent.selectedProperty());
//            cbToggleCurrent.setVisible(true);
//        } else {
//            cbToggleCurrent.setVisible(false);
//        }
    }

//    public AgentDisplay(IntegerProperty agentDisplayId, ObjectProperty<PCAlgorithmState.AgentState> stateProperty) throws IOException {
//        this(agentDisplayId, stateProperty, null, null);
//    }
}
