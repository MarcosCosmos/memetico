package org.marcos.uon.tspaidemo.canvas;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

public class TransformationContext {

    public static final double DEFAULT_MIN_DECORATION_SCALE = 1, DEFAULT_MAX_DECORATION_SCALE = 5;

    private DoubleProperty mouseAnchorX;
    private DoubleProperty mouseAnchorY;

    private DoubleProperty translationX;
    private DoubleProperty translationY;
    private DoubleProperty scale;
    private ObjectProperty<Bounds> logicalBounds, boundsInLocal, canvasBounds;

    private ReadOnlyObjectWrapper<Bounds> boundsInCanvas;

    private ReadOnlyObjectWrapper<Point2D> decorationPaddingInLocal;
    private ReadOnlyDoubleWrapper decorationScale;

    //track the targeted "ideal" scale - the largest scale that shows everything
    private BooleanProperty transformAutomatically;
    private ReadOnlyDoubleWrapper autoScale;
    private ReadOnlyObjectWrapper<Point2D> autoTranslation;
    private ReadOnlyDoubleWrapper autoTranslationX;
    private ReadOnlyDoubleWrapper autoTranslationY;

    private DoubleProperty minScale;
    private DoubleProperty maxScale;

    private DoubleProperty minDecorationScale;
    private DoubleProperty maxDecorationScale;


    //setup the permanent/read-only bindings and irremovable listeners
    private void setup() {
        decorationScale.bind(
                Bindings.createDoubleBinding(
                        () -> computeDecorationScale(scale.get()),
                        scale, minDecorationScale, maxDecorationScale
                )
        );

        decorationPaddingInLocal.bind(
                Bindings.createObjectBinding(
                        this::computeDecorationPaddingInLocal,
                        logicalBounds, boundsInLocal, decorationScale
                )
        );


        boundsInCanvas.bind(
                Bindings.createObjectBinding(
                        this::computeBoundsInCanvas,
                        boundsInLocal, scale, translationX, translationY
                )
        );

        autoScale.bind(
                Bindings.createDoubleBinding(
                        this::computeAutoScale,
                        canvasBounds, boundsInLocal
                )
        );

        autoTranslation.bind(
                Bindings.createObjectBinding(
                        this::computeAutoTranslation,
                        logicalBounds, boundsInLocal, decorationScale, canvasBounds, scale
                )
        );

        autoTranslationX.bind(Bindings.createDoubleBinding(() -> autoTranslation.get().getX(), autoTranslation));
        autoTranslationY.bind(Bindings.createDoubleBinding(() -> autoTranslation.get().getY(), autoTranslation));

        transformAutomatically.addListener(
                (observable, oldValue, newValue) -> {
                    if(newValue && !oldValue) {
                        scale.bind(autoScale);
                        translationX.bind(autoTranslationX);
                        translationY.bind(autoTranslationY);
                    } else if(!newValue) {
                        scale.unbind();
                        translationX.unbind();
                        translationY.unbind();
                    }
                }
        );
    }

    //BINDING COMPUTATION

    public Point2D computeDecorationPaddingInLocal() {
        Bounds logicalBounds = getLogicalBounds();
        Bounds boundsInLocal = getBoundsInLocal();
        return new Point2D((boundsInLocal.getWidth() - logicalBounds.getWidth())/2.0, (boundsInLocal.getHeight() - logicalBounds.getHeight())/2.0);
    }

    public Bounds computeBoundsInCanvas() {
        Bounds logicalBounds = getLogicalBounds();
        Point2D decorationPaddingInCanvas = getDecorationPaddingInLocal().multiply(getDecorationScale());
        Point2D minInCanvas = localToCanvas(logicalBounds.getMinX(), logicalBounds.getMinY()).subtract(decorationPaddingInCanvas);
        Point2D sizeInCanvas = localToCanvas(logicalBounds.getMaxX(), logicalBounds.getMaxY()).add(decorationPaddingInCanvas).subtract(minInCanvas);
        return new BoundingBox(minInCanvas.getX(), minInCanvas.getY(), sizeInCanvas.getX(), sizeInCanvas.getY());
    }

    public double computeAutoScale() {
        Bounds canvasBounds = getCanvasBounds();
        Bounds boundsInLocal = getBoundsInLocal();
        double naiveScale = Math.min(canvasBounds.getWidth()/boundsInLocal.getWidth(), canvasBounds.getHeight()/boundsInLocal.getHeight());
        double decorationScale = computeDecorationScale(naiveScale);
        //if we are trying to scale outside of the normal decoration scale range, we will need to factor in the clamped decoration padding
        if(naiveScale == decorationScale) {
            return naiveScale;
        } else {
            Bounds logicalBounds = getLogicalBounds();
            Point2D totalDecorationPaddingInCanvas = getDecorationPaddingInLocal().multiply(2*decorationScale);
            return Math.min((canvasBounds.getWidth()-totalDecorationPaddingInCanvas.getX())/logicalBounds.getWidth(), (canvasBounds.getHeight()-totalDecorationPaddingInCanvas.getY())/logicalBounds.getHeight());
        }
    }

    public Point2D computeAutoTranslation() {
        double scale = getScale();
        double decorationScale = getDecorationScale();
        Bounds logicalBounds = getLogicalBounds();
        Bounds boundsInLocal = getBoundsInLocal();
        Bounds canvasBounds = getCanvasBounds();

        Point2D totalDecorationPaddingInCanvas = getDecorationPaddingInLocal().multiply(2*decorationScale);
        Point2D sizeInCanvas = new Point2D(logicalBounds.getWidth(), logicalBounds.getHeight())
                .multiply(scale)
                .add(totalDecorationPaddingInCanvas);
        Point2D canvasMin = new Point2D(canvasBounds.getMinX(), canvasBounds.getMinY());
        Point2D canvasSize = new Point2D(canvasBounds.getWidth(), canvasBounds.getHeight());

        return canvasMin.add(
                canvasSize.subtract(sizeInCanvas)
                        .multiply(0.5)
        )
                .multiply(1/scale)
                .subtract(boundsInLocal.getMinX(), boundsInLocal.getMinY());

    }

    //OTHER FUNCTIONS

    public void considerAutoTransform() {
        Bounds boundsInCanvas = getBoundsInCanvas();
        Bounds canvasBounds = getCanvasBounds();
        if(boundsInCanvas.getWidth() <= canvasBounds.getWidth() && boundsInCanvas.getHeight() <= canvasBounds.getHeight()) {
            setTransformAutomatically(true);
        }
    }

    public double computeDecorationScale(double requestedScale) {
        double minDecorationScale = getMinDecorationScale();
        double maxDecorationScale = getMaxDecorationScale();
        if(requestedScale < minDecorationScale) {
            return minDecorationScale;
        } else if (requestedScale > maxDecorationScale) {
            return maxDecorationScale;
        } else {
            return requestedScale;
        }
    }

    //OTHER FUNCTIONS

    /**
     * Computes the radius used when drawing the vertex
     * @param preferredRadius
     */
    public double computeRadiusToUse(double preferredRadius) {
        return preferredRadius*getDecorationScale();
    }

    /**
     * Computes the line width used when drawing the vertex
     * @param preferredWidth
     */
    public double computeStrokeWidthToUse(double preferredWidth) {
        return preferredWidth*getDecorationScale();
    }

    /**
     * Computes the line width used when drawing the vertex
     * @param preferredWidth
     */
    public double computeLineWidthToUse(double preferredWidth) {
        return  preferredWidth*getDecorationScale();
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

    public void setTranslationX(double translationX) {
        setTransformAutomatically(false);
        this.translationX.set(translationX);
    }

    public void setTranslationY(double translationY) {
        setTransformAutomatically(false);
        this.translationY.set(translationY);
    }

    //prevents the translation from leaving components unnecessarily out of bounds
    public void santiseTranslation() {
        considerAutoTransform();
        if (isTransformAutomatically()) {
            return;
        }
        Bounds canvasBounds = getCanvasBounds();
        Bounds boundsInCanvas = getBoundsInCanvas();
        double newTranslationX = getTranslationX(), newTranslationY = getTranslationY();
        double scale = getScale();
        if(boundsInCanvas.getMaxX() < canvasBounds.getMaxX()) {
            newTranslationX = ((canvasBounds.getMinX()+(canvasBounds.getWidth()-boundsInCanvas.getWidth()))/scale) - getBoundsInLocal().getMinX();
        } else if (boundsInCanvas.getMinX() > canvasBounds.getMinX()) {
            newTranslationX = (canvasBounds.getMinX()/scale) - getBoundsInLocal().getMinX();
        }
        if(boundsInCanvas.getMaxY() < canvasBounds.getMaxY()) {
            newTranslationY = ((canvasBounds.getMinY()+(canvasBounds.getHeight()-boundsInCanvas.getHeight()))/scale) - getBoundsInLocal().getMinY();
        } else if (boundsInCanvas.getMinY() > canvasBounds.getMinY()) {
            newTranslationY = (canvasBounds.getMinY()/scale) - getBoundsInLocal().getMinY();
        }
        setTranslation(newTranslationX, newTranslationY);
    }


    public void translate(double x, double y) {
        setTranslation(getTranslationX() + x, getTranslationY() + y);
    }

    public void setScale(double scale) {
        scale = Math.max(minScale.get(), Math.min(maxScale.get(), scale));
        setTransformAutomatically(false);
        double oldScale = getScale();
        Point2D originForScale;
        Point2D mouseAnchor = getMouseAnchor();
        Bounds boundsInCanvas = getBoundsInCanvas();

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

    //the remainder of these are exempt from testing (though content they call may not be)

    public TransformationContext(double mouseAnchorX, double mouseAnchorY, double translationX, double translationY, double scale, Bounds logicalBounds, Bounds boundsInLocal, Bounds canvasBounds, double minScale, double maxScale, double minDecorationScale, double maxDecorationScale, boolean transformAutomatically) {
        this.mouseAnchorX = new SimpleDoubleProperty(mouseAnchorX);
        this.mouseAnchorY = new SimpleDoubleProperty(mouseAnchorY);

        this.translationX = new SimpleDoubleProperty(translationX);
        this.translationY = new SimpleDoubleProperty(translationY);
        this.scale = new SimpleDoubleProperty(scale);

        this.logicalBounds = new SimpleObjectProperty<>(logicalBounds);
        this.boundsInLocal = new SimpleObjectProperty<>(boundsInLocal);
        this.canvasBounds = new SimpleObjectProperty<>(canvasBounds);

        this.minScale = new SimpleDoubleProperty(minScale);
        this.maxScale = new SimpleDoubleProperty(maxScale);

        this.minDecorationScale = new SimpleDoubleProperty(minDecorationScale);
        this.maxDecorationScale = new SimpleDoubleProperty(maxDecorationScale);

        //properties that should only ever be computed

        boundsInCanvas = new ReadOnlyObjectWrapper<>();

        decorationPaddingInLocal = new ReadOnlyObjectWrapper<>();
        decorationScale = new ReadOnlyDoubleWrapper();

        this.transformAutomatically = new SimpleBooleanProperty(false);
        autoScale = new ReadOnlyDoubleWrapper();
        autoTranslation = new ReadOnlyObjectWrapper<>();
        autoTranslationX = new ReadOnlyDoubleWrapper();
        autoTranslationY = new ReadOnlyDoubleWrapper();

        setup();

        setTransformAutomatically(transformAutomatically);
    }



    public TransformationContext() {
        this(0, 0, 0, 0, 1, new BoundingBox(0,0,100,100), new BoundingBox(0,0,100,100), new BoundingBox(0,0,100,100), 1, 1_000_000, DEFAULT_MIN_DECORATION_SCALE, DEFAULT_MAX_DECORATION_SCALE, true);

        minScale.bind(autoScale);

        maxScale.bind(
                Bindings.createDoubleBinding(
                        () -> {
                            Bounds logicalBounds = getLogicalBounds();
                            return 10*Math.max(logicalBounds.getWidth(), logicalBounds.getHeight());
                        },
                        logicalBounds
                )
        );
    }

    public TransformationContext(TransformationContext source) {
        this(source.mouseAnchorX.get(), source.mouseAnchorY.get(), source.translationX.get(), source.translationY.get(), source.scale.get(), source.logicalBounds.get(), source.boundsInLocal.get(), source.canvasBounds.get(), source.minScale.get(), source.maxScale.get(), source.minDecorationScale.get(), source.maxDecorationScale.get(), source.transformAutomatically.get());
    }

    public void setState(TransformationContext source) {
        transformAutomatically.set(false);
        setMouseAnchor(source.getMouseAnchor());
        setTranslation(source.getTranslation());
        setMinScale(source.getMinScale());
        setMaxScale(source.getMaxScale());
        setMinDecorationScale(source.getMinDecorationScale());
        setMaxDecorationScale(source.getMaxDecorationScale());
        scale.set(source.getScale());
        setLogicalBounds(source.getLogicalBounds());
        setBoundsInLocal(source.getBoundsInLocal());
        setCanvasBounds(source.getCanvasBounds());

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

    public Bounds getLogicalBounds() {
        return logicalBounds.get();
    }

    public ObjectProperty<Bounds> logicalBoundsProperty() {
        return logicalBounds;
    }

    public void setLogicalBounds(Bounds logicalBounds) {
        if (this.logicalBounds.isBound()) {
            this.logicalBounds.unbind();
        }
        this.logicalBounds.set(logicalBounds);
    }

    public Bounds getBoundsInLocal() {
        return boundsInLocal.get();
    }

    public ObjectProperty<Bounds> boundsInLocalProperty() {
        return boundsInLocal;
    }

    public void setBoundsInLocal(Bounds boundsInLocal) {
        if (this.boundsInLocal.isBound()) {
            this.boundsInLocal.unbind();
        }
        this.boundsInLocal.set(boundsInLocal);
    }

    public double getMinScale() {
        return minScale.get();
    }

    public DoubleProperty minScaleProperty() {
        return minScale;
    }

    public void setMinScale(double minScale) {
        if(this.minScale.isBound()) {
            this.minScale.unbind();
        }
        this.minScale.set(minScale);
    }

    public double getMaxScale() {
        return maxScale.get();
    }

    public DoubleProperty maxScaleProperty() {
        return maxScale;
    }

    public void setMaxScale(double maxScale) {
        if(this.maxScale.isBound()) {
            this.maxScale.unbind();
        }
        this.maxScale.set(maxScale);
    }

    public double getMinDecorationScale() {
        return minDecorationScale.get();
    }

    public DoubleProperty minDecorationScaleProperty() {
        return minDecorationScale;
    }

    public void setMinDecorationScale(double minDecorationScale) {
        this.minDecorationScale.set(minDecorationScale);
    }

    public double getMaxDecorationScale() {
        return maxDecorationScale.get();
    }

    public DoubleProperty maxDecorationScaleProperty() {
        return maxDecorationScale;
    }

    public void setMaxDecorationScale(double maxDecorationScale) {
        this.maxDecorationScale.set(maxDecorationScale);
    }

    public Bounds getBoundsInCanvas() {
        return boundsInCanvas.get();
    }

    public ReadOnlyObjectProperty<Bounds> boundsInCanvasProperty() {
        return boundsInCanvas.getReadOnlyProperty();
    }

    public Bounds getCanvasBounds() {
        return canvasBounds.get();
    }

    public ObjectProperty<Bounds> canvasBoundsProperty() {
        return canvasBounds;
    }

    public void setCanvasBounds(Bounds canvasBounds) {
        if (this.canvasBounds.isBound()) {
            this.canvasBounds.unbind();
        }
        this.canvasBounds.set(canvasBounds);
    }

    public double getAutoScale() {
        return autoScale.get();
    }

    public ReadOnlyDoubleProperty autoScaleProperty() {
        return autoScale.getReadOnlyProperty();
    }

    public double getAutoTranslationX() {
        return autoTranslation.get().getX();
    }

    public ReadOnlyDoubleProperty autoTranslationXProperty() {
        return autoTranslationY.getReadOnlyProperty();
    }

    public double getAutoTranslationY() {
        return autoTranslation.get().getY();
    }

    public ReadOnlyDoubleProperty autoTranslationYProperty() {
        return autoTranslationY.getReadOnlyProperty();
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

    public Point2D getDecorationPaddingInLocal() {
        return decorationPaddingInLocal.get();
    }

    public ReadOnlyObjectProperty<Point2D> decorationPaddingInLocalProperty() {
        return decorationPaddingInLocal.getReadOnlyProperty();
    }

    public double getDecorationScale() {
        return decorationScale.get();
    }

    public ReadOnlyDoubleProperty decorationScaleProperty() {
        return decorationScale.getReadOnlyProperty();
    }
}
