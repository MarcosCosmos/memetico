package org.marcos.uon.tspaidemo.canvas;

import javafx.geometry.Bounds;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CanvasTSPGraph {

    private CanvasGraph internalGraphic;
    private CanvasGraph.VertexLayer vertexLayer;
    private CanvasGraph.OutlineEdgeLayer targetLayer;
    private CanvasGraph.EdgeLayer predictionLayer;

    private ViewportGestures gestures;

    public static final double DEFAULT_DOT_RADIUS = 3;
    public static final double DEFAULT_STROKE_WIDTH = 1;
    public static final Color DEFAULT_BACKGROUND_COLOR = Color.BLACK;
//    public static final Color DEFAULT_DOT_FILL = Color.BLACK;
//    public static final Color DEFAULT_EDGE_COLOR = Color.BLACK;
//    public static final Color DEFAULT_TARGET_EDGE_COLOR = Color.LIME;
//    public static final Color DEFAULT_PREDICTION_COLOR = DEFAULT_EDGE_COLOR;
//    public static final Color DEFAULT_LABEL_COLOR = null;
//    public static final Color DEFAULT_DOT_FILL = Color.YELLOW;

    public static final Color DEFAULT_DOT_FILL = DEFAULT_BACKGROUND_COLOR;
    public static final Color DEFAULT_DOT_STROKE = Color.WHITE;
    public static final Color DEFAULT_EDGE_COLOR = Color.WHITE;//Color.rgb(255, 255, 0);
    public static final Color DEFAULT_TARGET_EDGE_COLOR = Color.LIME;
    public static final Color DEFAULT_PREDICTION_COLOR = DEFAULT_EDGE_COLOR;
    public static final Color DEFAULT_LABEL_COLOR = null;
//    public static final Color DEFAULT_DOT_FILL = Color.WHITE;
//    public static final Color DEFAULT_EDGE_COLOR = Color.rgb(255, 255, 0);
//    public static final Color DEFAULT_TARGET_EDGE_COLOR = Color.LIME;
//    public static final Color DEFAULT_PREDICTION_COLOR = DEFAULT_EDGE_COLOR;
//    public static final Color DEFAULT_LABEL_COLOR = null;

    public CanvasTSPGraph() {
        internalGraphic = new CanvasGraph();
        internalGraphic.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
        //layer showing edges explicitly listed for the intstance? ("fixed edges"?)
        targetLayer = internalGraphic.addOutlineEdgeLayer(0);
        predictionLayer = internalGraphic.addEdgeLayer(10);
        vertexLayer = internalGraphic.addVertexLayer(100);
        DragContext dragContext = getDragContext();
        dragContext.boundsInLocalProperty().bind(vertexLayer.logicalBoundsProperty());
        gestures = new ViewportGestures(dragContext);
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
//
    private void addEdges(CanvasGraph.Layer<Edge> edgeCategory, List<int[]> newEdges, Color strokeColour) {
        for(int[] eachEdge : newEdges) {
            Vertex a = vertexLayer.get(eachEdge[0]),
                    b = vertexLayer.get(eachEdge[1])
                            ;
            Edge eachResult = new Edge(a,b, String.valueOf(a.getLocation().distance(b.getLocation())), strokeColour, DEFAULT_LABEL_COLOR);
            edgeCategory.add(eachResult);
        }
        edgeCategory.requestRedraw();
    }

    public void addTargetEdges(List<int[]> edges) {
        addEdges(targetLayer, edges, DEFAULT_TARGET_EDGE_COLOR);
    }

    public void addPredictionEdges(List<int[]> edges, Color stroke) {
        addEdges(predictionLayer, edges, stroke);
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
        return getDragContext().getBoundsInLocal();
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

        //clip the logical bounds to remove excess min x/y
        Bounds logicalBounds = getLogicalBounds();
        for (Vertex each: vertexLayer) {
            each.setLocation(each.getLocation().subtract(logicalBounds.getMinX(), logicalBounds.getMinY()));
        }
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

    public DragContext getDragContext() {
        return internalGraphic.getDragContext();
    }

    public ViewportGestures getGestures() {
        return gestures;
    }
}
