package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * An fxgraph cell for a simple vertex in a tsp instance
 * It supports an optional label. (Todo: possibly support changing the text position?)
 * Todo: see if radius can be bound to css, etc?
 */
public class SimpleVertex extends AbstractCell implements ISelfLocatingCell {
    public static final double DEFAULT_RADIUS = 3;
    public static class VertexGraphic extends Pane {
        private final Group group;
        private final Circle dot;
        private final Text text;
        public VertexGraphic(Graph graph, DoubleProperty radius, StringProperty textProperty) {
            group = new Group();
            dot = new Circle();
            text = new Text();

            group.getStyleClass().add("vertex");
            text.getStyleClass().add("label");
            dot.getStyleClass().add("dot");

            final DoubleProperty textWidth = new SimpleDoubleProperty();
            final DoubleProperty textHeight = new SimpleDoubleProperty();
            text.textProperty().bind(textProperty);
            text.getStyleClass().add("label");

            text.xProperty().bind(
                    dot.centerYProperty().subtract(textWidth.divide(2))
            );
            text.yProperty().bind(
                    dot.centerYProperty().subtract(dot.radiusProperty())
                        .subtract(textHeight.divide(2))
            );

            final Runnable recalculateWidth = () -> {
                textWidth.set(text.getLayoutBounds().getWidth());
                textHeight.set(text.getLayoutBounds().getHeight());
            };
            text.parentProperty().addListener((obs, oldVal, newVal) -> recalculateWidth.run());
            text.textProperty().addListener((obs, oldVal, newVal) -> recalculateWidth.run());


            group.getChildren().addAll(dot, text);

            dot.radiusProperty().bind(radius);

            getChildren().add(group);

        }

        public Group getGroup() {
            return group;
        }

        public Circle getDot() {
            return dot;
        }

        public Text getText() {
            return text;
        }

    }

    private transient final StringProperty textProperty;
    private final DoubleProperty radius;
    private final DoubleProperty locationX;
    private final DoubleProperty locationY;
    public SimpleVertex(double x, double y, double radius) {
        textProperty = new SimpleStringProperty();
        this.radius = new SimpleDoubleProperty(radius);
        locationX = new SimpleDoubleProperty(x);
        locationY = new SimpleDoubleProperty(y);
    }
    public SimpleVertex(double x, double y) {
        this(x,y,DEFAULT_RADIUS);
    }

    public StringProperty textProperty() {
        return textProperty;
    }
    public DoubleProperty radius() {
        return radius;
    }
    public DoubleProperty locationX() {return locationX;}
    public DoubleProperty locationY() {return locationY;}

    @Override
    public Region getGraphic(Graph graph) {
        return new VertexGraphic(graph, radius, textProperty);
    }

    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        return Bindings.createDoubleBinding(locationX::get, locationX);
    }

    public DoubleBinding getYAnchor(Graph graph, IEdge edge) {
        return Bindings.createDoubleBinding(locationY::get, locationY);
    }

    public void applyLocation(Graph g) {
        g.getGraphic(this).relocate(locationX.get(), locationY.get());
    }
}
