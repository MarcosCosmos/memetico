package org.marcos.uon.tspaidemo.canvas.test.layers;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.test.TransformationContext;
import org.marcos.uon.tspaidemo.canvas.test.layer.LayerBase;

public class BoundsInCanvas extends LayerBase {
    ChangeListener<Bounds> changeListener;

    public BoundsInCanvas(int priority) {
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
        
        Bounds boundsInCanvas = transformationContext.getBoundsInCanvas();
        
        gc.setLineWidth(1);
        gc.setFill(Color.rgb(0, 0, 255, 0.25));
        gc.fillRect(boundsInCanvas.getMinX(), boundsInCanvas.getMinY(), boundsInCanvas.getWidth(), boundsInCanvas.getHeight());
        gc.setStroke(Color.rgb(0, 0, 255, 0.5));
        gc.strokeRect(boundsInCanvas.getMinX(), boundsInCanvas.getMinY(), boundsInCanvas.getWidth(), boundsInCanvas.getHeight());
        requiresRedraw = false;
    }

    @Override
    public void setTransformationContext(TransformationContext context) {
        if(transformationContext != null) {
            transformationContext.boundsInLocalProperty().removeListener(changeListener);
        }
        super.setTransformationContext(context);
        context.boundsInLocalProperty().addListener(changeListener);
        requestRedraw(); //trigger the first update
    }
}
