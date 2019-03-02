package org.marcos.uon.tspaidemo.canvas;

import com.fxgraph.graph.PannableCanvas;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

/**
 * Derived from com.fxgraph.graph.ViewportGestures
 * Listeners for making the scene's viewport draggable and zoomable
 */
public class ViewportGestures {

    //todo: finish me

    private final DoubleProperty zoomSpeedProperty = new SimpleDoubleProperty(1.2d);
    private final DragContext sceneDragContext;

    public EventHandler<MouseEvent> getOnMousePressedEventHandler() {
        return onMousePressedEventHandler;
    }

    public EventHandler<MouseEvent> getOnMouseDraggedEventHandler() {
        return onMouseDraggedEventHandler;
    }

    public EventHandler<ScrollEvent> getOnScrollEventHandler() {
        return onScrollEventHandler;
    }

    public EventHandler<MouseEvent> getOnMouseReleasedEventHandler() {
        return onMouseReleasedEventHandler;
    }

    private final EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            if (event.isPrimaryButtonDown()) {
                sceneDragContext.setMouseAnchor(event.getX(), event.getY());
            }
        }
    };

    private final EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(event.getButton() == MouseButton.PRIMARY) {
                sceneDragContext.santiseTranslation();
            }
        }
    };

    private final EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {

            // right mouse button => panning
            if(!event.isPrimaryButtonDown()) {
                return;
            }

//            sceneDragContext.setTranslationX(sceneDragContext.getTranslationX() + (event.getX() - sceneDragContext.getMouseAnchorX())/sceneDragContext.getScale());
//            sceneDragContext.setTranslationY(sceneDragContext.getTranslationY() + (event.getY() - sceneDragContext.getMouseAnchorY())/sceneDragContext.getScale());
            sceneDragContext.translate((event.getX() - sceneDragContext.getMouseAnchorX())/sceneDragContext.getScale(), (event.getY() - sceneDragContext.getMouseAnchorY())/sceneDragContext.getScale());

            sceneDragContext.setMouseAnchor(event.getX(), event.getY());
//            sceneDragContext.santiseTranslation();
            event.consume();
        }
    };

    /**
     * Mouse wheel handler: zoom to pivot point
     */
    private final EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {
            sceneDragContext.setMouseAnchor(event.getX(), event.getY());
            sceneDragContext.zoom(event.getDeltaY() < 0 ? 1/getZoomSpeed() : getZoomSpeed());
//            sceneDragContext.santiseTranslation();
            event.consume();
        }

    };

    public ViewportGestures(DragContext sceneDragContext) {
        this.sceneDragContext = sceneDragContext;
    }

//    public static double clamp(double value, double min, double max) {
//        if(Double.compare(value, min) < 0) {
//            return min;
//        }
//
//        if(Double.compare(value, max) > 0) {
//            return max;
//        }
//
//        return value;
//    }

    public double getZoomSpeed() {
        return zoomSpeedProperty.get();
    }

    public DoubleProperty zoomSpeedProperty() {
        return zoomSpeedProperty;
    }

    public void setZoomSpeed(double zoomSpeed) {
        zoomSpeedProperty.set(zoomSpeed);
    }
}
