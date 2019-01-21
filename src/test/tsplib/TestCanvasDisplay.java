package tsplib;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import org.jorlib.io.tspLibReader.TSPInstance;
import org.jorlib.io.tspLibReader.Tour;
import org.jorlib.io.tspLibReader.fieldTypesAndFormats.DisplayDataType;
import org.jorlib.io.tspLibReader.graph.DistanceTable;
import org.jorlib.io.tspLibReader.graph.Edge;
import org.jorlib.io.tspLibReader.graph.Node;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;

import java.io.File;
import java.io.IOException;

/**
 * Simple test that draws the nodes and edges of the graph on a simple canvas
 * Note that this only works (currently) for 2d graphs
 */
public class TestCanvasDisplay extends Application{
    TSPInstance theInstance;
    @Override
    public void start(Stage primaryStage) throws Exception {
        theInstance = new TSPInstance();
        theInstance.load(new File("/home/marcos/Downloads/ALL_tsp/a280.tsp"));
        theInstance.addTour(new File("/home/marcos/Downloads/ALL_tsp/a280.opt.tour"));
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
                primaryStage.setScene(new Scene(new Group(new Text("Could not display instance"))));
                return;
        }

        Group graphDisplay = new Group();
        ObservableList<javafx.scene.Node> elements = graphDisplay.getChildren();
        for(Tour eachTour : this.theInstance.getTours()) {
            for(Edge eachEdge : eachTour.toEdges()) {
                double[] from = nodeData.get(eachEdge.getId1()-1).getPosition();
                double[] to = nodeData.get(eachEdge.getId2()-1).getPosition();
                Line eachLine = new Line(from[0], from[1], to[0], to[1]);
                eachLine.setStroke(Color.GREEN);
                eachLine.setFill(null);
                elements.add(eachLine);
            }
        }

        //stroke the dots ontop of the edges
        for(int eachIndex : nodeData.listNodes()) {
            Node eachNode = nodeData.get(eachIndex);
            double[] eachPos = eachNode.getPosition();
            elements.add(new Circle(eachPos[0], eachPos[1], 2, Color.DARKRED));
        }


        Transform scale = new Scale(10, 10);

        ScrollPane root = new ScrollPane(new Group(graphDisplay));

        root.setPannable(true);
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        primaryStage.setScene(new Scene(root, 300,300));
        primaryStage.show();



        int done = 0;
    }
    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
