package org.marcos.uon.tspaidemo.canvas;

import javafx.beans.property.*;
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
    public static final double MIN_RADIUS = CanvasTSPGraph.DEFAULT_DOT_RADIUS;
    public static final double MIN_LINE_WIDTH = CanvasTSPGraph.DEFAULT_STROKE_WIDTH;
    public static final double MIN_DECORATION_SCALE = 1, MAX_DECORATION_SCALE = 5;

    public static double computeDecorationScale(double requestedScale) {
        if(requestedScale < MIN_DECORATION_SCALE) {
            return MIN_DECORATION_SCALE;
        } else if (requestedScale > MAX_DECORATION_SCALE) {
            return MAX_DECORATION_SCALE;
        } else {
            return requestedScale;
        }
    }

    /**
     * Computes the radius used when drawing the vertex
     * @param preferredRadius
     * @param scale
     */
    public static double computeRadiusToUse(double preferredRadius, double scale) {
        return preferredRadius*computeDecorationScale(scale);
    }

    /**
     * Computes the line width used when drawing the vertex
     * @param preferredWidth
     * @param scale
     */
    public static double computeStrokeWidthToUse(double preferredWidth, double scale) {
        return preferredWidth*computeDecorationScale(scale);
    }
    /**
     * Computes the line width used when drawing the vertex
     * @param preferredWidth
     * @param scale
     */
    public static double computeLineWidthToUse(double preferredWidth, double scale) {
        return  preferredWidth*computeDecorationScale(scale);
    }

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

        private final ReadOnlyObjectWrapper<Bounds> logicalBounds;
        private final ReadOnlyObjectWrapper<Bounds> boundsInLocal;
        private final ReadOnlyObjectWrapper<Bounds> boundsInCanvas;
        private final BooleanProperty boundsValid = new SimpleBooleanProperty(true);

//        //these corner vertices are used for scaling/translation purposes;
//        private Vertex
        public VertexLayer(int priority) {
            super(priority);
            logicalBounds = new ReadOnlyObjectWrapper<>();
            boundsInLocal = new ReadOnlyObjectWrapper<>();
            boundsInCanvas = new ReadOnlyObjectWrapper<>();
            updateBounds();
            boundsValid.addListener((observable, oldValue, newValue) -> {
                if(!newValue) {
                    updateBounds();
                    boundsValid.set(true);
                }
            });
        }

        @Override
        public void redraw() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.clearRect(0,0, canvas.getWidth(), canvas.getHeight());
            double scale = dragContext.getScale();
            for (Vertex each : this) {
                double radiusToUse = computeRadiusToUse(each.getDotRadius(), scale);
                double lineWidthToUse = computeLineWidthToUse(each.getStrokeWidth(), scale);
                gc.setFill(each.getDotFill());
                Point2D minCorner = dragContext.localToCanvas(each.getLocation()).subtract(new Point2D(radiusToUse, radiusToUse));
                gc.fillOval(minCorner.getX(), minCorner.getY(), radiusToUse*2, radiusToUse*2);
                gc.setLineWidth(lineWidthToUse);
                gc.setStroke(each.getDotStroke());
                gc.strokeOval(minCorner.getX(), minCorner.getY(), radiusToUse*2, radiusToUse*2);
            }
            requiresRedraw = false;
        }

        /**
         * {@inheritDoc}
         * Note: also updates the boundsInLocal
         */
        @Override
        public void requestRedraw() {
            //if this is the first redraw request since the last draw, update the bounds
            if (boundsValid.get()) {
                boundsValid.setValue(false);
            }
            requiresRedraw = true;
        }

        /**
         * Returns the bounds as determined by the cells
         * @return
         */
        private void updateBounds() {
//        (todo: cater for text in the bounds check?)
            double minLogicalX=Double.POSITIVE_INFINITY, minLogicalY=Double.POSITIVE_INFINITY, maxLogicalX=Double.NEGATIVE_INFINITY, maxLogicalY=Double.NEGATIVE_INFINITY;
            double minLocalX=Double.POSITIVE_INFINITY, minLocalY=Double.POSITIVE_INFINITY, maxLocalX=Double.NEGATIVE_INFINITY, maxLocalY=Double.NEGATIVE_INFINITY;
            double minInCanvasX=Double.POSITIVE_INFINITY, minInCanvasY=Double.POSITIVE_INFINITY, maxInCanvasX=Double.NEGATIVE_INFINITY, maxInCanvasY=Double.NEGATIVE_INFINITY;
            double scale = dragContext.getScale();
            double translationX = dragContext.getTranslationX();
            double translationY = dragContext.getTranslationY();
            for(Vertex each : this) {
                double x = each.getLocation().getX();
                double y = each.getLocation().getY();
                double baseRadius = CanvasGraph.computeRadiusToUse(each.getDotRadius(), 1) + CanvasGraph.computeStrokeWidthToUse(each.getStrokeWidth(), 1)/2.0;
                double mnX = x-baseRadius, mxX=x+baseRadius, mnY=y-baseRadius, mxY=y+baseRadius;
                double appliedRadius = CanvasGraph.computeRadiusToUse(each.getDotRadius(), scale) + CanvasGraph.computeLineWidthToUse(each.getStrokeWidth(), scale)/2.0;
                double icX = (x+translationX)*scale, icY = (y+translationY)*scale;
                double mnicX = icX-appliedRadius, mxicX = icX+appliedRadius, mnicY = icY-appliedRadius, mxicY = icY+appliedRadius;
                
                if(minLogicalX > x) {
                    minLogicalX=x;
                }
                if(minLogicalY > y) {
                    minLogicalY=y;
                }
                if(maxLogicalX < x) {
                    maxLogicalX=x;
                }
                if(maxLogicalY < y) {
                    maxLogicalY=y;
                }

                if(minLocalX > mnX) {
                    minLocalX=mnX;
                }
                if(minLocalY > mnY) {
                    minLocalY=mnY;
                }
                if(maxLocalX < mxX) {
                    maxLocalX=mxX;
                }
                if(maxLocalY < mxY) {
                    maxLocalY=mxY;
                }

                if(minInCanvasX > mnicX) {
                    minInCanvasX=mnicX;
                }
                if(minInCanvasY > mnicY) {
                    minInCanvasY=mnicY;
                }
                if(maxInCanvasX < mxicX) {
                    maxInCanvasX=mxicX;
                }
                if(maxInCanvasY < mxicY) {
                    maxInCanvasY=mxicY;
                }
            }

            logicalBounds.set(new BoundingBox(minLogicalX, minLogicalY, maxLogicalX-minLogicalX, maxLogicalY-minLogicalY));
            boundsInLocal.set(new BoundingBox(minLocalX, minLocalY, maxLocalX-minLocalX, maxLocalY-minLocalY));
            boundsInCanvas.set(new BoundingBox(minInCanvasX, minInCanvasY, maxInCanvasX-minInCanvasX, maxInCanvasY-minInCanvasY));
        }

        public Bounds getLogicalBounds() {
            return logicalBounds.get();
        }

        public ReadOnlyObjectProperty<Bounds> logicalBoundsProperty() {
            return logicalBounds.getReadOnlyProperty();
        }

        public void setLogicalBounds(Bounds logicalBounds) {
            this.logicalBounds.set(logicalBounds);
        }

        public Bounds getBoundsInLocal() {
            return boundsInLocal.get();
        }

        public ReadOnlyObjectProperty<Bounds> boundsInLocalProperty() {
            return boundsInLocal.getReadOnlyProperty();
        }

        public void setBoundsInLocal(Bounds boundsInLocal) {
            this.boundsInLocal.set(boundsInLocal);
        }

        public Bounds getBoundsInCanvas() {
            return boundsInCanvas.get();
        }

        public ReadOnlyObjectProperty<Bounds> boundsInCanvasProperty() {
            return boundsInCanvas.getReadOnlyProperty();
        }

        public void setBoundsInCanvas(Bounds boundsInCanvas) {
            this.boundsInCanvas.set(boundsInCanvas);
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
            double scale = dragContext.getScale();
            for(Edge each : this) {
                gc.setStroke(each.getLineStroke());
                gc.setLineWidth(computeLineWidthToUse(each.getLineWidth(), scale));
                Point2D aPos =  dragContext.localToCanvas(each.getA().getLocation()), bPos = dragContext.localToCanvas(each.getB().getLocation());
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
            double scale = dragContext.getScale();
            for(Edge each : this) {
                double lineWidthToUse = computeLineWidthToUse(each.getLineWidth(), scale);
                Point2D aPos =  dragContext.localToCanvas(each.getA().getLocation()), bPos = dragContext.localToCanvas(each.getB().getLocation());
                gc.setLineWidth(2*lineWidthToUse);
                gc.setStroke(each.getLineStroke());
                gc.strokeLine(aPos.getX(), aPos.getY(), bPos.getX(), bPos.getY());
                gc.setStroke(backgroundColor);
                gc.setLineWidth(lineWidthToUse);
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
        requiresReordering = false;

        //automatically redraw when resized
        widthProperty().addListener((x,y,z) -> requestAllRedraw());
        heightProperty().addListener((x,y,z) -> requestAllRedraw());
        this.dragContext = new DragContext();
        dragContext.canvasBoundsProperty().bind(boundsInLocalProperty());
//        this.dragContext.mouseAnchorXProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
//        this.dragContext.mouseAnchorYProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
        this.dragContext.translationXProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
        this.dragContext.translationYProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
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
