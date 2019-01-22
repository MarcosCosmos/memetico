package fxgraph;
import com.fxgraph.edges.CorneredEdge;
import com.fxgraph.edges.DoubleCorneredEdge;
import com.fxgraph.edges.Edge;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.layout.AbegoTreeLayout;
import com.fxgraph.layout.RandomLayout;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.abego.treelayout.Configuration;
import org.marcos.uon.tspaidemo.fxgraph.SimpleVertexCell;

public class TestVertexCell extends Application {

    Graph graph = new Graph();

    @Override
    public void start(Stage primaryStage) {
        final BorderPane root = new BorderPane();

        graph = new Graph();
        graph.getUseNodeGestures().setValue(false);

        root.setCenter(graph.getCanvas());

        final Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        addTreeComponents();
        graph.layout(new AbegoTreeLayout(200, 200, Configuration.Location.Top));
    }

    @SuppressWarnings("unused")
    private void addGraphComponents() {

        final Model model = graph.getModel();

        graph.beginUpdate();

        final ICell cellA = new SimpleVertexCell();
        final ICell cellB = new SimpleVertexCell();
        final ICell cellC = new SimpleVertexCell();
        final ICell cellD = new SimpleVertexCell();
        final ICell cellE = new SimpleVertexCell();
        final ICell cellF = new SimpleVertexCell();
        final ICell cellG = new SimpleVertexCell();

        model.addCell(cellA);
        model.addCell(cellB);
        model.addCell(cellC);
        model.addCell(cellD);
        model.addCell(cellE);
        model.addCell(cellF);
        model.addCell(cellG);

        model.addEdge(cellA, cellB);
        model.addEdge(cellA, cellC);
        model.addEdge(cellB, cellC);
        model.addEdge(cellC, cellD);
        model.addEdge(cellB, cellE);
        model.addEdge(cellD, cellF);

        final Edge edge = new Edge(cellD, cellG);
        edge.textProperty().set("Edges can have text too!");
        model.addEdge(edge);

        graph.endUpdate();

        graph.layout(new RandomLayout());
    }

    @SuppressWarnings("unused")
    private void addTreeComponents() {

        final Model model = graph.getModel();

        graph.beginUpdate();

        final ICell cellA = new SimpleVertexCell();
        final ICell cellB = new SimpleVertexCell();
        final ICell cellC = new SimpleVertexCell();
        final ICell cellD = new SimpleVertexCell();
        final ICell cellE = new SimpleVertexCell();
        final ICell cellF = new SimpleVertexCell();
        final ICell cellG = new SimpleVertexCell();

        model.addCell(cellA);
        model.addCell(cellB);
        model.addCell(cellC);
        model.addCell(cellD);
        model.addCell(cellE);
        model.addCell(cellF);
        model.addCell(cellG);

        final Edge edgeAB = new Edge(cellA, cellB);
        edgeAB.textProperty().set("Edges can have text too!");
        model.addEdge(edgeAB);
        final CorneredEdge edgeAC = new CorneredEdge(cellA, cellC, Orientation.HORIZONTAL);
        edgeAC.textProperty().set("Edges can have corners too!");
        model.addEdge(edgeAC);
        model.addEdge(cellB, cellD);
        final DoubleCorneredEdge edgeBE = new DoubleCorneredEdge(cellB, cellE, Orientation.HORIZONTAL);
        edgeBE.textProperty().set("You can implement custom edges and nodes too!");
        model.addEdge(edgeBE);
        model.addEdge(cellC, cellF);
        model.addEdge(cellC, cellG);

        graph.endUpdate();
    }

    public static void main(String[] args) {
        launch(args);
    }
}