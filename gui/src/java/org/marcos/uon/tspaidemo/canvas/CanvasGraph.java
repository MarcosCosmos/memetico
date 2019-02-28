package org.marcos.uon.tspaidemo.canvas;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
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
//
//    public void setPivot(double x, double y) {
//        setTranslateX(getTranslateX() - x);
//        setTranslateY(getTranslateY() - y);
//    }
//
//

    ViewportGestures gestures;

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
        private BoundingBox logicalBounds;
        public static final double MIN_RADIUS = 4;
        public VertexLayer(int priority) {
            super(priority);
            computeLogicalBounds();
        }

        @Override
        public void redraw() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
            Point2D dragOffset = new Point2D(dragContext.getTranslateAnchorX(), dragContext.getTranslateAnchorY());
            double scale = dragContext.getScale();
//            gc.setFill(Color.BLACK);
//
//            Bounds logicalBounds = dragContext.getLogicalBoundsRaw();
//            Bounds viewportBounds = dragContext.getViewportBounds();
//            double translatedMin = (logicalBounds.getMinY()+dragOffset.getY())*scale;
//            double translatedMax = (logicalBounds.getMaxY()+dragOffset.getY())*scale;
//            gc.fillRect(dragOffset.getX(), translatedMin, dragContext.getLogicalBoundsScaled().getWidth(), dragContext.getLogicalBoundsScaled().getHeight());
            for (Vertex each : this) {
                double radiusToUse = Math.max(MIN_RADIUS, each.getDotRadius()*scale);
                double halfRad = radiusToUse/2;
                gc.setFill(each.getDotFill());
                Point2D minCorner = each.getLocation().add(dragOffset).multiply(scale).subtract(new Point2D(halfRad, halfRad));
                gc.fillOval(minCorner.getX(), minCorner.getY(), radiusToUse, radiusToUse);
            }
            requiresRedraw = false;
        }

        /**
         * Returns the bounds as determined by the cells
         * @return
         */
        private void computeLogicalBounds() {
//        (todo: cater for text in the bounds check?)
            double minX=Double.POSITIVE_INFINITY, minY=Double.POSITIVE_INFINITY, maxX=Double.NEGATIVE_INFINITY, maxY=Double.NEGATIVE_INFINITY;
            for(Vertex each : this) {
                double x = each.getLocation().getX();
                double y = each.getLocation().getY();
                double radius = each.getDotRadius();
                double mnX = x-radius, mxX = x+radius, mnY = y-radius, mxY = y+radius;
                if(minX > mnX) {
                    minX=mnX;
                }
                if(minY > mnY) {
                    minY=mnY;
                }
                if(maxX < mxX) {
                    maxX=mxX;
                }
                if(maxY < mxY) {
                    maxY=mxY;
                }
            }

            logicalBounds = new BoundingBox(minX, minY, maxX-minX, maxY-minY);
            dragContext.setLogicalBoundsRaw(logicalBounds);
        }

        /**
         * {@inheritDoc}
         * Note: also updates the logicalBounds
         */
        @Override
        public void requestRedraw() {
            //if this is the first redraw request since the last draw, update the bounds
            if (!requiresRedraw()) {
                computeLogicalBounds();
            }
            requiresRedraw = true;
        }

        public BoundingBox getLogicalBounds() {
            return logicalBounds;
        }
    }

    public class EdgeLayer extends LayerImpl<Edge> {

        public static final double MIN_LINE_WIDTH = 1;
        public EdgeLayer(int priority) {
            super(priority);
        }

        @Override
        public void redraw() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0, getWidth(), getHeight());
            double scale = dragContext.getScale();
            Point2D dragOffset = new Point2D(dragContext.getTranslateAnchorX(), dragContext.getTranslateAnchorY());
            for(Edge each : this) {
                gc.setStroke(each.getLineStroke());
                gc.setLineWidth(Math.max(MIN_LINE_WIDTH, scale*each.getLineWidth()));
                Point2D aPos = each.getA().getLocation().add(dragOffset).multiply(scale), bPos = each.getB().getLocation().add(dragOffset).multiply(scale);
                gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
            }
            requiresRedraw = false;
        }
    }

    public class OutlineEdgeLayer extends LayerImpl<Edge> {

        public static final double MIN_LINE_WIDTH = EdgeLayer.MIN_LINE_WIDTH;
        public OutlineEdgeLayer(int priority) {
            super(priority);
        }

        @Override
        public void redraw() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0, getWidth(), getHeight());
            double scale = dragContext.getScale();
            Point2D dragOffset = new Point2D(dragContext.getTranslateAnchorX(), dragContext.getTranslateAnchorY());
            for(Edge each : this) {
                Point2D aPos = each.getA().getLocation().add(dragOffset).multiply(scale), bPos = each.getB().getLocation().add(dragOffset).multiply(scale);
                gc.setLineWidth(Math.max(2*MIN_LINE_WIDTH, scale*each.getLineWidth()*2));
                gc.setStroke(each.getLineStroke());
                gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
                gc.setStroke(backgroundColor);
                gc.setLineWidth(Math.max(MIN_LINE_WIDTH, scale*each.getLineWidth()));
                gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
            }
            requiresRedraw = false;
        }
    }


    private Color backgroundColor;
    private final ObservableList<Layer> layers;
    private final DragContext dragContext;
    private boolean requiresReordering;


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

        setBackgroundColor(Color.BLACK);
        styleProperty().set("-fx-border-color:white;");
        requiresReordering = false;

        //automatically redraw when resized
        widthProperty().addListener((x,y,z) -> requestAllRedraw());
        heightProperty().addListener((x,y,z) -> requestAllRedraw());
        this.dragContext = new DragContext();
        dragContext.viewportBoundsProperty().bind(boundsInLocalProperty());
//        this.dragContext.mouseAnchorXProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
//        this.dragContext.mouseAnchorYProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
        this.dragContext.translateAnchorXProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
        this.dragContext.translateAnchorYProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
        this.dragContext.scaleProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
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

    public DragContext getDragContext() {
        return dragContext;
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
