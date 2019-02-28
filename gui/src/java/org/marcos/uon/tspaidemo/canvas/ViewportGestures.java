package org.marcos.uon.tspaidemo.canvas;

import com.fxgraph.graph.PannableCanvas;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
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

    private final EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            // right mouse button => panning
            if(!event.isSecondaryButtonDown()) {
                return;
            }

            sceneDragContext.setMouseAnchorX(event.getSceneX());
            sceneDragContext.setMouseAnchorY(event.getSceneY());
//
//            sceneDragContext.setTranslateAnchorX(); = canvas.getTranslateX();
//            sceneDragContext.translateAnchorY = canvas.getTranslateY();
        }

    };

    private final EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {

            // right mouse button => panning
            if(!event.isSecondaryButtonDown()) {
                return;
            }

//            sceneDragContext.setTranslateAnchorX(sceneDragContext.getTranslateAnchorX() + (event.getSceneX() - sceneDragContext.getMouseAnchorX())/sceneDragContext.getScale());
//            sceneDragContext.setTranslateAnchorY(sceneDragContext.getTranslateAnchorY() + (event.getSceneY() - sceneDragContext.getMouseAnchorY())/sceneDragContext.getScale());
            sceneDragContext.translate((event.getSceneX() - sceneDragContext.getMouseAnchorX())/sceneDragContext.getScale(), (event.getSceneY() - sceneDragContext.getMouseAnchorY())/sceneDragContext.getScale());

            sceneDragContext.setMouseAnchorX(event.getSceneX());
            sceneDragContext.setMouseAnchorY(event.getSceneY());
            event.consume();
        }
    };

    /**
     * Mouse wheel handler: zoom to pivot point
     */
    private final EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {
            sceneDragContext.zoom(event.getDeltaY() < 0 ? 1/getZoomSpeed() : getZoomSpeed());
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
