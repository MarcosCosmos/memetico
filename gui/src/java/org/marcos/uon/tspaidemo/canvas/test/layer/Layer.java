package org.marcos.uon.tspaidemo.canvas.test.layer;

import javafx.beans.property.IntegerProperty;
import javafx.scene.canvas.Canvas;
import org.marcos.uon.tspaidemo.canvas.test.TransformationContext;

/**
 * Publically exposed interface; hides the
 */
public interface Layer {
    int getPriority();
    IntegerProperty priorityProperty();
    void setPriority(int priority);
    void setTransformationContext(TransformationContext context);
    boolean requiresRedraw();
    void requestRedraw();
    Canvas getCanvas();
    void draw();
}
