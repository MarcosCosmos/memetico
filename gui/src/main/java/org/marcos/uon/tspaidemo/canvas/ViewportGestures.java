package org.marcos.uon.tspaidemo.canvas;

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
    private final TransformationContext transformationContext;

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
                transformationContext.setMouseAnchor(event.getX(), event.getY());
            }
        }
    };

    private final EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if(event.getButton() == MouseButton.PRIMARY) {
                transformationContext.santiseTranslation();
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
            transformationContext.translate((event.getX() - transformationContext.getMouseAnchorX())/ transformationContext.getScale(), (event.getY() - transformationContext.getMouseAnchorY())/ transformationContext.getScale());

            transformationContext.setMouseAnchor(event.getX(), event.getY());
            event.consume();
        }
    };

    /**
     * Mouse wheel handler: zoom to pivot point
     */
    private final EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

        @Override
        public void handle(ScrollEvent event) {
            if(event.getDeltaY() != 0) {
                double zoom = 0;
                if(event.getDeltaY() > 0) {
                    zoom = getZoomSpeed();
                } else if(event.getDeltaY() < 0){
                    zoom = 1/getZoomSpeed();
                }
                transformationContext.setMouseAnchor(event.getX(), event.getY());
                transformationContext.zoom(event.getDeltaY() < 0 ? 1 / getZoomSpeed() : getZoomSpeed());
            }
            event.consume();
        }

    };

    public ViewportGestures(TransformationContext sceneTransformationContext) {
        this.transformationContext = sceneTransformationContext;
    }

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
