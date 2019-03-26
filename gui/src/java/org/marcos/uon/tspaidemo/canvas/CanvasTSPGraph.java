package org.marcos.uon.tspaidemo.canvas;

import javafx.geometry.Bounds;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;
import org.marcos.uon.tspaidemo.canvas.drawable.Edge;
import org.marcos.uon.tspaidemo.canvas.drawable.OutlineEdge;
import org.marcos.uon.tspaidemo.canvas.drawable.Vertex;
import org.marcos.uon.tspaidemo.canvas.layer.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CanvasTSPGraph {

    private LayeredCanvas internalGraphic;
    private VertexLayer vertexLayer;
    private ListLayer<OutlineEdge> targetLayer;
    private ListLayer<Edge> predictionLayer;

    private ViewportGestures gestures;

    public static final double DEFAULT_DOT_RADIUS = 2;
    public static final double DEFAULT_STROKE_WIDTH = 1;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.web("#26262b");
    public static final Color DEFAULT_DOT_FILL = DEFAULT_BACKGROUND_COLOR;
    public static final Color DEFAULT_DOT_STROKE = Color.WHITE;
    public static final Color DEFAULT_EDGE_COLOR = Color.WHITE;
    public static final Color DEFAULT_TARGET_EDGE_COLOR = Color.LIME;
    public static final Color DEFAULT_PREDICTION_COLOR = DEFAULT_EDGE_COLOR;
    public static final Color DEFAULT_LABEL_COLOR = null;

    public CanvasTSPGraph() {
        internalGraphic = new LayeredCanvas();
        internalGraphic.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
        //layer showing edges explicitly listed for the intstance? ("fixed edges"?)
        predictionLayer = new ListLayer<>(0);
        targetLayer = new ListLayer<>(50);
        vertexLayer = new VertexLayer(100);
        internalGraphic.getLayers().addAll(vertexLayer, targetLayer, predictionLayer);
        TransformationContext transformationContext = getDragContext();
        transformationContext.logicalBoundsProperty().bind(vertexLayer.logicalBoundsProperty());
        transformationContext.boundsInLocalProperty().bind(vertexLayer.boundsInLocalProperty());
        gestures = new ViewportGestures(transformationContext);
//        internalGraphic.getLayers().addAll(new LogicalBounds(200), new BoundsInLocal(300), new BoundsInCanvas(400), new CanvasBounds(500));
    }

    /**
     * Determines whether or not there is anything to display
     * @return
     */
    public boolean isEmpty() {
        return vertexLayer.isEmpty();
    }

    /**
     * Replaces the set of vertices
     * @param newVertices
     */
    public void setVertices(List<double[]> newVertices) {
        vertexLayer.clear();
        for (int i = 0; i < newVertices.size(); i++) {
            double[] eachCoords = newVertices.get(i);
            //invert the y-axis
            vertexLayer.add(new Vertex(eachCoords[0], -eachCoords[1], DEFAULT_DOT_RADIUS, DEFAULT_STROKE_WIDTH, "C"+i, DEFAULT_DOT_FILL, DEFAULT_DOT_STROKE, DEFAULT_LABEL_COLOR));
        }
        vertexLayer.requestRedraw();
    }

    public void clearTargets() {
        targetLayer.clear();
        targetLayer.requestRedraw();
    }

    public void clearPredictions() {
        predictionLayer.clear();
        predictionLayer.requestRedraw();
    }

    public void clearInstance() {
        vertexLayer.clear();
        vertexLayer.requestRedraw();
        clearTargets();
        clearPredictions();
    }

    public void addTargetEdges(List<int[]> edges) {
        for (int i = 0; i < edges.size(); i++) {
            int[] eachEdge = edges.get(i);
            Vertex a = vertexLayer.get(eachEdge[0]),
                    b = vertexLayer.get(eachEdge[1])
                            ;
            OutlineEdge eachResult = new OutlineEdge(a,b, String.valueOf(a.getLocation().distance(b.getLocation())), DEFAULT_TARGET_EDGE_COLOR, DEFAULT_LABEL_COLOR);
            targetLayer.add(eachResult);
        }
        targetLayer.requestRedraw();
    }

    public void addPredictionEdges(List<int[]> edges, Color stroke) {
        for (int i = 0; i < edges.size(); i++) {
            int[] eachEdge = edges.get(i);
            Vertex a = vertexLayer.get(eachEdge[0]),
                    b = vertexLayer.get(eachEdge[1])
                            ;
            Edge eachResult = new Edge(a,b, String.valueOf(a.getLocation().distance(b.getLocation())), stroke, DEFAULT_LABEL_COLOR);
            predictionLayer.add(eachResult);
        }
        predictionLayer.requestRedraw();
    }

    public void addPredictionEdges(List<int[]> edges) {
        addPredictionEdges(edges, DEFAULT_PREDICTION_COLOR);
    }

    public Region getGraphic() {
        return internalGraphic;
    }

    /**
     * Returns the bounds as determined by the cells
     * @return
     */
    public Bounds getLogicalBounds() {
        return getDragContext().getLogicalBounds();
    }

    public void draw() {
        internalGraphic.draw();
    }

    public void requestRedraw() {
        internalGraphic.requestAllRedraw();
    }

    public void showTargets() {
        targetLayer.getCanvas().setVisible(true);
    }
    public void hideTargets() {
        targetLayer.getCanvas().setVisible(false);
    }

    /**
     * Reconfigures to the new solution (forgetting any existing predictions); If the instance isn't 2d, this will be empty
     * @param instance A tsp instance,with main data already loaded from file
     */
    public void applyInstance(TSPLibInstance instance) {
        clearTargets();
        clearPredictions();
        final NodeCoordinates nodeData;
        switch (instance.getDisplayDataType()) {
            case TWOD_DISPLAY:
                nodeData = instance.getDisplayData();
                break;
            case COORD_DISPLAY:
                nodeData = (NodeCoordinates) instance.getDistanceTable();
                //only try to display 2d nodes for now; maybe use jgrapht for more?
                if(nodeData.get(nodeData.listNodes()[0]).getPosition().length == 2) {
                    break;
                }
            default:
                return;
        }

        setVertices(Arrays.stream(nodeData.listNodes()).mapToObj(i -> nodeData.get(i).getPosition()).collect(Collectors.toList()));

//        //clip the logical bounds to remove excess min x/y
//        Bounds logicalBounds = getLogicalBounds();
//        for (Vertex each: vertexLayer) {
//            each.setLocation(each.getLocation().subtract(logicalBounds.getMinX(), logicalBounds.getMinY()));
//        }
        vertexLayer.requestRedraw();

        //add targets
        addTargetEdges(
                instance.getTours()
                        .stream()
                        .flatMap(
                                eachTour -> eachTour.toEdges()
                                        .stream()
                                        .map(eachEdge -> new int[]{eachEdge.getId1(), eachEdge.getId2()})
                        )
                        .collect(Collectors.toList())
        );

        getDragContext().setTransformAutomatically(true);
    }

    public TransformationContext getDragContext() {
        return internalGraphic.getTransformationContext();
    }

    public ViewportGestures getGestures() {
        return gestures;
    }
}
