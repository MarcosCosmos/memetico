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
    private ObjectProperty<Bounds> boundsInLocal, canvasBounds;
    private ObservableValue<Bounds> boundsInCanvas;

    //track the targeted "ideal" scale - the largest scale that shows everything
    private DoubleProperty autoScale;
    private DoubleProperty autoTranslationX;
    private DoubleProperty autoTranslationY;

//    private ChangeListener<Bounds> minScaleAdjusterInstance =

    private BooleanProperty transformAutomatically = new SimpleBooleanProperty(false);

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
        boundsInLocal = new SimpleObjectProperty<>(new BoundingBox(0,0,100,100));
        canvasBounds = new SimpleObjectProperty<>(boundsInLocal.get()); //arbitrary default really
        boundsInCanvas = Bindings.createObjectBinding(
                () -> {
                    double scale = getScale();
                    Bounds local = boundsInLocal.get();
                    Point2D min = localToCanvas(local.getMinX(), local.getMinY());
                    return new BoundingBox(min.getX(), min.getY(), local.getWidth()*scale, local.getHeight()*scale);
                },
                boundsInLocal, scale, translationX, translationY
        );

        autoScale.bind(Bindings.createDoubleBinding(
                () -> {
                    Bounds canvasBounds = getCanvasBounds();
                    Bounds boundsInLocal = getBoundsInLocal();
                    return Math.min(canvasBounds.getWidth()/boundsInLocal.getWidth(), canvasBounds.getHeight()/boundsInLocal.getHeight());
                },
                canvasBounds, boundsInLocal
        ));

        autoTranslationX.bind(Bindings.createDoubleBinding(() -> {
                    Bounds canvasBounds = getCanvasBounds();
                    Bounds boundsInLocal = getBoundsInLocal();
                    double scale = getScale();
                    return ((canvasBounds.getWidth() - boundsInLocal.getWidth()*scale) / 2) / scale;
                },
                canvasBounds, boundsInLocal, scale
        ));
        autoTranslationY.bind(Bindings.createDoubleBinding(() -> {
                    Bounds canvasBounds = getCanvasBounds();
                    Bounds boundsInLocal = getBoundsInLocal();
                    double scale = getScale();
                    return ((canvasBounds.getHeight() - boundsInLocal.getHeight()*scale) / 2) / scale;
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
        Bounds boundsInCanvas = getBoundsInCanvas();
        Bounds canvasBounds = getCanvasBounds();
        double oldTranslationX = getTranslationX();
        double oldTranslationY = getTranslationY();
        double newTranslationX = oldTranslationX, newTranslationY = oldTranslationY;
        Point2D translationDiff = new Point2D(0, 0);
        Point2D translationInCanvas = localToCanvas(translationDiff);
        double translatedMinX = translationInCanvas.getX();
        double translatedMaxX = translationInCanvas.getX()+boundsInCanvas.getWidth();
        double translatedMinY = translationInCanvas.getY();
        double translatedMaxY = translationInCanvas.getY()+boundsInCanvas.getHeight();
        if(boundsInCanvas.getWidth() <= canvasBounds.getWidth()) {
            newTranslationX = autoTranslationX.get();
        } else {
            if(translatedMaxX < canvasBounds.getMaxX()) {
                newTranslationX  = (canvasBounds.getWidth() - boundsInCanvas.getWidth())/scale.get();
            } else if (translatedMinX > canvasBounds.getMinX()) {
                newTranslationX = 0;
            }
        }
        if(boundsInCanvas.getHeight() <= canvasBounds.getHeight()) {
            newTranslationY = autoTranslationY.get();
        } else {
            if(translatedMaxY < canvasBounds.getMaxY()) {
                newTranslationY = (canvasBounds.getHeight() - boundsInCanvas.getHeight())/scale.get();
            } else if (translatedMinY > canvasBounds.getMinY()) {
                newTranslationY = 0;
            }
        }
        setTranslation(newTranslationX, newTranslationY);
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
        setTransformAutomatically(false);
        scale = Math.max(scale, getAutoScale());
        if(scale == 0) {
            return;
        }
        double oldScale = getScale();
        Point2D originForScale;
        Point2D mouseAnchor = getMouseAnchor();
        Bounds boundsInCanvas = getBoundsInCanvas();
        Point2D positionInCanvas = new Point2D(boundsInCanvas.getMinX(), boundsInCanvas.getMinY());
        if(boundsInCanvas.contains(mouseAnchor)) {
            originForScale = mouseAnchor;
        } else {
            Point2D sizeInCanvas = new Point2D(boundsInCanvas.getWidth(), boundsInCanvas.getHeight());
            originForScale = positionInCanvas.add(sizeInCanvas.multiply(1 / 2.0)); //if the mouse is outside the box, use the center of the box
        }
        Point2D newTranslation = positionInCanvas.subtract(originForScale).multiply(scale/oldScale).add(originForScale).multiply(1/scale);
        setTranslation(newTranslation);
        this.scale.set(scale);

//        (10,10) = x1
//        (20,20) = x2
//        gap = (10, 10)

    }

    public void zoom(double factor) {
        setScale(getScale()*factor);
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
        return boundsInCanvas.getValue();
    }

    public ObservableValue<Bounds> boundsInCanvasProperty() {
        return boundsInCanvas;
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
