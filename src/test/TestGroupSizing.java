package jfx;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import org.jorlib.io.tspLibReader.TSPInstance;
import org.jorlib.io.tspLibReader.Tour;
import org.jorlib.io.tspLibReader.graph.Edge;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;

import java.io.File;

/**
 * Want to know if group sizing is affected by the position of shapes with the group, and further, how that effects display is say a scrollpane
 */
public class TestGroupSizing extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(new Group(), 300,300));
        primaryStage.show();



        int done = 0;
    }
}
