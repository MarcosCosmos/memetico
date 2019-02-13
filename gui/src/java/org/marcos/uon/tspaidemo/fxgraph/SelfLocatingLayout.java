package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;

import java.util.Iterator;
import java.util.List;

public class SelfLocatingLayout implements Layout {
    public SelfLocatingLayout() {

    }
    public void execute(Graph graph) {
        List<ICell> cells = graph.getModel().getAllCells();
        Iterator var3 = cells.iterator();

        while(var3.hasNext()) {
            ISelfLocatingCell cell = (ISelfLocatingCell) var3.next();
            cell.applyLocation(graph);
        }

    }
}
