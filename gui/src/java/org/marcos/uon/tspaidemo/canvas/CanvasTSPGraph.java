package org.marcos.uon.tspaidemo.canvas;

import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.geometry.BoundingBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.graph.NodeCoordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CanvasTSPGraph {
    private CanvasGraph internalGraphic;
    private List<Edge> normalEdges;
    private List<List<Edge>> targets;
    private List<List<Edge>> predictions;
    public static final double DEFAULT_DOT_RADIUS = 3;
    public static final Color DEFAULT_DOT_COLOR = Color.RED;
    public static final Color DEFAULT_EDGE_COLOR = Color.BLACK;
    public static final Color DEFAULT_TARGET_EDGE_COLOR = Color.LIME;
    public static final Color DEFAULT_PREDICTION_COLOR = DEFAULT_EDGE_COLOR;
    public static final Color DEFAULT_LABEL_COLOR = null;

    /**
     *
     * @param instance A tsp instance,with main data already loaded from file;
     * @throws InvalidArgumentException if the instance is not Euclidean 2D
     */
    public CanvasTSPGraph(TSPLibInstance instance) throws InvalidArgumentException {
        internalGraphic = new CanvasGraph();
        normalEdges = new ArrayList<>();
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


        //add the vertices after
        List<Vertex> vertices = internalGraphic.getVertices();
        for(int eachIndex : nodeData.listNodes()) {
            org.jorlib.io.tspLibReader.graph.Node eachNode = nodeData.get(eachIndex);
            double[] eachPos = eachNode.getPosition();
            vertices.add(new Vertex(eachPos[0], eachPos[1], DEFAULT_DOT_RADIUS, "C"+String.valueOf(eachIndex), DEFAULT_DOT_COLOR, DEFAULT_LABEL_COLOR));
        }

        if(instance.getFixedEdges() != null) {
            for(org.jorlib.io.tspLibReader.graph.Edge each : instance.getFixedEdges().getEdges()) {
                Vertex a = vertices.get(each.getId1()),
                        b = vertices.get(each.getId2())
                                ;
                normalEdges.add(new Edge(vertices.get(each.getId1()), vertices.get(each.getId2()), String.valueOf(a.getLocation().distance(b.getLocation())), DEFAULT_EDGE_COLOR, DEFAULT_LABEL_COLOR));
            }
        }
    }

    /**
     * Determines whether or not there is anything to display
     * @return
     */
    public boolean isEmpty() {
        return internalGraphic.getVertices().isEmpty();
    }

    private void clearEdges(List<List<Edge>> edgeCategory) {
        edgeCategory.clear();
    }

    public void clearTargets() {
        clearEdges(targets);
    }

    public void clearPredictions() {
        clearEdges(predictions);
    }

    private void addEdges(List<List<Edge>> edgeCategory, List<int[]> newEdges, Color strokeColour) {
        List<Vertex> vertices = internalGraphic.getVertices();
        List<Edge> edgeList = new ArrayList<>();
        for(int[] eachEdge : newEdges) {
            Vertex a = vertices.get(eachEdge[0]),
                    b = vertices.get(eachEdge[1])
                            ;
            Edge eachResult = new Edge(a,b, String.valueOf(a.getLocation().distance(b.getLocation())), strokeColour, DEFAULT_LABEL_COLOR);
            edgeList.add(eachResult);
        }
        edgeCategory.add(Collections.unmodifiableList(edgeList));
    }

    public void addTargetEdges(List<int[]> edges) {
        addEdges(targets, edges, DEFAULT_TARGET_EDGE_COLOR);
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
        double minX=Double.POSITIVE_INFINITY, minY=Double.POSITIVE_INFINITY, maxX=Double.NEGATIVE_INFINITY, maxY=Double.NEGATIVE_INFINITY;
        for(Vertex each : internalGraphic.getVertices()) {
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
        List<Edge> edgesToUse = internalGraphic.getEdges();
        edgesToUse.clear();
        edgesToUse.addAll(normalEdges);
        targets.forEach(edgesToUse::addAll);
        predictions.forEach(edgesToUse::addAll);
        internalGraphic.draw();
    }

    public double getScale() {
        return internalGraphic.getScale();
    }

    public void setScale(double scale) {
        internalGraphic.setScale(scale);
    }
}
