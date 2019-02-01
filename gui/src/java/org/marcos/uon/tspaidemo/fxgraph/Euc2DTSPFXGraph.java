package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.*;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
    private Graph fxGraph;
    private List<SimpleVertex> cells;
    private List<List<TourEdge>> tours;
    private List<SimpleEdge> normalEdges;

//    private final BorderPane root = new BorderPane();


    /**
     *
     * @param instance A tsp instance,with main data already loaded from file;
     * @throws InvalidArgumentException if the instance is not Euclidean 2D
     */
    public Euc2DTSPFXGraph(TSPInstance instance) throws InvalidArgumentException {
        fxGraph = new Graph();
        tours = new ArrayList<>();
        normalEdges = new ArrayList<>();

        fxGraph.getUseNodeGestures().setValue(false);

        NodeCoordinates nodeData;
        switch (instance.getDisplayDataType()) {
            case TWOD_DISPLAY:
                nodeData = instance.getDisplayData();
            case COORD_DISPLAY:
                nodeData = (NodeCoordinates) instance.getDistanceTable();
                //only try to display 2d nodes for now; maybe use jgrapht for more?
                if(nodeData.get(nodeData.listNodes()[0]).getPosition().length == 2) {
                    break;
                }
            default:
//                primaryStage.setScene(new Scene(new Group(new Text("Could not display instance"))));
                cells = new ArrayList<>();
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
        for(Tour eachTour : instance.getTours()) {
            List<TourEdge> eachEdgeList = new ArrayList<>();
            for(Edge eachEdge : eachTour.toEdges()) {
                int _a = eachEdge.getId1()-1;
                int _b = eachEdge.getId2()-1;
                SimpleVertex a = cells.get(_a),
                        b = cells.get(_b)
                                ;
                TourEdge eachResult = new TourEdge(a,b);
                eachResult.textProperty().set(String.valueOf(new Point2D(a.locationX().get(), b.locationY().get()).distance(b.locationX().get(), b.locationY().get())));
                eachEdgeList.add(eachResult);
            }
            tours.add(Collections.unmodifiableList(eachEdgeList));
        }
        Stream.of(
                normalEdges.stream(),
                tours.stream()
                        .flatMap(Collection::stream)
        ).flatMap(each -> each)
                .forEach(model::addEdge);

//        //scale the cells for visibility
//        for(SimpleVertex each : cells) {
//            each.locationX().set(each.locationX().get()*10);
//            each.locationY().set(each.locationY().get()*10);
//        }

        fxGraph.endUpdate();

        fxGraph.layout(new SelfLocatingLayout());

        //crop the layout
        BoundingBox bounds = getLogicalBounds();
        for (SimpleVertex cell : cells) {
            cell.locationX().subtract(bounds.getMinX());
            cell.locationY().subtract(bounds.getMinY());
        }
        fxGraph.layout(new SelfLocatingLayout());

        BoundingBox boundsB = getLogicalBounds();
//        root.setCenter();
        Region canvas = fxGraph.getCanvas();
        canvas.getStyleClass().add("graph");
    }

    /**
     * Determines whether or not there is anything to display
     * @return
     */
    public boolean isEmpty() {
        return cells.isEmpty();
    }

    //todo: make these modifications affect the inter

    public List<List<TourEdge>> getTours() {
        return Collections.unmodifiableList(tours);
    }

    public void clearTours() {
        ObservableList<IEdge> toRemove = fxGraph.getModel().getRemovedEdges();
        for(List<TourEdge> eachTour : tours) {
            toRemove.addAll(eachTour);
        }
        fxGraph.endUpdate();
    }



    public void addTour(List<int[]> edges) {
        //        graph.beginUpdate(); //only one of beginUpdate/endUpdate actually need to be called; begin update should probably never be called since it just wipes the canvas without changing other lists?
        Model model = fxGraph.getModel();
        List<TourEdge> edgeList = new ArrayList<>();
        for(int[] eachEdge : edges) {
            SimpleVertex a = cells.get(eachEdge[0]),
                    b = cells.get(eachEdge[1])
            ;
            TourEdge eachResult = new TourEdge(a,b);
            eachResult.textProperty().set(String.valueOf(new Point2D(a.locationX().get(), b.locationY().get()).distance(b.locationX().get(), b.locationY().get())));
            edgeList.add(eachResult);
            model.addEdge(eachResult);
        }
        //add the return-to-start

        tours.add(Collections.unmodifiableList(edgeList));
        fxGraph.endUpdate();
    }

    public PannableCanvas getCanvas() {
        return fxGraph.getCanvas();
    }

    /**
     * Returns the bounds as determined by the cells
     * @return
     */
    public BoundingBox getLogicalBounds() {
        double minX=Double.POSITIVE_INFINITY, minY=Double.POSITIVE_INFINITY, maxX=Double.NEGATIVE_INFINITY, maxY=Double.NEGATIVE_INFINITY;
        for(SimpleVertex each : cells) {
            double x = each.locationX().get();
            double y = each.locationY().get();
            double radius = each.radius().get();
            double mnX = x-radius, mxX = x+radius, mnY = y-radius, mxY = y+radius;
            if(minX > mnX) {
                minX=mnX;
            }
            if(minY > mnY) {
                minY=mnY;
            }
            if(maxX < mxX) {
                maxX=mxX;
            }
            if(maxY < mxY) {
                maxY=mxY;
            }
        }

        return new BoundingBox(minX, minY, maxX-minX, maxY-minY);

    }
}
