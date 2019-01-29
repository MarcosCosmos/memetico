package memetico;

public abstract class Instance {
    public final static int NONE = -1;
    public final static int GRAPH_TYPE = 0;

    public int problemType = NONE;
    public int dimension;

    abstract void readInstance(String fileName) throws Exception;

    abstract void setDimension(int dim);


}
