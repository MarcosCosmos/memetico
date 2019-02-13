package org.marcos.uon.tspaidemo.gui.memetico;

import javafx.scene.control.TreeCell;
import memetico.logging.PCAlgorithmState;

public class AgentTreeCell extends TreeCell<PCAlgorithmState.AgentState> {
    private AgentDisplay internalGraphic = new AgentDisplay();
    @Override
    protected void updateItem(PCAlgorithmState.AgentState item, boolean empty) {
        super.updateItem(item, empty);
        if(item == null) {
            setGraphic(null);
        } else {
            internalGraphic.setState(item);
            setGraphic(internalGraphic);
            setText(null);
        }
    }

    @Override
    public void requestFocus() {
        //do nothing
    }
}
