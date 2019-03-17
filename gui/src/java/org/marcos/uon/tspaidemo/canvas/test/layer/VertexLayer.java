package org.marcos.uon.tspaidemo.canvas.test.layer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import org.marcos.uon.tspaidemo.canvas.test.drawable.Vertex;

public class VertexLayer extends ListLayer<Vertex> {

    private final ReadOnlyObjectWrapper<Bounds> logicalBounds;
    private final ReadOnlyObjectWrapper<Bounds> boundsInLocal;
    private final BooleanProperty boundsValid = new SimpleBooleanProperty(true);

    public VertexLayer(int priority) {
        super(priority);
        logicalBounds = new ReadOnlyObjectWrapper<>(new BoundingBox(0,0,0,0));
        boundsInLocal = new ReadOnlyObjectWrapper<>(new BoundingBox(0,0,0,0));
        boundsValid.addListener((observable, oldValue, newValue) -> {
            if(!newValue) {
                updateBounds();
                boundsValid.set(true);
            }
        });
    }

    /**
     * {@inheritDoc}
     * Note: also updates the boundsInLocal
     */
    @Override
    public void requestRedraw() {
        //if this is the first drawOnto request since the last drawOnto, update the bounds
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
        for(Vertex each : this) {
            double x = each.getLocation().getX();
            double y = each.getLocation().getY();
            double baseRadius = each.getDotRadius() + each.getStrokeWidth()/2.0;
            double mnX = x-baseRadius, mxX=x+baseRadius, mnY=y-baseRadius, mxY=y+baseRadius;

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
        }

        logicalBounds.set(new BoundingBox(minLogicalX, minLogicalY, maxLogicalX-minLogicalX, maxLogicalY-minLogicalY));
        boundsInLocal.set(new BoundingBox(minLocalX, minLocalY, maxLocalX-minLocalX, maxLocalY-minLocalY));
    }

    public Bounds getLogicalBounds() {
        return logicalBounds.get();
    }

    public ReadOnlyObjectProperty<Bounds> logicalBoundsProperty() {
        return logicalBounds.getReadOnlyProperty();
    }

    public Bounds getBoundsInLocal() {
        return boundsInLocal.get();
    }

    public ReadOnlyObjectProperty<Bounds> boundsInLocalProperty() {
        return boundsInLocal.getReadOnlyProperty();
    }
}
