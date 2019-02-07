package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.IEdge;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SimpleEdge implements IEdge {
    private final transient StringProperty textProperty = new SimpleStringProperty();
    private final transient List<String> additionalStyleClasses = new ArrayList<>();

    private ObjectProperty<SimpleVertex> source = new SimpleObjectProperty<SimpleVertex>();
    private ObjectProperty<SimpleVertex> target = new SimpleObjectProperty<SimpleVertex>();

    public SimpleEdge(SimpleVertex source, SimpleVertex target, ListProperty<String> additionalStyleClasses) {
        this.source.setValue(source);
        this.target.setValue(target);
        this.additionalStyleClasses.addAll(additionalStyleClasses);
    }

    public SimpleEdge(SimpleVertex source, SimpleVertex target) {
        this(source, target, new SimpleListProperty<>());

    }

    public EdgeGraphic getGraphic(Graph graph) {
        return new EdgeGraphic();
    }

    public StringProperty textProperty() {
        return this.textProperty;
    }

    @Override
    public ICell getSource() {
        return source.get();
    }

    @Override
    public ICell getTarget() {
        return target.get();
    }

    public ObjectProperty<SimpleVertex> sourceProperty() {
        return source;
    }

    public ObjectProperty<SimpleVertex> targetProperty() {
        return target;
    }

    public class EdgeGraphic extends Pane {
        private final Group group = new Group();
        private final Line line = new Line();
        private final Text text;


        public EdgeGraphic() {
            DoubleBinding sourceX = Bindings.createDoubleBinding(
                    () -> source.get().getXAnchor().get(),
                    source
            );
            DoubleBinding sourceY = Bindings.createDoubleBinding(
                    () -> source.get().getYAnchor().get(),
                    source
            );
            DoubleBinding targetX = Bindings.createDoubleBinding(
                    () -> target.get().getXAnchor().get(),
                    target
            );
            DoubleBinding targetY = Bindings.createDoubleBinding(
                    () -> target.get().getYAnchor().get(),
                    target
            );
            this.line.startXProperty().bind(sourceX);
            this.line.startYProperty().bind(sourceY);
            this.line.endXProperty().bind(targetX);
            this.line.endYProperty().bind(targetY);
            this.group.getChildren().add(this.line);
            DoubleProperty textWidth = new SimpleDoubleProperty();
            DoubleProperty textHeight = new SimpleDoubleProperty();
            this.text = new Text();
            this.text.textProperty().bind(textProperty);
            this.text.xProperty().bind(this.line.startXProperty().add(this.line.endXProperty()).divide(2).subtract(textWidth.divide(2)));
            this.text.yProperty().bind(this.line.startYProperty().add(this.line.endYProperty()).divide(2).subtract(textHeight.divide(2)));
            Runnable recalculateWidth = () -> {
                textWidth.set(this.text.getLayoutBounds().getWidth());
                textHeight.set(this.text.getLayoutBounds().getHeight());
            };
            this.text.parentProperty().addListener((obs, oldVal, newVal) -> {
                recalculateWidth.run();
            });
            this.text.textProperty().addListener((obs, oldVal, newVal) -> {
                recalculateWidth.run();
            });
            this.group.getChildren().add(this.text);
            this.getChildren().add(this.group);

            this.group.getStyleClass().add("edge");
            this.line.getStyleClass().add("line");
            this.text.getStyleClass().add("label");

            this.getStyleClass().addAll(additionalStyleClasses);

        }

        public Group getGroup() {
            return this.group;
        }

        public Line getLine() {
            return this.line;
        }

        public Text getText() {
            return this.text;
        }
    }
    public void setAdditionalStyleClasses(List<String> classes) {
        this.additionalStyleClasses.clear();
        this.additionalStyleClasses.addAll(classes);
    }
}
