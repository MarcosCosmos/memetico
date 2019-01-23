package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.cells.CellGestures;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;

/**
 * An fxgraph cell for a simple vertex in a tsp instance
 * It supports an optional label. (Todo: possibly support changing the text position?)
 * Todo: see if radius can be bound to css, etc?
 */
public class SimpleVertexCell extends AbstractCell {
    public static final double PREFERRED_RADIUS = 3;
    public static class VertexGraphic extends BorderPane {
        private final Group group;
        private final Circle dot;
        private final Text text;
        public VertexGraphic(Graph graph, StringProperty textProperty) {
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
                    dot.centerXProperty()
                            .subtract(textWidth.divide(2))
            );
            text.yProperty().bind(
                    dot.centerYProperty()
                        .subtract(dot.radiusProperty())
                        .subtract(textHeight.divide(2))
            );

            final Runnable recalculateWidth = () -> {
                textWidth.set(text.getLayoutBounds().getWidth());
                textHeight.set(text.getLayoutBounds().getHeight());
            };
            text.parentProperty().addListener((obs, oldVal, newVal) -> recalculateWidth.run());
            text.textProperty().addListener((obs, oldVal, newVal) -> recalculateWidth.run());


            group.getChildren().addAll(dot, text);

            dot.setRadius(PREFERRED_RADIUS);

            setCenter(group);



//
//            group.translateXProperty().bind();
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

    public SimpleVertexCell() {
        textProperty = new SimpleStringProperty();

    }

    public StringProperty textProperty() {
        return textProperty;
    }

    @Override
    public Region getGraphic(Graph graph) {
        return new VertexGraphic(graph, textProperty);
    }

    public DoubleBinding getXAnchor(Graph graph, IEdge edge) {
        VertexGraphic graphic = (VertexGraphic) graph.getGraphic(this);
        DoubleProperty boundsMinX = new SimpleDoubleProperty();
        boundsMinX.bind(Bindings.selectDouble(graphic.getGroup().getBoundsInParent(), "minX"));
        return graphic.layoutXProperty().subtract(boundsMinX);
    }

    public DoubleBinding getYAnchor(Graph graph, IEdge edge) {
        VertexGraphic graphic = (VertexGraphic) graph.getGraphic(this);
        DoubleProperty boundsMinY = new SimpleDoubleProperty();
        boundsMinY.bind(Bindings.selectDouble(graphic.getGroup().getBoundsInParent(), "minY"));
        return graphic.layoutYProperty().subtract(boundsMinY);
    }
}
