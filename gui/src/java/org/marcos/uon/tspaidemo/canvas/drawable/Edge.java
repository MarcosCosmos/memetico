package org.marcos.uon.tspaidemo.canvas.drawable;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.TransformationContext;

public class Edge implements Drawable {
    private Vertex a;
    private Vertex b;
    private String label;
    private Color lineStroke;
    private Color labelFill;
    private double lineWidth;

    public Edge(Vertex a, Vertex b, String label, Color lineStroke, Color labelFill, double lineWidth) {
        this.a = a;
        this.b = b;
        this.label = label;
        this.lineStroke = lineStroke;
        this.labelFill = labelFill;
        this.lineWidth = lineWidth;
    }

    public Edge(Vertex a, Vertex b, String label, Color lineStroke, Color labelFill) {
        this(a,b,label,lineStroke,labelFill,1);
    }


    public Vertex getA() {
        return a;
    }

    public void setA(Vertex a) {
        this.a = a;
    }

    public Vertex getB() {
        return b;
    }

    public void setB(Vertex b) {
        this.b = b;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Color getLineStroke() {
        return lineStroke;
    }

    public void setLineStroke(Color lineStroke) {
        this.lineStroke = lineStroke;
    }

    public Color getLabelFill() {
        return labelFill;
    }

    public void setLabelFill(Color labelFill) {
        this.labelFill = labelFill;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth) {
        this.lineWidth = lineWidth;
    }

    @Override
    public void drawOnto(GraphicsContext graphicsContext, TransformationContext transformationContext) {
        graphicsContext.setStroke(lineStroke);
        graphicsContext.setLineWidth(transformationContext.computeLineWidthToUse(lineWidth));
        Point2D aPos = transformationContext.localToCanvas(a.getLocation());
        Point2D bPos = transformationContext.localToCanvas(b.getLocation());
        graphicsContext.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
    }
}
