package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;

public class PredictionEdge extends SimpleEdge {
    public PredictionEdge(ICell source, ICell target) {
        super(source, target);
    }

    @Override
    public EdgeGraphic getGraphic(Graph graph) {
        EdgeGraphic graphic = super.getGraphic(graph);
        graphic.getStyleClass().add("prediction");
        return graphic;
    }

}
