package org.marcos.uon.tspaidemo.canvas;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

public class Vertex {
    private Point2D location;
    private double dotRadius;
    private String label;
    private Color dotFill;
    private Color labelFill;

    public Vertex(Point2D location, double radius, String label, Color dotFill, Color labelFill) {
        this.location = location;
        this.dotRadius = radius;
        this.label = label;
        this.dotFill = dotFill;
        this.labelFill = labelFill;
    }

    public Vertex(double x, double y, double radius, String label, Color dotFill, Color labelFill) {
        this(new Point2D(x, y), radius, label, dotFill, labelFill);
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public double getDotRadius() {
        return dotRadius;
    }

    public void setDotRadius(double dotRadius) {
        this.dotRadius = dotRadius;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Color getDotFill() {
        return dotFill;
    }

    public void setDotFill(Color dotFill) {
        this.dotFill = dotFill;
    }

    public Color getLabelFill() {
        return labelFill;
    }

    public void setLabelFill(Color labelFill) {
        this.labelFill = labelFill;
    }
}
