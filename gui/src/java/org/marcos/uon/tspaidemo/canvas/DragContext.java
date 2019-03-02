package org.marcos.uon.tspaidemo.canvas;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

public class DragContext {

    private DoubleProperty mouseAnchorX;
    private DoubleProperty mouseAnchorY;

    private DoubleProperty translationX;
    private DoubleProperty translationY;
    private DoubleProperty scale;
    private ObjectProperty<Bounds> logicalBounds, boundsInLocal, boundsInCanvas, canvasBounds;

    //track the targeted "ideal" scale - the largest scale that shows everything
    private DoubleProperty autoScale;
    private DoubleProperty autoTranslationX;
    private DoubleProperty autoTranslationY;

//    private ChangeListener<Bounds> minScaleAdjusterInstance =

    private BooleanProperty transformAutomatically = new SimpleBooleanProperty(false);

    private DoubleProperty maxScaleProperty;

    private void minScaleAdjuster(ObservableValue<? extends Bounds> canvasBounds, Bounds oldBounds, Bounds newBounds) {
        if(getScale() < getAutoScale()) {
            transformAutomatically.set(true);
        }
    }

    public DragContext() {
        mouseAnchorX = new SimpleDoubleProperty(0);
        mouseAnchorY = new SimpleDoubleProperty(0);
        translationX = new SimpleDoubleProperty(0);
        translationY = new SimpleDoubleProperty(0);

        scale = new SimpleDoubleProperty(1);
        autoScale = new SimpleDoubleProperty(scale.get());
        autoTranslationX = new SimpleDoubleProperty(translationX.get());
        autoTranslationY = new SimpleDoubleProperty(translationY.get());
        logicalBounds = new SimpleObjectProperty<>(new BoundingBox(0,0,100,100));
        boundsInLocal = new SimpleObjectProperty<>(logicalBounds.get());
        boundsInCanvas = new SimpleObjectProperty<>(logicalBounds.get());
        canvasBounds = new SimpleObjectProperty<>(logicalBounds.get()); //arbitrary default really
//        boundsInCanvas = Bindings.createObjectBinding(
//                () -> {
//                    double scale = getScale();
//                    Bounds local = boundsInLocal.get();
//                    Point2D min = localToCanvas(local.getMinX(), local.getMinY());
//                    //todo: make this more robust vertices with custom sizes
//                    double appliedRadius = CanvasGraph.computeRadiusToUse(CanvasGraph.MIN_RADIUS, scale) + CanvasGraph.computeLineWidthToUse(CanvasGraph.MIN_LINE_WIDTH, scale);
//                    return new BoundingBox(min.getX()-padding, min.getY()-padding, local.getWidth()*scale + 2*padding, local.getHeight()*scale + 2*padding);
//                },
//                boundsInLocal, scale, translationX, translationY
//        );

        autoScale.bind(Bindings.createDoubleBinding(
                () -> {
                    Bounds canvasBounds = getCanvasBounds();
                    Bounds boundsInLocal = getBoundsInLocal();
                    double naiveScale = Math.min(canvasBounds.getWidth()/boundsInLocal.getWidth(), canvasBounds.getHeight()/boundsInLocal.getHeight());
                    double decorationScale = CanvasGraph.computeDecorationScale(naiveScale);
                    //if we are trying to scale outside of the normal decoration scale range, we will need to factor in the clamped decoration padding
                    if(naiveScale == decorationScale) {
                        return naiveScale;
                    } else {
                        Bounds logicalBounds = getLogicalBounds();
                        double baseDecorationPaddingX = boundsInLocal.getWidth()-logicalBounds.getWidth();
                        double baseDecorationPaddingY = boundsInLocal.getHeight()-logicalBounds.getHeight();
                        return Math.min((canvasBounds.getWidth()-(baseDecorationPaddingX*decorationScale))/logicalBounds.getWidth(), (canvasBounds.getHeight()-(baseDecorationPaddingY*decorationScale))/logicalBounds.getHeight());
                    }
                },
                canvasBounds, boundsInLocal
        ));

        autoTranslationX.bind(Bindings.createDoubleBinding(() -> {
                    double scale = getScale();
                    Bounds canvasBounds = getCanvasBounds();
                    Bounds logicalBounds = getLogicalBounds();
                    Bounds boundsInLocal = getBoundsInLocal();
                    double baseDecorationPaddingX = boundsInLocal.getWidth()-logicalBounds.getWidth();
                    return ((canvasBounds.getWidth() - (getLogicalBounds().getWidth()+baseDecorationPaddingX)*scale) / 2) / scale - getBoundsInLocal().getMinX();
                },
                canvasBounds, boundsInLocal, scale
        ));
        autoTranslationY.bind(Bindings.createDoubleBinding(() -> {
                    double scale = getScale();
                    Bounds canvasBounds = getCanvasBounds();
                    Bounds logicalBounds = getLogicalBounds();
                    Bounds boundsInLocal = getBoundsInLocal();
                    double baseDecorationPaddingY = boundsInLocal.getHeight()-logicalBounds.getHeight();
                    return ((canvasBounds.getHeight() - (getLogicalBounds().getHeight()+baseDecorationPaddingY)*scale) / 2) / scale - getBoundsInLocal().getMinY();
                },
                canvasBounds, boundsInLocal, scale
        ));



        transformAutomatically.addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                scale.bind(autoScale);
                translationX.bind(autoTranslationX);
                translationY.bind(autoTranslationY);
            } else {
                scale.unbind();
                translationX.unbind();
                translationY.unbind();
                canvasBounds.addListener(this::minScaleAdjuster);
            }
        });

        maxScaleProperty = new SimpleDoubleProperty(1);
        maxScaleProperty.bind(
                Bindings.createDoubleBinding(
                        () -> {
                            Bounds logicalBounds = getLogicalBounds();
                            return 10*Math.max(logicalBounds.getWidth(), logicalBounds.getHeight());
                        }
                )
        );


        setTransformAutomatically(true);
    }

    public Point2D localToCanvas(Point2D point) {
        double scale = getScale();
        if(scale == 0) {
            return new Point2D(0,0);
        } else {
            return point.add(translationX.get(), translationY.get()).multiply(scale);
        }
    }

    public Point2D localToCanvas(double x, double y) {
        return localToCanvas(new Point2D(x,y));
    }

    public Point2D canvasToLocal(Point2D point) {
        double scale = getScale();
        if (scale == 0) {
            return point.subtract(translationX.get(), translationY.get());
        } else {
            return point.multiply(1 / scale).subtract(translationX.get(), translationY.get());
        }
    }
    public Point2D canvasToLocal(double x, double y) {
        return canvasToLocal(new Point2D(x,y));
    }

    public double getMouseAnchorX() {
        return mouseAnchorX.get();
    }

    public DoubleProperty mouseAnchorXProperty() {
        return mouseAnchorX;
    }

    public void setMouseAnchorX(double mouseAnchorX) {
        this.mouseAnchorX.set(mouseAnchorX);
    }

    public double getMouseAnchorY() {
        return mouseAnchorY.get();
    }

    public DoubleProperty mouseAnchorYProperty() {
        return mouseAnchorY;
    }

    public void setMouseAnchorY(double mouseAnchorY) {
        this.mouseAnchorY.set(mouseAnchorY);
    }
    
    public Point2D getMouseAnchor() {
        return new Point2D(getMouseAnchorX(), getMouseAnchorY());
    }

    public void setMouseAnchor(Point2D anchor) {
        setMouseAnchor(anchor.getX(), anchor.getY());
    }
    public void setMouseAnchor(double x, double y) {
        setMouseAnchorX(x);
        setMouseAnchorY(y);
    }

    public double getTranslationX() {
        return translationX.get();
    }

    /**
     * Note that it is almost always better to use the setters or {@code #translate(int x, int y)} then to set via properties, as this will ensure that objects don't move entirely off-screen
     * @see #setTranslationX(double)
     * @see #translate(double, double)
     * @return
     */
    public DoubleProperty translationXProperty() {
        return translationX;
    }
    
    public void setTranslationX(double translationX) {
        setTransformAutomatically(false);
        this.translationX.set(translationX);
    }
    
    public double getTranslationY() {
        return translationY.get();
    }

    /**
     * Note that it is almost always better to use the setters or {@code #translate(int x, int y)} then to set via properties, as this will ensure that objects don't move entirely off-screen
     * @see #setTranslationY(double)
     * @see #translate(double, double)
     * @return
     */
    public DoubleProperty translationYProperty() {
        return translationY;
    }

    public void setTranslationY(double translationY) {
        setTransformAutomatically(false);
        this.translationY.set(translationY);
    }

    //prevents the translation from leaving components unnecessarily out of bounds
    public void santiseTranslation() {
        Bounds canvasBounds = getCanvasBounds();
        Bounds boundsInCanvas = getBoundsInCanvas();
        double newTranslationX = getTranslationX(), newTranslationY = getTranslationY();
        double scale = getScale();
        boolean autoX = false;
        boolean autoY = false;
        if(boundsInCanvas.getWidth() <= canvasBounds.getWidth()) {
            newTranslationX = autoTranslationX.get();
            autoX = true;
        } else {
            if(boundsInCanvas.getMaxX() < canvasBounds.getMaxX()) {
                newTranslationX += (canvasBounds.getMaxX()-boundsInCanvas.getMaxX())/scale;//(canvasBounds.getWidth() - boundsInCanvas.getWidth())/scale-boundsInLocal.getMinX();
            } else if (boundsInCanvas.getMinX() > canvasBounds.getMinX()) {
                newTranslationX += (canvasBounds.getMinX()-boundsInCanvas.getMinX())/scale;
            }
        }
        if(boundsInCanvas.getHeight() <= canvasBounds.getHeight()) {
            newTranslationY = autoTranslationY.get();
            autoY = true;
        } else {
            if(boundsInCanvas.getMaxY() < canvasBounds.getMaxY()) {
                newTranslationY += (canvasBounds.getMaxY()-boundsInCanvas.getMaxY())/scale;//(canvasBounds.getHeight() - boundsInCanvas.getHeight())/scale-boundsInLocal.getMinY();
            } else if (boundsInCanvas.getMinY() > canvasBounds.getMinY()) {
                newTranslationY += (canvasBounds.getMinY()-boundsInCanvas.getMinY())/scale;
            }
        }
        //if we are automatically managing both x and y - we may as well enable autotransforms entirely
        if(autoX && autoY) {
            transformAutomatically.set(true);
        } else {
            setTranslation(newTranslationX, newTranslationY);
        }
    }


    public void translate(double x, double y) {
        setTranslation(getTranslationX() + x, getTranslationY() + y);
    }

    public void setTranslation(Point2D translation) {
        setTranslation(translation.getX(), translation.getY());
    }

    public void setTranslation(double x, double y) {
        setTranslationX(x);
        setTranslationY(y);
    }
    
    public Point2D getTranslation() {
        return new Point2D(getTranslationX(), getTranslationY());
    }



    public double getScale() {
        return scale.get();
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public void setScale(double scale) {
        scale = Math.min(maxScaleProperty.get(), scale);
        setTransformAutomatically(false);
        scale = Math.max(scale, getAutoScale());
        if(scale == 0) {
            return;
        }
        double oldScale = getScale();
        Point2D originForScale;
        Point2D mouseAnchor = getMouseAnchor();
        Bounds boundsInCanvas = getBoundsInCanvas();
        Point2D minInCanvas = new Point2D(boundsInCanvas.getMinX(), boundsInCanvas.getMinY());



        if(boundsInCanvas.contains(mouseAnchor)) {
            originForScale = mouseAnchor;
        } else {
            //use the closest point on the bounding box to the anchor as the origin
            double originX = mouseAnchor.getX(), originY = mouseAnchor.getY();
            
            if(mouseAnchor.getX() < boundsInCanvas.getMinX()) {
                originX = boundsInCanvas.getMinX();
            } else if (mouseAnchor.getX() > boundsInCanvas.getMaxX()) {
                originX = boundsInCanvas.getMaxX();
            }

            if(mouseAnchor.getY() < boundsInCanvas.getMinY()) {
                originY = boundsInCanvas.getMinY();
            } else if (mouseAnchor.getY() > boundsInCanvas.getMaxY()) {
                originY = boundsInCanvas.getMaxY();
            }

            originForScale = new Point2D(originX, originY);
        }
        Point2D positionInCanvas = localToCanvas(0,0);
        Point2D newTranslation = positionInCanvas.subtract(originForScale).multiply(scale/oldScale).add(originForScale).multiply(1/scale);
        setTranslation(newTranslation);
        this.scale.set(scale);
    }

    public void zoom(double factor) {
        setScale(getScale()*factor);
    }

    public Bounds getLogicalBounds() {
        return logicalBounds.get();
    }

    public ObjectProperty<Bounds> logicalBoundsProperty() {
        return logicalBounds;
    }

    public void setLogicalBounds(Bounds logicalBounds) {
        this.logicalBounds.set(logicalBounds);
    }

    public Bounds getBoundsInLocal() {
        return boundsInLocal.get();
    }

    public ObjectProperty<Bounds> boundsInLocalProperty() {
        return boundsInLocal;
    }

    public void setBoundsInLocal(Bounds boundsInLocal) {
        this.boundsInLocal.set(boundsInLocal);
    }

    public Bounds getBoundsInCanvas() {
        return boundsInCanvas.get();
    }

    public ObjectProperty<Bounds> boundsInCanvasProperty() {
        return boundsInCanvas;
    }

    public void setBoundsInCanvas(Bounds boundsInCanvas) {
        this.boundsInCanvas.set(boundsInCanvas);
    }

    public Bounds getCanvasBounds() {
        return canvasBounds.get();
    }

    public ObjectProperty<Bounds> canvasBoundsProperty() {
        return canvasBounds;
    }

    public void setCanvasBounds(Bounds canvasBounds) {
        this.canvasBounds.set(canvasBounds);
    }

    public double getAutoScale() {
        return autoScale.get();
    }

    public DoubleProperty autoScaleProperty() {
        return autoScale;
    }

    public void setAutoScale(double autoScale) {
        this.autoScale.set(autoScale);
    }

    public double getAutoTranslationX() {
        return autoTranslationX.get();
    }

    public DoubleProperty autoTranslationXProperty() {
        return autoTranslationX;
    }

    public void setAutoTranslationX(double autoTranslationX) {
        this.autoTranslationX.set(autoTranslationX);
    }

    public double getAutoTranslationY() {
        return autoTranslationY.get();
    }

    public DoubleProperty autoTranslationYProperty() {
        return autoTranslationY;
    }

    public void setAutoTranslationY(double autoTranslationY) {
        this.autoTranslationY.set(autoTranslationY);
    }

    public boolean isTransformAutomatically() {
        return transformAutomatically.get();
    }

    public BooleanProperty transformAutomaticallyProperty() {
        return transformAutomatically;
    }

    public void setTransformAutomatically(boolean transformAutomatically) {
        this.transformAutomatically.set(transformAutomatically);
    }
}
