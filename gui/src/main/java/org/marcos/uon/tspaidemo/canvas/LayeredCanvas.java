package org.marcos.uon.tspaidemo.canvas;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.marcos.uon.tspaidemo.canvas.layer.Layer;

import java.util.Comparator;
import java.util.List;

/**
 * Internally contains one or more canvases
 * Note that text is skipped for now
 * (todo: rename? this is probably more generalised than it was before)
 */
public class LayeredCanvas extends Pane {

    private Color backgroundColor;
    private final ObservableList<Layer> layers;
    private final TransformationContext transformationContext;
    private boolean requiresReordering;


    public LayeredCanvas() {
        layers = FXCollections.observableArrayList();

        layers.addListener((ListChangeListener<Layer>) c -> {
                List<Node> children = getChildren();
                while(c.next()) {
                    if (c.wasRemoved()) {

                        for(Layer each : c.getRemoved()) {
                            Canvas eachCanvas = each.getCanvas();
                            eachCanvas.widthProperty().unbind();
                            eachCanvas.heightProperty().unbind();
                            children.remove(eachCanvas);
                        }
                        requestReorder();
                    }
                    if (c.wasAdded()) {
                        for(Layer each : c.getAddedSubList()) {
                            each.setTransformationContext(getTransformationContext());
                            each.priorityProperty().addListener(
                                    (observable, oldValue, newValue) -> {
                                        if(!newValue.equals(oldValue)) {
                                            requestReorder();
                                        }
                                    }
                            );
                            Canvas eachCanvas = each.getCanvas();
                            eachCanvas.widthProperty().bind(widthProperty());
                            eachCanvas.heightProperty().bind(heightProperty());
                            children.add(eachCanvas);
                        }
                        requestReorder();
                    }
                }
            }
        );

        setBackgroundColor(Color.BLACK);
        requiresReordering = false;

        transformationContext = new TransformationContext();
        transformationContext.canvasBoundsProperty().bind(
                Bindings.createObjectBinding(
                        () -> {
                            //apply some 1px "padding" to deal with minute rounding error and scale-induced fuzzing
                            Bounds boundsInLocal = getBoundsInLocal();
                            return new BoundingBox(boundsInLocal.getMinX(), boundsInLocal.getMinY(), boundsInLocal.getWidth(), boundsInLocal.getHeight());
                        },
                        boundsInLocalProperty()
                )
        );
        //automatically redraw
        transformationContext.translationXProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
        transformationContext.translationYProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
        transformationContext.scaleProperty().addListener((observable, oldValue, newValue) -> requestAllRedraw());
    }


    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
        this.backgroundColor = backgroundColor;
    }

    /**
     * Draws/redraws the canvas on an as-needed basis
     */
    public void draw() {
        if(requiresReordering) {
            layers.sorted(Comparator.comparing(Layer::getPriority)).forEach(each -> each.getCanvas().toFront());
            requiresReordering = false;
        }
        for (Layer each: layers) {
            if(each.requiresRedraw()) {
                each.draw();
            }
        }
    }

    public void requestReorder() {
        requiresReordering = true;
    }

    public boolean requiresReordering() {
        return requiresReordering;
    }

    public TransformationContext getTransformationContext() {
        return transformationContext;
    }

    public ObservableList<Layer> getLayers() {
        return layers;
    }
    /**
     * Flag all layers as needing to drawOnto (because, for example, the canvas size has changed)
     */
    public void requestAllRedraw() {
        layers.forEach(Layer::requestRedraw);
    }
}
