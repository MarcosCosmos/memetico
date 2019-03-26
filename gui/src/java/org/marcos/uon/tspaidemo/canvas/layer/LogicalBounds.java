package org.marcos.uon.tspaidemo.canvas.layer;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.TransformationContext;
import org.marcos.uon.tspaidemo.canvas.layer.LayerBase;

public class LogicalBounds extends LayerBase {
    ChangeListener<Bounds> changeListener;

    public LogicalBounds(int priority) {
        super(priority);
        changeListener = ((observable, oldValue, newValue) -> {
            if(!newValue.equals(oldValue)) {
                requestRedraw();
            }
        });
    }

    @Override
    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());

        Bounds logicalBounds = transformationContext.getLogicalBounds();
        gc.setLineWidth(1);
        gc.setFill(Color.rgb(255, 0, 0, 0.25));
        gc.fillRect(logicalBounds.getMinX(), logicalBounds.getMinY(), logicalBounds.getWidth(), logicalBounds.getHeight());
        gc.setStroke(Color.rgb(255, 0, 0, 0.5));
        gc.strokeRect(logicalBounds.getMinX(), logicalBounds.getMinY(), logicalBounds.getWidth(), logicalBounds.getHeight());
        requiresRedraw = false;
    }

    @Override
    public void setTransformationContext(TransformationContext context) {
        if(transformationContext != null) {
            transformationContext.logicalBoundsProperty().removeListener(changeListener);
        }
        super.setTransformationContext(context);
        context.logicalBoundsProperty().addListener(changeListener);
        requestRedraw(); //trigger the first update
    }
}
