package org.marcos.uon.tspaidemo.canvas;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Internally contains one or more canvases
 * Note that text is skipped for now
 * (todo: rename? this is probably more generalised than it was before)
 */
public class CanvasGraph extends Pane {
    /**
     * Publically exposed interface; hides the
     * @param <T> the content contained in the layer
     */
    public interface Layer<T> extends List<T> {
        int getPriority();
        void setPriority(int priority);
        boolean requiresRedraw();
        void requestRedraw();
        Canvas getCanvas();
        void redraw();
    }
    private abstract class LayerImpl<T> extends ArrayList<T> implements Layer<T> {
        protected final Canvas canvas;
        protected int priority = -1; //higher is better
        protected boolean requiresRedraw;

        public LayerImpl(int priority) {
            canvas = new Canvas();
            this.priority = priority;
            requestReorder();
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            if(priority != this.priority) {
                this.priority = priority;
                requestReorder();
            }
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

        public abstract void redraw();
    }

    public class VertexLayer extends LayerImpl<Vertex> {
        public VertexLayer(int priority) {
            super(priority);
        }

        @Override
        public void redraw() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
            for (Vertex each : this) {
                double radiusToUse = Math.max(2, each.getDotRadius()*scale);
                double halfRad = radiusToUse/2;
                gc.setFill(each.getDotFill());
                Point2D minCorner = each.getLocation().multiply(scale).subtract(new Point2D(halfRad, halfRad));
                gc.fillOval(minCorner.getX(), minCorner.getY(), radiusToUse, radiusToUse);
            }
            requiresRedraw = false;
        }
    }

    public class EdgeLayer extends LayerImpl<Edge> {
        public EdgeLayer(int priority) {
            super(priority);
        }

        @Override
        public void redraw() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0, getWidth(), getHeight());
            for(Edge each : this) {
                gc.setStroke(each.getLineStroke());
                gc.setLineWidth(Math.max(0.5, scale*each.getLineWidth()));
                Point2D aPos = each.getA().getLocation().multiply(scale), bPos = each.getB().getLocation().multiply(scale);
                gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
            }
            requiresRedraw = false;
        }
    }

    public class OutlineEdgeLayer extends LayerImpl<Edge> {
        public OutlineEdgeLayer(int priority) {
            super(priority);
        }

        @Override
        public void redraw() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0, getWidth(), getHeight());
            for(Edge each : this) {
                Point2D aPos = each.getA().getLocation().multiply(scale), bPos = each.getB().getLocation().multiply(scale);
                gc.setLineWidth(Math.max(1, scale*each.getLineWidth()*2));
                gc.setStroke(each.getLineStroke());
                gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
                gc.setStroke(backgroundColor);
                gc.setLineWidth(Math.max(0.5, scale*each.getLineWidth()));
                gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
            }
            requiresRedraw = false;
        }
    }


    private Color backgroundColor;
    private final ObservableList<Layer> layers;
    private boolean requiresReordering;


    private double scale = 1;


    public CanvasGraph() {
        layers = FXCollections.observableArrayList();

        layers.addListener((ListChangeListener<Layer>) c -> {
                List<Node> children = getChildren();
                while(c.next()) {
                    if (c.wasRemoved()) {

                        for(Layer each : c.getRemoved()) {
                            Canvas eachCanvas = each.getCanvas();
                            eachCanvas.widthProperty().unbind();
                            eachCanvas.heightProperty().unbind();
                            children.remove(eachCanvas);
                        }
                        requiresReordering = true;
                    }
                    if (c.wasAdded()) {
                        for(Layer each : c.getAddedSubList()) {
                            Canvas eachCanvas = each.getCanvas();
                            eachCanvas.widthProperty().bind(widthProperty());
                            eachCanvas.heightProperty().bind(heightProperty());
                            children.add(eachCanvas);
                        }
                        requiresReordering = true;
                    }
                }
            }
        );

        setBackgroundColor(Color.WHITE);
        requiresReordering = false;

        //automatically redraw when resized
        widthProperty().addListener((x,y,z) -> requestAllRedraw());
        heightProperty().addListener((x,y,z) -> requestAllRedraw());
    }


    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
        this.backgroundColor = backgroundColor;
    }

    /**
     * Draws/redraws the canvas on an as-needed basis
     */
    public void draw() {
        if(requiresReordering) {
            layers.sorted(Comparator.comparing(Layer::getPriority)).forEach(each -> each.getCanvas().toFront());
            requiresReordering = false;
        }
        for (Layer each: layers) {
            if(each.requiresRedraw()) {
                each.redraw();
            }
        }
    }

    public void requestReorder() {
        requiresReordering = true;
    }

    public boolean requiresReordering() {
        return requiresReordering;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public ObservableList<Layer> getLayers() {
        return layers;
    }

    public VertexLayer addVertexLayer(int priority) {
        VertexLayer result = new VertexLayer(priority);
        layers.add(result);
        return result;
    }

    public EdgeLayer addEdgeLayer(int priority) {
        EdgeLayer result = new EdgeLayer(priority);
        layers.add(result);
        return result;
    }

    public OutlineEdgeLayer addOutlineEdgeLayer(int priority) {
        OutlineEdgeLayer result = new OutlineEdgeLayer(priority);
        layers.add(result);
        return result;
    }

    /**
     * Flag all layers as needing to redraw (because, for example, the canvas size has changed)
     */
    public void requestAllRedraw() {
        layers.forEach(Layer::requestRedraw);
    }
}
