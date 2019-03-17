package org.marcos.uon.tspaidemo.canvas.drawable;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.TransformationContext;

public class OutlineEdge extends Edge {
    public OutlineEdge(Vertex a, Vertex b, String label, Color lineStroke, Color labelFill, double lineWidth) {
        super(a, b, label, lineStroke, labelFill, lineWidth);
    }

    public OutlineEdge(Vertex a, Vertex b, String label, Color lineStroke, Color labelFill) {
        super(a, b, label, lineStroke, labelFill);
    }

    @Override
    public void drawOnto(GraphicsContext graphicsContext, TransformationContext transformationContext) {
        double lineWidthToUse = transformationContext.computeLineWidthToUse(getLineWidth());
        graphicsContext.setLineWidth(lineWidthToUse);
        graphicsContext.setStroke(getLineStroke());
        Point2D aPos = transformationContext.localToCanvas(getA().getLocation());
        Point2D bPos = transformationContext.localToCanvas(getB().getLocation());

        Point2D asLineWidthUnit = bPos.subtract(aPos).normalize().multiply(lineWidthToUse);
        Point2D clockwisePerpendicular = new Point2D(asLineWidthUnit.getY(), -asLineWidthUnit.getX());
        Point2D counterClockwisePerpendicular = new Point2D(-asLineWidthUnit.getY(), asLineWidthUnit.getX());

        Point2D tmpA = aPos.add(clockwisePerpendicular);
        Point2D tmpB = bPos.add(clockwisePerpendicular);
        graphicsContext.strokeLine(tmpA.getX(), tmpA.getY(), tmpB.getX(), tmpB.getY());

        tmpA = aPos.add(counterClockwisePerpendicular);
        tmpB = bPos.add(counterClockwisePerpendicular);
        graphicsContext.strokeLine(tmpA.getX(), tmpA.getY(), tmpB.getX(), tmpB.getY());

    }
}
