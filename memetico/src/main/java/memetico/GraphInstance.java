package memetico;

import java.lang.String;

public abstract class GraphInstance extends Instance {
    // Constants used for instance classes
    final protected static String DIMENSION = "DIMENSION";
    final protected static String MATRIX = "EDGE_WEIGHT_SECTION";

    final static int NONE = -1;
    final static int TSP_TYPE = 0;
    final static int ATSP_TYPE = 1;
    final static int HCP_TYPE = 2;
    final static int DHCP_TYPE = 3;

    //Variable declaration
    int graphType = NONE;

    double matDist[][] = null;

    public GraphInstance() {
        super.problemType = super.GRAPH_TYPE;
    }

    public void setDimension(int dim) {
        dimension = dim;
        matDist = new double[dimension][dimension];
    }

    public double[][] getMatDist() {
        return matDist;
    }

    public void setMatDist(double[][] matDist) {
        this.matDist = matDist;
    }
}/*end of Graph Instance class*/