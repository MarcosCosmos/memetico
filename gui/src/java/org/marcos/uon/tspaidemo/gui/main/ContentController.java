package org.marcos.uon.tspaidemo.gui.main;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

import java.time.Duration;
import java.util.Optional;

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

    /**
     * Retrieves the root node associated with the controller (for the purposes of placing it into a new scene, etc)
     */
    Parent getRoot();

    /**
     * Updates the number of available frames (call this before frame-selection logic)
     */
    void frameCountUpdate();
    /**
     * Updates the displayed content to match the selected frame (call this after frame-selection logic)
     */
    void contentUpdate();

    ReadOnlyObjectProperty<Optional<Duration>> currentFrameDurationProperty();
}
