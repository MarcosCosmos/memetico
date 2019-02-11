package org.marcos.uon.tspaidemo.canvas;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Internally contains one or more canvases
 * Note that text is skipped for now
 */
public class CanvasGraph extends Pane {
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Color backgroundColor;

    private Canvas verticesLayer;
    private Canvas edgesLayer;
    private Canvas backgroundLayer;

    private double scale = 1;


    public CanvasGraph() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        backgroundColor = Color.WHITE;
        backgroundLayer = new Canvas();
        verticesLayer = new Canvas();
        edgesLayer = new Canvas();
        backgroundLayer.widthProperty().bind(this.widthProperty());
        backgroundLayer.heightProperty().bind(this.heightProperty());
        verticesLayer.widthProperty().bind(this.widthProperty());
        verticesLayer.heightProperty().bind(this.heightProperty());
        edgesLayer.widthProperty().bind(this.widthProperty());
        edgesLayer.heightProperty().bind(this.heightProperty());
        getChildren().setAll(backgroundLayer,edgesLayer,verticesLayer);
        edgesLayer.toFront();
        verticesLayer.toFront();
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    //draws/redraws the canvas
    public void draw() {
        double width = this.getWidth();
        double height = this.getHeight();
        GraphicsContext gc = backgroundLayer.getGraphicsContext2D();
        gc.setFill(backgroundColor);
        gc.fillRect(0,0, width, height);

        gc = edgesLayer.getGraphicsContext2D();
        gc.clearRect(0,0, width, height);
        for(Edge each : edges) {
            gc.setStroke(each.getLineStroke());
            gc.setLineWidth(scale);
            Vertex a = each.getA(), b = each.getB();
            double halfRad = a.getDotRadius()/2;
            Point2D halfRadVec = new Point2D(halfRad, halfRad);
            Point2D aPos = a.getLocation().add(halfRadVec).multiply(scale), bPos = b.getLocation().add(halfRadVec).multiply(scale);
            gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
        }

        gc = verticesLayer.getGraphicsContext2D();
        gc.clearRect(0,0, width, height);
        for (Vertex each : vertices) {
           Point2D point = each.getLocation().multiply(scale);
           double radius = each.getDotRadius()*scale;
           gc.setFill(each.getDotFill());
           gc.fillOval(point.getX(), point.getY(), radius, radius);
        }
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }
}
