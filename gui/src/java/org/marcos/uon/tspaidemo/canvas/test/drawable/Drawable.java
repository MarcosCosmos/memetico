package org.marcos.uon.tspaidemo.canvas.test.drawable;

import javafx.scene.canvas.GraphicsContext;
import org.marcos.uon.tspaidemo.canvas.test.TransformationContext;

public interface Drawable {
    void drawOnto(GraphicsContext graphicsContext, TransformationContext transformationContext);
}
