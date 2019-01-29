package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;

public class TourEdge extends SimpleEdge {
    public TourEdge(ICell source, ICell target) {
        super(source, target);
    }

    @Override
    public EdgeGraphic getGraphic(Graph graph) {
        EdgeGraphic graphic = super.getGraphic(graph);
        graphic.getStyleClass().add("tour");
        return graphic;
    }

}
