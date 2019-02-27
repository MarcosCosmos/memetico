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
        private final DragContext sceneDragContext = new DragContext();

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
            }

        };

        private final EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // right mouse button => panning
                if(!event.isSecondaryButtonDown()) {
                    return;
                }

                sceneDragContext.setTranslateAnchorX(sceneDragContext.getTranslateAnchorX() + (event.getSceneX() - sceneDragContext.getMouseAnchorX())/sceneDragContext.getScale());
                sceneDragContext.setTranslateAnchorY(sceneDragContext.getTranslateAnchorY() + (event.getSceneY() - sceneDragContext.getMouseAnchorY())/sceneDragContext.getScale());
                event.consume();
            }
        };

        /**
         * Mouse wheel handler: zoom to pivot point
         */
        private final EventHandler<ScrollEvent> onScrollEventHandler = new EventHandler<ScrollEvent>() {

            @Override
            public void handle(ScrollEvent event) {
                double oldScale = sceneDragContext.getScale();
                double scale;
                if(event.getDeltaY() < 0) {
                    scale = oldScale/getZoomSpeed();
                } else {
                    scale = oldScale*getZoomSpeed();
                }

//                scale = clamp(scale, minScaleProperty.get(), maxScaleProperty.get());
                final double f = (scale / oldScale) - 1;

                // maxX = right overhang, maxY = lower overhang
                final double maxX = canvas.getBoundsInParent().getMaxX() - canvas.localToParent(canvas.getPrefWidth(), canvas.getPrefHeight()).getX();
                final double maxY = canvas.getBoundsInParent().getMaxY() - canvas.localToParent(canvas.getPrefWidth(), canvas.getPrefHeight()).getY();

                // minX = left overhang, minY = upper overhang
                final double minX = canvas.localToParent(0, 0).getX() - canvas.getBoundsInParent().getMinX();
                final double minY = canvas.localToParent(0, 0).getY() - canvas.getBoundsInParent().getMinY();

                // adding the overhangs together, as we only consider the width of canvas itself
                final double subX = maxX + minX;
                final double subY = maxY + minY;

                // subtracting the overall overhang from the width and only the left and upper overhang from the upper left point
                final double dx = (event.getSceneX() - ((canvas.getBoundsInParent().getWidth() - subX) / 2 + (canvas.getBoundsInParent().getMinX() + minX)));
                final double dy = (event.getSceneY() - ((canvas.getBoundsInParent().getHeight() - subY) / 2 + (canvas.getBoundsInParent().getMinY() + minY)));

                canvas.setScale(scale);

                //not anymore; it must be transformed in order to be applied virtually
//                // note: pivot value must be untransformed, i. e. without scaling
//                canvas.setPivot(f * dx, f * dy);

                event.consume();

            }

        };

        public static double clamp(double value, double min, double max) {
            if(Double.compare(value, min) < 0) {
                return min;
            }

            if(Double.compare(value, max) > 0) {
                return max;
            }

            return value;
        }

        public double getMinScale() {
            return minScaleProperty.get();
        }

        public void setMinScale(double minScale) {
            minScaleProperty.set(minScale);
        }

        public DoubleProperty minScaleProperty() {
            return minScaleProperty;
        }

        public double getMaxScale() {
            return maxScaleProperty.get();
        }

        public DoubleProperty maxScaleProperty() {
            return maxScaleProperty;
        }

        public void setMaxScale(double maxScale) {
            maxScaleProperty.set(maxScale);
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
}
