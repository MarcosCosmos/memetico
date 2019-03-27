package fxgraph;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.Model;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marcos.uon.tspaidemo.util.tsplib.TSPInstance;
import org.marcos.uon.tspaidemo.util.tsplib.Tour;
import org.marcos.uon.tspaidemo.util.tsplib.graph.Edge;
import org.marcos.uon.tspaidemo.util.tsplib.graph.NodeCoordinates;
import org.marcos.uon.tspaidemo.fxgraph.SelfLocatingLayout;
import org.marcos.uon.tspaidemo.fxgraph.SimpleEdge;
import org.marcos.uon.tspaidemo.fxgraph.SimpleVertex;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestFXGraphTSPLIB extends Application {
    TSPInstance theInstance;
    Graph graph = new Graph();
    List<SimpleVertex> cells;
    List<List<SimpleEdge>> tours;
    List<SimpleEdge> normalEdges;
    @Override
    public void start(Stage primaryStage) throws Exception {
        final BorderPane root = new BorderPane();

        graph = new Graph();
        tours = new ArrayList<>();
        normalEdges = new ArrayList<>();
        graph.getUseNodeGestures().setValue(false);

        root.setCenter(graph.getCanvas());

        final Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        loadInstance();

        //may be to consider spacing for specific cells etc in a more complex way instead
        //scale the instance so it's big enough for the text
//        for(SimpleVertex each : cells) {
//            each.locationX().set(each.locationX().get()*10);
//            each.locationY().set(each.locationY().get()*10);
//        }

        for(SimpleEdge eachCell : normalEdges) {
            graph.getGraphic(eachCell).getStyleClass().add("suboptimal");
        }
        for(List<SimpleEdge> eachTour : tours) {
            for(SimpleEdge eachCell : eachTour) {
                graph.getGraphic(eachCell).getStyleClass().add("optimal");
                graph.getGraphic(eachCell).getStyleClass().remove("suboptimal");
            }
        }


        graph.layout(new SelfLocatingLayout());
        root.getStyleClass().add("graph");

    }

    public void loadInstance() throws IOException {
        theInstance = new TSPInstance();
        theInstance.load(new File("/home/marcos/Downloads/all_tsplib/berlin52.tsp"));
        theInstance.addTour(new File("/home/marcos/Downloads/all_tsplib/berlin52.opt.tour"));
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

        final Model model = graph.getModel();

//        graph.beginUpdate(); //only one of beginUpdate/endUpdate actually need to be called; begin update should probably never be called since it just wipes the canvas without changing other lists?

        cells = Stream.generate(() -> (SimpleVertex)null)
                .limit(nodeData.size())
                .collect(Collectors.toList());

        //stroke the dots ontop of the edges
        for(int eachIndex : nodeData.listNodes()) {
            org.marcos.uon.tspaidemo.util.tsplib.graph.Node eachNode = nodeData.get(eachIndex);
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
            List<SimpleEdge> eachEdgeList = new ArrayList<>();
            for(Edge eachEdge : eachTour.toEdges()) {
                int _a = eachEdge.getId1()-1;
                int _b = eachEdge.getId2()-1;
                options.remove(Arrays.asList(_a, _b));
                SimpleVertex a = cells.get(_a),
                    b = cells.get(_b)
                ;
                SimpleEdge eachResult = new SimpleEdge(a,b);
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

        graph.endUpdate();
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
