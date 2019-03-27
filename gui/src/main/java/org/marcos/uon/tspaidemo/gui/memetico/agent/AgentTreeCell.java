package org.marcos.uon.tspaidemo.gui.memetico.agent;

import javafx.scene.control.TreeCell;
import memetico.logging.MemeticoSnapshot;

public class AgentTreeCell extends TreeCell<MemeticoSnapshot.AgentSnapshot> {
    private AgentDisplay internalGraphic = new AgentDisplay();
    @Override
    protected void updateItem(MemeticoSnapshot.AgentSnapshot item, boolean empty) {
        super.updateItem(item, empty);
        if(item == null) {
            setGraphic(null);
        } else {
            internalGraphic.setSnapShot(item);
            setGraphic(internalGraphic);
            setText(null);
        }
    }

    @Override
    public void requestFocus() {
        //do nothing
    }
}
