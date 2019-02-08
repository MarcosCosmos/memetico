package org.marcos.uon.tspaidemo.canvas;

import javafx.scene.paint.Color;

public class Edge {
    private Vertex a;
    private Vertex b;
    private String label;
    private Color lineStroke;
    private Color labelFill;

    public Edge(Vertex a, Vertex b, String label, Color lineStroke, Color labelFill) {
        this.a = a;
        this.b = b;
        this.label = label;
        this.lineStroke = lineStroke;
        this.labelFill = labelFill;
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
}
