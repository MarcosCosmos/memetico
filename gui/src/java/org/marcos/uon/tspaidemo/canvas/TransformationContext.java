package org.marcos.uon.tspaidemo.canvas;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;

/**
 * A utility class which manages the calculation and sanitation of translation and and scaling transformations which allow the user to perform panning and zooming, respectively, on a {@link LayeredCanvas}
 * <b>Computed Properties & Dependencies:</b> Some properties are bound to functions, rather than being set to individual values. These properties are dependant on other properties, and their values are recomputed whenever one of their dependencies change. This ensures that whenever they are accessed, the value will reflect their intended logic, whilst eliminating redundant recalculation, and associated boilerplate.
 * <br/>
 * <b>Neutral Values:</b> note that some properties are documented to have neutral values. Where applicable, this means the value should have no effect. In other words, result of a mathematical operation using the value (in the usage documented for that property) should have no effect, and be equivalent to simply omitting the operation.
 */
public class TransformationContext {

    public static final double DEFAULT_MIN_DECORATION_SCALE = 1, DEFAULT_MAX_DECORATION_SCALE = 5;

    /**
     * The x-coordinate of the position of the mouse when the most recent gesture was triggered.
     * <br/>
     * Used to control the point about which scaling is performed.
     * @see #mouseAnchorY
     * @see #setScale(double)
     */
    private DoubleProperty mouseAnchorX;

    /**
     * The y-coordinate of the position of the mouse when the most recent gesture was triggered.
     * <br/>
     * Used to control the point about which scaling is performed.
     * @see #mouseAnchorY
     * @see #setScale(double)
     */
    private DoubleProperty mouseAnchorY;

    /**
     * The amount by which to translate coordinates (with respect to the x axis) when transforming from local space to canvas space.
     * <br/>
     * May be bound to to {@link #autoTranslationX} during calls to {@link #considerAutoTransform()} (under the conditions detailed in that method)
     * <br/>
     * <b>Neutral value</b>: 0.0; Neutral when used in scalar addition and as an x coordinate in vector addition
     * @see #localToCanvas(Point2D)
     * @see #localToCanvas(double, double)
     */
    private DoubleProperty translationX;

    /**
     * The amount by which to translate coordinates (with respect to the y axis) when transforming from local space to canvas space.
     * <br/>
     * May be bound to to {@link #autoTranslationY} during calls to {@link #considerAutoTransform()} (under the conditions detailed in that method)
     * <br/>
     * <b>Neutral value</b>: 0.0; Neutral when used in scalar addition and as a y coordinate in vector addition
     * @see #localToCanvas(Point2D)
     * @see #localToCanvas(double, double)
     */
    private DoubleProperty translationY;

    /**
     * The factor by which to scale coordinates when transforming from local space to canvas space.
     * <br/>
     * The value is always between {@link #minScale} and {@link #maxScale}, inclusive.
     * <br/>
     * Note that this property may not always be equal to {@link #decorationScale}, as the two properties have independent minimums and maximum
     * <br/>
     * May be bound to to {@link #autoScale} by calls to {@link #considerAutoTransform()} (under the conditions detailed in that method)
     * <br/>
     * <b>Neutral value</b>: 1.0; Neutral when used in both scalar-scalar and vector-scalar multiplication
     * @see #minScale
     * @see #maxScale
     * @see #decorationScale
     * @see #localToCanvas(Point2D)
     * @see #localToCanvas(double, double)
     * @see #considerAutoTransform()
     */
    private DoubleProperty scale;

    /**
     * The boundaries of the contents of a canvas in logical-space (that is, local space, but without any decoration)
     * @see #boundsInLocal
     * @see #decorationPaddingInLocal
     */
    private ObjectProperty<Bounds> logicalBounds;

    /**
     * The boundaries of the contents of a canvas in local-space.
     * <br/>
     * That is, the boundaries including decoration when {@link #translationX}, {@link #translationY}, {@link #scale}, and {@link #decorationScale} all have neutral values)
     * <br/>
     * {@link #boundsInCanvas} should be equivalent to this when the aforementioned properties are neutral.
     * @see #boundsInCanvas
     */
    private ObjectProperty<Bounds> boundsInLocal;

    /**
     * The boundaries of the canvas in which content should be drawn.
     * <br/>
     * Used along with {@link #logicalBounds} and {@link #decorationPaddingInLocal} to compute automatic transformations.
     * @see #logicalBounds
     * @see #decorationPaddingInLocal
     * @see #autoScale
     * @see #autoTranslation
     */
    private ObjectProperty<Bounds> canvasBounds;

    /**
     * The boundaries of the contents of the canvas in canvas space.
     * <br/>
     * The resulting bounds reflect the bounding box of content after applying {@link #localToCanvas(Point2D)} to the location of content elements, and applying {@link #decorationScale} to their decorations.
     * <br/>
     * This should correspond to the boundaries of the content when it is actually drawn on the canvas
     * <br/>
     * <b>Computed:</b> via {@link #computeBoundsInCanvas()}
     * <br/>
     * <b>Dependencies:</b> {@link #logicalBounds}, {@link #boundsInLocal}, {@link #scale}
     * @see #logicalBounds
     * @see #boundsInLocal
     * @see #scale
     * @see #decorationScale
     * @see #computeBoundsInCanvas()
     * @see #localToCanvas(Point2D)
     * @see #localToCanvas(double, double)
     */
    private ReadOnlyObjectWrapper<Bounds> boundsInCanvas;

    /**
     * The amount of padding space required around {@link #logicalBounds} to cater for/fit the content decoration.
     * <br/>
     * E.g. a circle drawn to identify a vertex, a line between vertices.
     * <br/>
     * The x-coordinate specifies the left padding and the right padding whilst the y-coordinate specifies the top and bottom padding
     * <br/>
     * <b>Computed:</b> via {@link #computeDecorationPaddingInLocal()}
     * <br/>
     * <b>Dependencies:</b> {@link #logicalBounds}, {@link #boundsInLocal}, {@link #scale}
     * @see #logicalBounds
     * @see #boundsInLocal
     * @see #scale
     * @see #computeDecorationPaddingInLocal()
     */
    private ReadOnlyObjectWrapper<Insets> decorationPaddingInLocal;

    private ReadOnlyObjectWrapper<Insets> decorationPaddingInCanvas;

    /**
     * The scale to apply to content decorations (e.g. a circle drawn to identify a vertex, a line between vertices).
     * <br/>
     * The value is computed by clamping {@link #scale} to be between {@link #minDecorationScale} and {@link #maxDecorationScale}.
     * <br/>
     * Note that this property may not always be equal to {@link #scale} as the two properties have independent minimums and maximum
     * <br/>
     * <b>Computed:</b> via {@link #estimateDecorationScale(double)} (with {@link #scale} as the {@code requestedScale} parameter)
     * <br/>
     * <b>Dependencies:</b> {@link #scale}, {@link #minDecorationScale}, {@link #maxDecorationScale}
     * @see #minDecorationScale
     * @see #maxDecorationScale
     * @see #scale
     * @see #estimateDecorationScale(double)
     * @see #computeRadiusToUse(double)
     * @see #computeStrokeWidthToUse(double)
     * @see #computeLineWidthToUse(double)
     */
    private ReadOnlyDoubleWrapper decorationScale;

    /**
     * A flag identifying whether or not transformations should be applied automatically.
     * <br/>
     * Whenever this property is true, {@link #scale} is be bound to {@link #autoScale}, {@link #translationX} is bound to {@link #autoTranslationX}, and {@link #translationX} is bound to {@link #autoTranslationY}.
     * <br/>
     * Whenever this property is not true, none of the aforementioned bindings should be in effect.
     * @see #scale
     * @see #translationX
     * @see #translationY
     * @see #autoScale
     * @see #autoTranslation
     * @see #autoTranslationX
     * @see #autoTranslationY
     */
    private BooleanProperty transformAutomatically;

    /**
     * An automatically calculated scale which should always match the maximum scale whereby the content fits entirely within the canvas.
     * <br/>
     * <b>Computed:</b> via {@link #computeAutoScale()}
     * <br/>
     * <b>Dependencies:</b> {@link #boundsInLocal}, {@link #canvasBounds}
     * @see #boundsInLocal
     * @see #canvasBounds
     * @see #computeAutoScale()
     */
    private ReadOnlyDoubleWrapper autoScale;

    /**
     * Automatically computed translation coordinates which center the content within the canvas (based on it's bounds)
     * <br/>
     * <b>Computed:</b> via {@link #computeAutoTranslation()}
     * <br/>
     * <b>Dependencies:</b> {@link #logicalBounds}, {@link #boundsInLocal}, {@link #canvasBounds}, {@link #scale}
     *
     */
    private ReadOnlyObjectWrapper<Point2D> autoTranslation;

    /**
     * Bound to always reflect the x coordinate of {@link #autoTranslation}
     * @see #autoTranslation
     */
    private ReadOnlyDoubleWrapper autoTranslationX;

    /**
     * Bound to always reflect the y coordinate of {@link #autoTranslation}
     * @see #autoTranslation
     */
    private ReadOnlyDoubleWrapper autoTranslationY;

    /**
     * The minimum allowed {@link #scale}
     * <br/>
     * Having a defined minimum and maximum scale ensures that {@link #zoom(double)} always has some effect (rather than, for example, the user being stuck with an invisible and irrecoverable graph as a result of a zero scale)
     * @see #scale
     * @see #maxScale
     */
    private DoubleProperty minScale;

    /**
     * The maximum allowed {@link #scale}
     * <br/>
     * Having a defined minimum and maximum {@link #scale} ensures that {@link #zoom(double)} always has some effect (rather than, for example, the user being stuck with an invisible and irrecoverable graph as a result of a zero scale)
     * @see #scale
     * @see #minScale
     */
    private DoubleProperty maxScale;

    /**
     * The minimum allowed {@link #decorationScale}
     * <br/>
     * Having a defined minimum and maximum {@link #decorationScale} ensures that decorations (and therefore content elements) are always visible, at any valid scale, and that there exists some scale large enough such that space between any two distinct elements can be seen (within the limitations of floating-point calculations)
     * @see #decorationScale
     * @see #minDecorationScale
     */
    private DoubleProperty minDecorationScale;

    /**
     * The maximum allowed {@link #decorationScale}
     * <br/>
     * Having a defined minimum and maximum {@link #decorationScale} ensures that decorations (and therefore content elements) are always visible, at any valid scale, and that there exists some scale large enough such that space between any two distinct elements can be seen (within the limitations of floating-point calculations)
     * @see #decorationScale
     * @see #minDecorationScale
     */
    private DoubleProperty maxDecorationScale;


    //setup the permanent/read-only bindings and irremovable listeners


    /**
     * Assigns bindings to <b>computed properties</b>.
     * @see TransformationContext
     */
    private void setup() {
        decorationScale.bind(
                Bindings.createDoubleBinding(
                        () -> estimateDecorationScale(scale.get()),
                        scale, minDecorationScale, maxDecorationScale
                )
        );

        decorationPaddingInLocal.bind(
                Bindings.createObjectBinding(
                        this::computeDecorationPaddingInLocal,
                        logicalBounds, boundsInLocal, scale
                )
        );

        decorationPaddingInCanvas.bind(
                Bindings.createObjectBinding(
                        () -> estimateDecorationPadding(getDecorationScale()),
                        decorationPaddingInLocal, decorationScale
                )
        );


        boundsInCanvas.bind(
                Bindings.createObjectBinding(
                        this::computeBoundsInCanvas,
                        logicalBounds, boundsInLocal, scale, translationX, translationY
                )
        );

        autoScale.bind(
                Bindings.createDoubleBinding(
                        this::computeAutoScale,
                         boundsInLocal, canvasBounds
                )
        );

        autoTranslation.bind(
                Bindings.createObjectBinding(
                        this::computeAutoTranslation,
                        logicalBounds, boundsInLocal, canvasBounds, scale
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

    /**
     * Computes an updated value for {@link #decorationPaddingInLocal}
     * @return an updated value for {@link #decorationPaddingInLocal}
     * @see #decorationPaddingInLocal
     */
    public Insets computeDecorationPaddingInLocal() {
        Bounds logicalBounds = getLogicalBounds();
        Bounds boundsInLocal = getBoundsInLocal();
        return new Insets(logicalBounds.getMinY() - boundsInLocal.getMinY(), boundsInLocal.getMaxX() - logicalBounds.getMaxX(), boundsInLocal.getMaxY() - logicalBounds.getMaxY(), logicalBounds.getMinX() - boundsInLocal.getMinX());
    }

    public Insets estimateDecorationPadding(double decorationScale) {
        Insets decorationPaddingInLocal = getDecorationPaddingInLocal();
        return new Insets(decorationPaddingInLocal.getTop() * decorationScale, decorationPaddingInLocal.getRight() * decorationScale, decorationPaddingInLocal.getBottom() * decorationScale, decorationPaddingInLocal.getLeft() * decorationScale);
    }

    /**
     * Computes an updated value for {@link #boundsInCanvas}
     * @return an updated value for {@link #boundsInCanvas}
     * @see #boundsInCanvas
     */
    public Bounds computeBoundsInCanvas() {
        Bounds logicalBounds = getLogicalBounds();
        Insets decorationPaddingInCanvas = getDecorationPaddingInCanvas();
        Point2D minInCanvas = localToCanvas(logicalBounds.getMinX(), logicalBounds.getMinY()).subtract(decorationPaddingInCanvas.getLeft(), decorationPaddingInCanvas.getTop());
        Point2D sizeInCanvas = localToCanvas(logicalBounds.getMaxX(), logicalBounds.getMaxY()).add(decorationPaddingInCanvas.getRight(), decorationPaddingInCanvas.getBottom()).subtract(minInCanvas);
        return new BoundingBox(minInCanvas.getX(), minInCanvas.getY(), sizeInCanvas.getX(), sizeInCanvas.getY());
    }

    /**
     * Computes an updated value for {@link #autoScale}
     * @return an updated value for {@link #autoScale}
     * @see #autoScale
     */
    public double computeAutoScale() {
        Bounds canvasBounds = getCanvasBounds();
        Bounds boundsInLocal = getBoundsInLocal();
        double naiveScale = Math.min(canvasBounds.getWidth()/boundsInLocal.getWidth(), canvasBounds.getHeight()/boundsInLocal.getHeight());
        double decorationScale = estimateDecorationScale(naiveScale);
        //if we are trying to scale outside of the normal decoration scale range, we will need to factor in the clamped decoration padding
        if(naiveScale == decorationScale) {
            return naiveScale;
        } else {
            Bounds logicalBounds = getLogicalBounds();
            Insets totalDecorationPaddingInCanvas = estimateDecorationPadding(decorationScale);
            return Math.min((canvasBounds.getWidth()-totalDecorationPaddingInCanvas.getLeft()-totalDecorationPaddingInCanvas.getRight())/logicalBounds.getWidth(), (canvasBounds.getHeight()-totalDecorationPaddingInCanvas.getTop()-totalDecorationPaddingInCanvas.getBottom())/logicalBounds.getHeight());
        }
    }

    /**
     * Computes an updated value for {@link #autoTranslation}
     * @return an updated value for {@link #autoTranslation}
     * @see #autoTranslation
     */
    public Point2D computeAutoTranslation() {
        double scale = getScale();
        double decorationScale = getDecorationScale();
        Bounds logicalBounds = getLogicalBounds();
        Bounds boundsInLocal = getBoundsInLocal();
        Bounds canvasBounds = getCanvasBounds();

        Insets decorationPaddingInCanvas = getDecorationPaddingInCanvas();
        Point2D sizeInCanvas = new Point2D(logicalBounds.getWidth(), logicalBounds.getHeight())
                .multiply(scale)
                .add(decorationPaddingInCanvas.getLeft()+decorationPaddingInCanvas.getRight(), decorationPaddingInCanvas.getTop()+decorationPaddingInCanvas.getBottom());
        Point2D canvasMin = new Point2D(canvasBounds.getMinX(), canvasBounds.getMinY());
        Point2D canvasSize = new Point2D(canvasBounds.getWidth(), canvasBounds.getHeight());

        return canvasMin.add(
                canvasSize.subtract(sizeInCanvas)
                        .multiply(0.5)
        )
                .multiply(1/scale)
                .subtract(boundsInLocal.getMinX(), boundsInLocal.getMinY());

    }

    /**
     * Conditionally enables {@link #transformAutomatically}
     * <br/>
     * Automatic transformations will be enabled if the content is small enough to entirely fit into the canvas (i.e. if the size of {@link #boundsInCanvas} is less than the size of {@link #canvasBounds} in both width and height).
     * @see #transformAutomatically
     */
    public void considerAutoTransform() {
        Bounds boundsInCanvas = getBoundsInCanvas();
        Bounds canvasBounds = getCanvasBounds();
        if(boundsInCanvas.getWidth() <= canvasBounds.getWidth() && boundsInCanvas.getHeight() <= canvasBounds.getHeight()) {
            setTransformAutomatically(true);
        }
    }

    /**
     * Computes a potential {@link #decorationScale}
     * @param requestedScale a candidate {@link #scale} value (not necessarily the <i>current</i> value)
     * @return the value that {@link #decorationScale} would take if {@link #scale} was {@code requestedScale}
     * @see #decorationScale
     * @see #scale
     */
    public double estimateDecorationScale(double requestedScale) {
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

    /**
     * Transforms the supplied coordinates ({@code coords}) from local space to canvas space.
     * <br/>
     * The transformation is achieved by translating the coordinates by {@link #translationX} and {@link #translationY}, then multiplying them by {@link #scale}
     * @param coords
     * @return coordinates in canvas space corresponding to {@code coords}
     * @see #localToCanvas(double, double)
     * @see #canvasToLocal(Point2D)
     */
    public Point2D localToCanvas(Point2D coords) {
        double scale = getScale();
        if(scale == 0) {
            return new Point2D(0,0);
        } else {
            return coords.add(translationX.get(), translationY.get()).multiply(scale);
        }
    }

    /**
     * Transforms the supplied coordinates ({@code x} and {@code y}) from local space to canvas space.
     * <br/>
     * The transformation is achieved by translating the coordinates by {@link #translationX} and {@link #translationY}, then multiplying them by {@link #scale}
     * @param x
     * @param y
     * @return coordinates in canvas space corresponding to the coordinates {@code coords} from local space
     * @see #localToCanvas(Point2D)
     * @see #canvasToLocal(double, double)
     */
    public Point2D localToCanvas(double x, double y) {
        return localToCanvas(new Point2D(x,y));
    }

    /**
     * Transforms the supplied coordinates ({@code coords}) from canvas space to local space.
     * <br/>
     * This is the inverse of {@link #localToCanvas(Point2D)}.
     * <br/><br/>
     * The transformation is achieved by dividing the coordinates by {@link #scale} and then translating them by translating them by -{@link #translationX} and -{@link #translationY}
     * @param coords
     * @return coordinates in local space corresponding to the coordinates {@code coords} from canvas space
     * @see #canvasToLocal(double, double)
     * @see #localToCanvas(Point2D)
     */
    public Point2D canvasToLocal(Point2D coords) {
        double scale = getScale();
        if (scale == 0) {
            return coords.subtract(translationX.get(), translationY.get());
        } else {
            return coords.multiply(1 / scale).subtract(translationX.get(), translationY.get());
        }
    }

    /**
     * Transforms the supplied coordinates ({@code coords}) from canvas space to local space.
     * <br/>
     * This is the inverse of {@link #localToCanvas(Point2D)}.
     * <br/><br/>
     * The transformation is achieved by dividing the coordinates by {@link #scale} and then translating them by translating them by -{@link #translationX} and -{@link #translationY}
     * @param x
     * @return coordinates in local space corresponding to the coordinates {@code coords} from canvas space
     * @see #canvasToLocal(Point2D)
     * @see #localToCanvas(double, double)
     */
    public Point2D canvasToLocal(double x, double y) {
        return canvasToLocal(new Point2D(x,y));
    }

    /**
     * Sets {@link #translationX} and disables {@link #transformAutomatically}
     * @param translationX
     */
    public void setTranslationX(double translationX) {
        setTransformAutomatically(false);
        this.translationX.set(translationX);
    }

    /**
     * Sets {@link #translationY} and disables {@link #transformAutomatically}
     * @param translationY
     */
    public void setTranslationY(double translationY) {
        setTransformAutomatically(false);
        this.translationY.set(translationY);
    }

    /**
     * Sets {@link #translationX} to {@code translation.getX()}, sets {@link #translationY} to {@code translation.getY()}, and disables {@link #transformAutomatically}
     * @param translation
     */
    public void setTranslation(Point2D translation) {
        setTranslation(translation.getX(), translation.getY());
    }


    /**
     * Assesses and (potentially) modifies {@link #translationX} and {@link #translationY} for user convenience.
     * <br/>
     * More specificially, this method implements the following algorithms:
     * <br/>
     * 1. Call {@link #considerAutoTransform()} so that automatic transforms are applied (if appropriate).
     * <br/>
     * 2. If {@link #transformAutomatically}, stop.
     * <br/>
     * 3. Otherwise:
     * <br/>
     * 4. If the width of {@link #boundsInCanvas} <= the width of {@link #canvasBounds}, but some part of {@link #boundsInCanvas} lies to the left or right of any part of {@link #canvasBounds}, adjust {@link #translationX} such that the two bounding boxes are flush along the outlying side.
     * <br/>
     * 5. If the height of {@link #boundsInCanvas} <= the height of {@link #canvasBounds}, but some part of {@link #boundsInCanvas} lies above or below any part of {@link #canvasBounds}, adjust {@link #translationY} such that the two bounding boxes are flush along the outlying side.
     */
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
        decorationPaddingInCanvas = new ReadOnlyObjectWrapper<>();
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
     * Note that it is almost always better to use the setters or {@link #translate(double, double)} then to set via properties, as this will ensure that objects don't move entirely off-screen
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
     * Note that it is almost always better to use the setters or {@link #translate(double, double)} then to set via properties, as this will ensure that objects don't move entirely off-screen
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

    public Insets getDecorationPaddingInLocal() {
        return decorationPaddingInLocal.get();
    }

    public ReadOnlyObjectProperty<Insets> decorationPaddingInLocalProperty() {
        return decorationPaddingInLocal.getReadOnlyProperty();
    }

    public Insets getDecorationPaddingInCanvas() {
        return decorationPaddingInCanvas.get();
    }

    public ReadOnlyObjectProperty<Insets> decorationPaddingInCanvasProperty() {
        return decorationPaddingInCanvas.getReadOnlyProperty();
    }

    public double getDecorationScale() {
        return decorationScale.get();
    }

    public ReadOnlyDoubleProperty decorationScaleProperty() {
        return decorationScale.getReadOnlyProperty();
    }
}
