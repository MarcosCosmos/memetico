package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
public interface ISelfLocatingCell extends ICell {
    void applyLocation(Graph g);
}