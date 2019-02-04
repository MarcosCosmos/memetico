package org.marcos.uon.tspaidemo.fxgraph;

import com.fxgraph.graph.*;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.TSPLibTour;
import org.jorlib.io.tspLibReader.graph.Edge;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates JavaFX display of graphs (including tours) for Euclidean 2D TSP instances.
 * Todo: possibly make this modifiable; Possibly using observable lists, possibly using views and explicit add/remove helpers.
 */
public class Euc2DTSPFXGraph {
    public static final ListProperty<String>
            TARGET_STYLE_CLASSES = new SimpleListProperty<String>(FXCollections.unmodifiableObservableList(FXCollections.observableArrayList("target"))),
            PREDICTION_STYLE_CLASSES = new SimpleListProperty<>(FXCollections.unmodifiableObservableList(FXCollections.observableArrayList("prediction"))),
            EMPTY_STYLE_CLASSES = new SimpleListProperty<>(FXCollections.unmodifiableObservableList(FXCollections.emptyObservableList()));

    /**
     * Helper that reduces memory overhead/gc costs by recycling edges
     */
    protected static final class SimpleEdgePool {
        private final List<SimpleEdge> available = new ArrayList<>();

        public SimpleEdgePool() {

        }

        public void discard(SimpleEdge edge) {
            available.add(edge);
        }

        public void discardAll(Collection<SimpleEdge> edges) {
            available.addAll(edges);
        }

        public void discardAll(SimpleEdge... edges) {
            available.addAll(Arrays.asList(edges));
        }

        /**
         * Provides a surrogate constructor; draws from the pool and assigns properties etc as needed,
         * @return
         */
        public SimpleEdge retrieve(SimpleVertex source, SimpleVertex target, ListProperty<String> additionalStyleClasses) {
            if(!available.isEmpty()) {
                SimpleEdge result = available.remove(available.size()-1);
                result.sourceProperty().set(source);
                result.targetProperty().set(target);
                result.additionalStyleClassesProperty().bind(additionalStyleClasses);
                return result;
            } else {
                return new SimpleEdge(source, target, additionalStyleClasses);
            }
        }
        public SimpleEdge retrieve(SimpleVertex source, SimpleVertex target) {
            return retrieve(source, target, EMPTY_STYLE_CLASSES);
        }
    }

    protected Graph fxGraph;
    protected List<SimpleVertex> cells;
    protected List<List<SimpleEdge>> targets;
    protected List<List<SimpleEdge>> predictions;
    protected List<SimpleEdge> normalEdges;

    protected final transient SimpleEdgePool edgePool = new SimpleEdgePool();

    private final BorderPane root = new BorderPane();


    /**
     *
     * @param instance A tsp instance,with main data already loaded from file;
     * @throws InvalidArgumentException if the instance is not Euclidean 2D
     */
    public Euc2DTSPFXGraph(TSPLibInstance instance) throws InvalidArgumentException {
        fxGraph = new Graph();
        targets = new ArrayList<>();
        predictions = new ArrayList<>();
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

        //todo: possibly add option to display base edgeset (as "normal" edges)

        for(TSPLibTour eachTour : instance.getTours()) {
            List<SimpleEdge> eachEdgeList = new ArrayList<>();
            for(Edge eachEdge : eachTour.toEdges()) {
                int _a = eachEdge.getId1();
                int _b = eachEdge.getId2();
                SimpleVertex a = cells.get(_a),
                        b = cells.get(_b)
                                ;
                SimpleEdge eachResult = edgePool.retrieve(a,b);
                eachResult.textProperty().set(String.valueOf(new Point2D(a.locationX().get(), b.locationY().get()).distance(b.locationX().get(), b.locationY().get())));
                eachEdgeList.add(eachResult);
            }
            targets.add(Collections.unmodifiableList(eachEdgeList));
        }
        Stream.of(
                normalEdges.stream(),
                targets.stream()
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
        fxGraph.layout(new SelfLocatingLayout());

        root.setCenter(fxGraph.getCanvas());
        Region canvas = fxGraph.getCanvas();
        root.getStyleClass().add("graph");
    }

    /**
     * Determines whether or not there is anything to display
     * @return
     */
    public boolean isEmpty() {
        return cells.isEmpty();
    }

    //todo: make these modifications affect the inter

    public List<List<SimpleEdge>> getTargets() {
        return Collections.unmodifiableList(targets);
    }

    public List<List<SimpleEdge>> getPredictions() {
        return Collections.unmodifiableList(predictions);
    }

    private void clearEdges(List<List<SimpleEdge>> edgeCategory) {
//        ObservableList<IEdge> toRemove = fxGraph.getModel().getRemovedEdges();
//        for(List<SimpleEdge> eachTour : edgeCategory) {
//            toRemove.addAll(eachTour);
//        }
        edgeCategory.forEach(edgePool::discardAll);
        edgeCategory.clear();
    }

    public void clearTargets() {
        clearEdges(targets);
    }

    public void clearPredictions() {
        clearEdges(predictions);
    }

    private void addEdges(List<List<SimpleEdge>> edgeCategory, List<int[]> newEdges, BiFunction<SimpleVertex, SimpleVertex, SimpleEdge> constructor) {
        //        graph.beginUpdate(); //only one of beginUpdate/endUpdate actually need to be called; begin update should probably never be called since it just wipes the canvas without changing other lists?
//        Model model = fxGraph.getModel();
        List<SimpleEdge> edgeList = new ArrayList<>();
        for(int[] eachEdge : newEdges) {
            SimpleVertex a = cells.get(eachEdge[0]),
                    b = cells.get(eachEdge[1])
                            ;
            SimpleEdge eachResult = constructor.apply(a,b);
            eachResult.textProperty().set(String.valueOf(new Point2D(a.locationX().get(), b.locationY().get()).distance(b.locationX().get(), b.locationY().get())));
            edgeList.add(eachResult);
//            model.addEdge(eachResult);
        }
        //add the return-to-start

        edgeCategory.add(Collections.unmodifiableList(edgeList));
    }

    public void addTargetEdges(List<int[]> edges) {
        addEdges(targets, edges, (a, b) -> edgePool.retrieve(a, b, TARGET_STYLE_CLASSES));
    }

    public void addPredictionEdges(List<int[]> edges) {
        addEdges(predictions, edges, (a,b) -> edgePool.retrieve(a,b, PREDICTION_STYLE_CLASSES));
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

    public Region getGraphic() {
        return root;
    }

    public void beginUpdate() {
//        fxGraph.getModel().getRemovedCells().addAll(cells);
//        Stream.concat(
//                Stream.of(normalEdges),
//                Stream.of(
//                        targets.stream(),
//                        predictions.stream()
//                ).flatMap(Function.identity())
//        )
//                .forEach(
//                        fxGraph.getModel()
//                                .getRemovedEdges()
//                                ::addAll
//                );
//        predictions.forEach(
//                fxGraph.getModel().getRemovedEdges()::addAll
//        );
    }

    public void endUpdate() {

        fxGraph.beginUpdate();
//        fxGraph.getModel().getRemovedCells().addAll(fxGraph.getModel().getAllCells()); //remove cells
        fxGraph.getModel().getAllCells().clear();
        fxGraph.getModel().getAllEdges().clear();
        fxGraph.getModel().getAddedEdges().addAll(
                Stream.of(
                        normalEdges.stream(),
                        Stream.concat(targets.stream(), predictions.stream())
                                .flatMap(Collection::stream)
                )
                        .flatMap(Function.identity())
                        .collect(Collectors.toList())
        );
        fxGraph.getModel().getAddedCells().addAll(cells);
        fxGraph.endUpdate();
    }
}
