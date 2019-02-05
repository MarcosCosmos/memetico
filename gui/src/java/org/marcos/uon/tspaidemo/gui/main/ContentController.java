package org.marcos.uon.tspaidemo.gui.main;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;

public interface ContentController extends Initializable {
    /**
     * Used by the playback controller
     * @return a property indicating the total number of frames (e.g. for a slider or progress bar)
     */
    ReadOnlyIntegerProperty numberOfFramesProperty();
    /**
     * Allows something else (i.e. the playback controller) to control which frame to show.
     */
    void bindSelectedFrameIndex(ObservableValue<Number> source);
}
