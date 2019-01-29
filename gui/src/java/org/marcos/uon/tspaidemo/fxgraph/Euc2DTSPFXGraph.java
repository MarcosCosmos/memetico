package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.Model;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.geometry.Point2D;
import org.jorlib.io.tspLibReader.TSPInstance;
import org.jorlib.io.tspLibReader.Tour;
import org.jorlib.io.tspLibReader.graph.Edge;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates JavaFX display of graphs (including tours) for Euclidean 2D TSP instances.
 * Todo: possibly make this modifiable; Possibly using observable lists, possibly using views and explicit add/remove helpers.
 */
public class Euc2DTSPFXGraph {
    private TSPInstance theInstance;
    private Graph fxGraph;
    List<SimpleVertex> cells;
    List<List<TourEdge>> tours;
    List<SimpleEdge> normalEdges;

    /**
     *
     * @param instance A tsp instance,with main data already loaded from file;
     * @throws InvalidArgumentException if the instance is not Euclidean 2D
     */
    Euc2DTSPFXGraph(TSPInstance instance) throws InvalidArgumentException {
        theInstance = instance;
        fxGraph = new Graph();
        NodeCoordinates nodeData;
        switch (this.theInstance.getDisplayDataType()) {
            case TWOD_DISPLAY:
                nodeData = this.theInstance.getDisplayData();
            case COORD_DISPLAY:
                nodeData = (NodeCoordinates) this.theInstance.getDistanceTable();
                //only try to display 2d nodes for now; maybe use jgrapht for more?
                if(nodeData.get(nodeData.listNodes()[0]).getPosition().length == 2) {
                    break;
                }
            default:
//                primaryStage.setScene(new Scene(new Group(new Text("Could not display instance"))));
                return;
        }

        final Model model = fxGraph.getModel();

        fxGraph.beginUpdate();

        cells = Stream.generate(() -> (SimpleVertex)null)
                .limit(nodeData.size())
                .collect(Collectors.toList());

        //stroke the dots ontop of the edges
        for(int eachIndex : nodeData.listNodes()) {
            org.jorlib.io.tspLibReader.graph.Node eachNode = nodeData.get(eachIndex);
            double[] eachPos = eachNode.getPosition();
            SimpleVertex eachCell = new SimpleVertex(eachPos[0], eachPos[1]);
            eachCell.textProperty().set("C"+String.valueOf(eachIndex));
            cells.set(eachIndex, eachCell);
            model.addCell(eachCell);
        }


        Set<List<Integer>> options = new HashSet<>();
        for(int i=0; i<nodeData.size(); ++i) {
            for(int k=i+1; k<nodeData.size(); ++k) {
                options.add(Arrays.asList(i, k));
            }
        }

        for(Tour eachTour : this.theInstance.getTours()) {
            List<TourEdge> eachEdgeList = new ArrayList<>();
            for(Edge eachEdge : eachTour.toEdges()) {
                int _a = eachEdge.getId1()-1;
                int _b = eachEdge.getId2()-1;
                options.remove(Arrays.asList(_a, _b));
                SimpleVertex a = cells.get(_a),
                        b = cells.get(_b)
                                ;
                TourEdge eachResult = new TourEdge(a,b);
                eachResult.textProperty().set(String.valueOf(new Point2D(a.locationX().get(), b.locationY().get()).distance(b.locationX().get(), b.locationY().get())));
                eachEdgeList.add(eachResult);
            }
            tours.add(eachEdgeList);
        }

        List<List<Integer>> randomisedNodes = new ArrayList<>(options);
        Collections.shuffle(randomisedNodes);
        //only take as many extra edges as there are nodes
        randomisedNodes.subList(50, randomisedNodes.size()).clear();

        for(List<Integer> eachPair : randomisedNodes) {
            SimpleVertex a = cells.get(eachPair.get(0)),
                    b = cells.get(eachPair.get(1))
                            ;
            SimpleEdge eachResult = new SimpleEdge(a,b);
            eachResult.textProperty().set(String.valueOf(new Point2D(a.locationX().get(), b.locationY().get()).distance(b.locationX().get(), b.locationY().get())));
            normalEdges.add(eachResult);
        }

        Stream.of(
                normalEdges.stream(),
                tours.stream()
                        .flatMap(Collection::stream)
        ).flatMap(each -> each)
                .forEach(model::addEdge);

        fxGraph.endUpdate();
    }
}
