package org.marcos.uon.tspaidemo.canvas;

import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.geometry.BoundingBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.TSPLibTour;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CanvasTSPGraph {
    private CanvasGraph internalGraphic;
    private List<List<Edge>> targets;
    private List<List<Edge>> predictions;

    private CanvasGraph.VertexLayer vertexLayer;
    private CanvasGraph.EdgeLayer normalEdgeLayer;
    private CanvasGraph.EdgeLayer targetLayer;
    private CanvasGraph.EdgeLayer predictionLayer;

    public static final double DEFAULT_DOT_RADIUS = 3;
    public static final Color DEFAULT_DOT_COLOR = Color.BLACK;
    public static final Color DEFAULT_EDGE_COLOR = Color.BLACK;
    public static final Color DEFAULT_TARGET_EDGE_COLOR = Color.LIME;
    public static final Color DEFAULT_PREDICTION_COLOR = DEFAULT_EDGE_COLOR;
    public static final Color DEFAULT_LABEL_COLOR = null;

    private boolean showTargets;

    /**
     *
     * @param instance A tsp instance,with main data already loaded from file;
     * @throws InvalidArgumentException if the instance is not Euclidean 2D
     */
    public CanvasTSPGraph(TSPLibInstance instance) throws InvalidArgumentException {
        showTargets = true;
        internalGraphic = new CanvasGraph();

        vertexLayer = internalGraphic.addVertexLayer(100);
        normalEdgeLayer = internalGraphic.addEdgeLayer(0);
        targetLayer = internalGraphic.addEdgeLayer(1);
        predictionLayer = internalGraphic.addEdgeLayer(2);

        targets = new ArrayList<>();
        predictions = new ArrayList<>();

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
                return;
        }

        for(int eachIndex : nodeData.listNodes()) {
            org.jorlib.io.tspLibReader.graph.Node eachNode = nodeData.get(eachIndex);
            double[] eachPos = eachNode.getPosition();
            vertexLayer.add(new Vertex(eachPos[0], eachPos[1], DEFAULT_DOT_RADIUS, "C"+String.valueOf(eachIndex), DEFAULT_DOT_COLOR, DEFAULT_LABEL_COLOR));
        }

        //clip the logical bounds to remove excess min x/y
        BoundingBox logicalBounds = getLogicalBounds();
        for (Vertex each: vertexLayer) {
            each.setLocation(each.getLocation().subtract(logicalBounds.getMinX(), logicalBounds.getMinY()));
        }
        vertexLayer.requestRedraw();

        if(instance.getFixedEdges() != null) {
            for(org.jorlib.io.tspLibReader.graph.Edge each : instance.getFixedEdges().getEdges()) {
                Vertex a = vertexLayer.get(each.getId1()),
                        b = vertexLayer.get(each.getId2())
                                ;
                Edge coreEdge = new Edge(vertexLayer.get(each.getId1()), vertexLayer.get(each.getId2()), String.valueOf(a.getLocation().distance(b.getLocation())), internalGraphic.getBackgroundColor(), DEFAULT_LABEL_COLOR);
                Edge outlineEdge = new Edge(coreEdge.getA(), coreEdge.getB(), coreEdge.getLabel(), DEFAULT_EDGE_COLOR, DEFAULT_LABEL_COLOR, 2*coreEdge.getLineWidth());
                normalEdgeLayer.add(outlineEdge);
                normalEdgeLayer.add(coreEdge);
            }
        }
        normalEdgeLayer.requestRedraw();

        //add targets
        for(TSPLibTour eachTour : instance.getTours()) {
            List<int[]> edges = eachTour.toEdges()
                    .stream()
                    .map(each -> new int[]{each.getId1(), each.getId2()})
                    .collect(Collectors.toList());
            addTargetEdges(
                    edges
            );
        }


    }

    /**
     * Determines whether or not there is anything to display
     * @return
     */
    public boolean isEmpty() {
        return vertexLayer.isEmpty();
    }

    private void clearEdges(List<List<Edge>> edgeCategory) {
        edgeCategory.clear();
    }

    public void clearTargets() {
        clearEdges(targets);
        targetLayer.clear();
        targetLayer.requestRedraw();
    }

    public void clearPredictions() {
        clearEdges(predictions);
        predictionLayer.clear();
        predictionLayer.requestRedraw();
    }

    private void addEdges(List<List<Edge>> edgeCategory, List<int[]> newEdges, Color strokeColour) {
        List<Edge> edgeList = new ArrayList<>();
        for(int[] eachEdge : newEdges) {
            Vertex a = vertexLayer.get(eachEdge[0]),
                    b = vertexLayer.get(eachEdge[1])
                            ;
            Edge eachResult = new Edge(a,b, String.valueOf(a.getLocation().distance(b.getLocation())), strokeColour, DEFAULT_LABEL_COLOR);
            edgeList.add(eachResult);
        }
        edgeCategory.add(Collections.unmodifiableList(edgeList));
    }

    public void addTargetEdges(List<int[]> edges) {
        addEdges(targets, edges, DEFAULT_TARGET_EDGE_COLOR);
        List<Edge> edgeList = new ArrayList<>();
        for(int[] eachEdge : edges) {
            Vertex a = vertexLayer.get(eachEdge[0]),
                    b = vertexLayer.get(eachEdge[1])
                            ;
            Edge coreEdge = new Edge(a,b, String.valueOf(a.getLocation().distance(b.getLocation())), internalGraphic.getBackgroundColor(), DEFAULT_LABEL_COLOR);
            Edge outlineEdge = new Edge(coreEdge.getA(), coreEdge.getB(), coreEdge.getLabel(), DEFAULT_EDGE_COLOR, DEFAULT_LABEL_COLOR, 2*coreEdge.getLineWidth());
            edgeList.add(coreEdge);
            targetLayer.add(outlineEdge);
            targetLayer.add(coreEdge);
        }
        targets.add(edgeList);
        targetLayer.requestRedraw();
    }

    public void addPredictionEdges(List<int[]> edges, Color stroke) {
        List<Edge> edgeList = new ArrayList<>();
        for(int[] eachEdge : edges) {
            Vertex a = vertexLayer.get(eachEdge[0]),
                    b = vertexLayer.get(eachEdge[1])
                            ;
            Edge eachResult = new Edge(a,b, String.valueOf(a.getLocation().distance(b.getLocation())), stroke, DEFAULT_LABEL_COLOR);
            edgeList.add(eachResult);
            predictionLayer.add(eachResult);
        }
        predictions.add(edgeList);
        predictionLayer.requestRedraw();
    }

    public void addPredictionEdges(List<int[]> edges) {
        addEdges(predictions, edges, DEFAULT_PREDICTION_COLOR);
    }

    public Region getGraphic() {
        return internalGraphic;
    }

    /**
     * Returns the bounds as determined by the cells
     * @return
     */
    public BoundingBox getLogicalBounds() {
//        (todo: cater for text in the bounds check?)
        double minX=Double.POSITIVE_INFINITY, minY=Double.POSITIVE_INFINITY, maxX=Double.NEGATIVE_INFINITY, maxY=Double.NEGATIVE_INFINITY;
        for(Vertex each : vertexLayer) {
            double x = each.getLocation().getX();
            double y = each.getLocation().getY();
            double radius = each.getDotRadius();
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

    public void draw() {
        internalGraphic.draw();
    }

    public double getScale() {
        return internalGraphic.getScale();
    }

    public void setScale(double scale) {
        internalGraphic.setScale(scale);
    }

    public void showTargets() {
        targetLayer.getCanvas().setVisible(false);
    }
    public void hideTargets() {
        targetLayer.getCanvas().setVisible(true);
    }
}
