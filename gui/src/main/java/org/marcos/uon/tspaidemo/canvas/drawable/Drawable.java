package org.marcos.uon.tspaidemo.canvas.drawable;

import javafx.scene.canvas.GraphicsContext;
import org.marcos.uon.tspaidemo.canvas.TransformationContext;

public interface Drawable {
    void drawOnto(GraphicsContext graphicsContext, TransformationContext transformationContext);
}
