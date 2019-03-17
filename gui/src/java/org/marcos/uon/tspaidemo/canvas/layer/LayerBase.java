package org.marcos.uon.tspaidemo.canvas.layer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import org.marcos.uon.tspaidemo.canvas.TransformationContext;

public abstract class LayerBase implements Layer {
    protected final Canvas canvas;
    private IntegerProperty priority;
    protected boolean requiresRedraw;
    protected TransformationContext transformationContext;

    public LayerBase(int priority) {
        this.priority = new SimpleIntegerProperty(priority);
        canvas = new Canvas();
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> requestRedraw());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> requestRedraw());
    }

    @Override
    public int getPriority() {
        return priority.get();
    }

    @Override
    public IntegerProperty priorityProperty() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority.set(priority);
    }

    @Override
    public void setTransformationContext(TransformationContext context) {
        this.transformationContext = context;
        requestRedraw();
    }

    @Override
    public boolean requiresRedraw() {
        return requiresRedraw;
    }

    @Override
    public void requestRedraw() {
        requiresRedraw = true;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public abstract void draw();
}
