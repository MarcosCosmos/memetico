package org.marcos.uon.tspaidemo.canvas;

import javafx.application.Application;
import javafx.geometry.BoundingBox;
import javafx.stage.Stage;
import org.marcos.uon.tspaidemo.canvas.drawable.Edge;
import org.marcos.uon.tspaidemo.canvas.drawable.Vertex;
import org.marcos.uon.tspaidemo.canvas.layer.ListLayer;
import org.marcos.uon.tspaidemo.canvas.layer.VertexLayer;
import org.marcos.uon.tspaidemo.canvas.test.util.Display;

import java.util.Arrays;

public class DataDisplayExample extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        VertexLayer vertexLayer = new VertexLayer(100);
        ListLayer<Edge> edgeLayer = new ListLayer<>(0);
        for(int i = 0; i < 6; ++i) {
            vertexLayer.add(
                    new Vertex(Math.random()*100, Math.random()*100, Math.random()*5 + 8, Math.random()*4 + 2, "", CanvasTSPGraph.DEFAULT_DOT_FILL, CanvasTSPGraph.DEFAULT_DOT_STROKE, CanvasTSPGraph.DEFAULT_LABEL_COLOR)
            );
        }

        for (int i = 0; i < vertexLayer.size(); i++) {
            for (int k = i+1; k < vertexLayer.size(); k++) {
                edgeLayer.add(
                        new Edge(vertexLayer.get(i), vertexLayer.get(k), "", CanvasTSPGraph.DEFAULT_EDGE_COLOR, CanvasTSPGraph.DEFAULT_LABEL_COLOR)
                );
            }
        }

        TransformationContext context = new TransformationContext();
        context.setTransformAutomatically(true);
        context.logicalBoundsProperty().bind(vertexLayer.logicalBoundsProperty());
        context.boundsInLocalProperty().bind(vertexLayer.boundsInLocalProperty());
        context.setCanvasBounds(new BoundingBox(0, 0, 500, 500));
        Display.displayContext(primaryStage, context, Arrays.asList(vertexLayer, edgeLayer));
    }
}
