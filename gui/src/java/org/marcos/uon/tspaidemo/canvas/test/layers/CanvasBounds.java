package org.marcos.uon.tspaidemo.canvas.test.layers;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.test.TransformationContext;
import org.marcos.uon.tspaidemo.canvas.test.layer.LayerBase;

public class CanvasBounds extends LayerBase {
    ChangeListener<Bounds> changeListener;

    public CanvasBounds(int priority) {
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

        Bounds canvasBounds = transformationContext.getCanvasBounds();
        gc.setLineWidth(1);
        gc.setStroke(Color.rgb(255, 255, 255, 0.5));
        gc.strokeRect(canvasBounds.getMinX(), canvasBounds.getMinY(), canvasBounds.getWidth(), canvasBounds.getHeight());
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
