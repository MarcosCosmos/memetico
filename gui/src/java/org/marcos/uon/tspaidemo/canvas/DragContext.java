package org.marcos.uon.tspaidemo.canvas;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

public class DragContext {

    private DoubleProperty mouseAnchorX;
    private DoubleProperty mouseAnchorY;

    private DoubleProperty translateAnchorX;
    private DoubleProperty translateAnchorY;
    private DoubleProperty scale;
    private ObjectProperty<Bounds> logicalBoundsRaw, viewportBounds;
    private ObservableValue<Bounds> logicalBoundsScaled;

    public DragContext() {
        mouseAnchorX = new SimpleDoubleProperty(0);
        mouseAnchorY = new SimpleDoubleProperty(0);
        translateAnchorX = new SimpleDoubleProperty(0);
        translateAnchorY = new SimpleDoubleProperty(0);
        scale = new SimpleDoubleProperty(1);
        logicalBoundsRaw = new SimpleObjectProperty<>(new BoundingBox(0,0,100,100)); //arbitrary default really
        logicalBoundsScaled = Bindings.createObjectBinding(
                () -> {
                    double scale = getScale();
                    Bounds src = logicalBoundsRaw.get();
                    return new BoundingBox(src.getMinX()*scale, src.getMinY()*scale, src.getWidth()*scale, src.getHeight()*scale);
                },
                logicalBoundsRaw, scale
        );
        viewportBounds = new SimpleObjectProperty<>(logicalBoundsRaw.get());
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

    public double getTranslateAnchorX() {
        return translateAnchorX.get();
    }

    /**
     * Note that it is almost always better to use the setters or {@code #translate(int x, int y)} then to set via properties, as this will ensure that objects don't move entirely off-screen
     * @see #setTranslateAnchorX(double)
     * @see #translate(double, double)
     * @return
     */
    public DoubleProperty translateAnchorXProperty() {
        return translateAnchorX;
    }

    public void setTranslateAnchorX(double translateAnchorX) {
        Bounds logicalBounds = getLogicalBoundsScaled();
        Bounds viewportBounds = getViewportBounds();
        double scale=getScale();
        double scaledTranslation = translateAnchorX*scale;
        double translatedMin = logicalBounds.getMinX()+scaledTranslation;
        double translatedMax = logicalBounds.getMaxX()+scaledTranslation;
        if(logicalBounds.getWidth() <= viewportBounds.getWidth()) {
            if (translatedMin < viewportBounds.getMinX() && translatedMax < viewportBounds.getMaxX()) {
                translateAnchorX = 0;
            } else if (translatedMax > viewportBounds.getMaxX() && translatedMin > viewportBounds.getMinX()) {
                translateAnchorX = (viewportBounds.getWidth() - logicalBounds.getWidth())/scale;
            }
        } else {
            if(translatedMax < viewportBounds.getMaxX()) {
                translateAnchorX = (viewportBounds.getWidth()-logicalBounds.getWidth())/scale;
            } else if (translatedMin > viewportBounds.getMinX()) {
                translateAnchorX = 0;
            }
        }
        this.translateAnchorX.set(translateAnchorX);
    }

    public void translate(double x, double y) {
        setTranslateAnchorX(getTranslateAnchorX() + x);
        setTranslateAnchorY(getTranslateAnchorY() + y);
    }

    public double getTranslateAnchorY() {
        return translateAnchorY.get();
    }

    /**
     * Note that it is almost always better to use the setters or {@code #translate(int x, int y)} then to set via properties, as this will ensure that objects don't move entirely off-screen
     * @see #setTranslateAnchorY(double)
     * @see #translate(double, double)
     * @return
     */
    public DoubleProperty translateAnchorYProperty() {
        return translateAnchorY;
    }

    public void setTranslateAnchorY(double translateAnchorY) {
        Bounds logicalBounds = getLogicalBoundsScaled();
        Bounds viewportBounds = getViewportBounds();
        double scale=getScale();
        double scaledTranslation = translateAnchorY*scale;
        double translatedMin = logicalBounds.getMinY()+scaledTranslation;
        double translatedMax = logicalBounds.getMaxY()+scaledTranslation;
        if(logicalBounds.getHeight() <= viewportBounds.getHeight()) {
            if (translatedMin < viewportBounds.getMinY() && translatedMax < viewportBounds.getMaxY()) {
                translateAnchorY = 0;
            } else if (translatedMax > viewportBounds.getMaxY() && translatedMin > viewportBounds.getMinY()) {
                translateAnchorY = (viewportBounds.getHeight() - logicalBounds.getHeight())/scale;
            }
        } else {
            if(translatedMax < viewportBounds.getMaxY() && translatedMin <= viewportBounds.getMinY()) {
                translateAnchorY = (viewportBounds.getHeight()-logicalBounds.getHeight())/scale;
            } else if (translatedMin > viewportBounds.getMinY() && translatedMax >= viewportBounds.getMaxY()) {
                translateAnchorY = 0;
            }
        }
        this.translateAnchorY.set(translateAnchorY);
    }



    public double getScale() {
        return scale.get();
    }

    public DoubleProperty scaleProperty() {
        return scale;
    }

    public void setScale(double scale) {
        if(scale == 0) {
            return;
        }
        double oldScale = this.scale.get();
        Bounds scaledBounds = getLogicalBoundsScaled();
        Bounds bounds = getLogicalBoundsRaw();
        Bounds viewportBounds = getViewportBounds();
        Point2D oldTranslationInLocal = new Point2D(getTranslateAnchorX(), getTranslateAnchorY());
        Point2D oldTranslationInParent = oldTranslationInLocal.multiply(oldScale);
        Point2D positionInLocal = new Point2D(bounds.getMinX(), bounds.getMinY());
        Point2D positionInParent = positionInLocal.add(oldTranslationInLocal).multiply(scale);
        Point2D viewCenterInParent = new Point2D(getMouseAnchorX(), getMouseAnchorY()).subtract(positionInParent);
        Point2D viewCenterInLocal = viewCenterInParent.multiply(1/oldScale);
//        apply the translation fixers
        Point2D resultTranslation = oldTranslationInLocal.subtract(viewCenterInLocal)
                .multiply(scale/oldScale)
                .add(viewCenterInLocal)
                .multiply(oldScale/scale);
        this.scale.set(scale);
        setTranslateAnchorX(resultTranslation.getX());
        setTranslateAnchorY(resultTranslation.getY());
//        setTranslateAnchorX(getTranslateAnchorX());
//        setTranslateAnchorY(getTranslateAnchorY());
    }

    public void zoom(double factor) {
        setScale(getScale()*factor);
    }

    public Bounds getLogicalBoundsRaw() {
        return logicalBoundsRaw.get();
    }

    public ObjectProperty<Bounds> logicalBoundsRawProperty() {
        return logicalBoundsRaw;
    }

    public void setLogicalBoundsRaw(Bounds logicalBoundsRaw) {
        this.logicalBoundsRaw.set(logicalBoundsRaw);
    }


    public Bounds getLogicalBoundsScaled() {
        return logicalBoundsScaled.getValue();
    }

    public ObservableValue<Bounds> logicalBoundsScaledProperty() {
        return logicalBoundsScaled;
    }

    public Bounds getViewportBounds() {
        return viewportBounds.get();
    }

    public ObjectProperty<Bounds> viewportBoundsProperty() {
        return viewportBounds;
    }

    public void setViewportBounds(Bounds viewportBounds) {
        this.viewportBounds.set(viewportBounds);
    }
}
