package org.marcos.uon.tspaidemo.canvas.test.layers;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.test.TransformationContext;
import org.marcos.uon.tspaidemo.canvas.test.layer.LayerBase;

public class BoundsInLocal extends LayerBase {
    ChangeListener<Bounds> changeListener;

    public BoundsInLocal(int priority) {
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
        Bounds boundsInLocal = transformationContext.getBoundsInLocal();

        gc.setLineWidth(1);
        gc.setFill(Color.rgb(0, 255, 0, 0.25));
        gc.fillRect(boundsInLocal.getMinX(), boundsInLocal.getMinY(), boundsInLocal.getWidth(), boundsInLocal.getHeight());
        gc.setStroke(Color.rgb(0, 255, 0, 0.5));
        gc.strokeRect(boundsInLocal.getMinX(), boundsInLocal.getMinY(), boundsInLocal.getWidth(), boundsInLocal.getHeight());
        requiresRedraw = false;
    }

    @Override
    public void setTransformationContext(TransformationContext context) {
        if(transformationContext != null) {
            transformationContext.boundsInLocalProperty().removeListener(changeListener);
            transformationContext.boundsInCanvasProperty().removeListener(changeListener);
        }
        super.setTransformationContext(context);
        context.boundsInLocalProperty().addListener(changeListener);
        context.boundsInCanvasProperty().addListener(changeListener);
        requestRedraw(); //trigger the first update
    }
}
