package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.cells.AbstractCell;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
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
    public static final double PREFERRED_RADIUS = 20.5;
    public static class VertexGraphic extends Pane {
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

//            final DoubleBinding sourceX = edge.getSource().getXAnchor(graph, edge);
//            final DoubleBinding sourceY = edge.getSource().getYAnchor(graph, edge);
//            final DoubleBinding targetX = edge.getTarget().getXAnchor(graph, edge);
//            final DoubleBinding targetY = edge.getTarget().getYAnchor(graph, edge);
//
//            line.startXProperty().bind(sourceX);
//            line.startYProperty().bind(sourceY);
//
//            line.endXProperty().bind(targetX);
//            line.endYProperty().bind(targetY);
//            group.getChildren().add(line);

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
                        .add(dot.radiusProperty())
                        .add(textHeight.divide(2))
            );

            final Runnable recalculateWidth = () -> {
                textWidth.set(text.getLayoutBounds().getWidth());
                textHeight.set(text.getLayoutBounds().getHeight());
            };
            text.parentProperty().addListener((obs, oldVal, newVal) -> recalculateWidth.run());
            text.textProperty().addListener((obs, oldVal, newVal) -> recalculateWidth.run());


            group.getChildren().addAll(dot, text);
            getChildren().addAll(dot, text); //wrapper group for autosizing

            dot.setRadius(PREFERRED_RADIUS);

//            setPrefSize(PREFERRED_RADIUS*2, PREFERRED_RADIUS*2);

            final Scale scale = new Scale(1, 1);
            dot.getTransforms().add(scale);
            scale.xProperty().bind(widthProperty().divide(PREFERRED_RADIUS));
            scale.yProperty().bind(heightProperty().divide(PREFERRED_RADIUS));
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
}
