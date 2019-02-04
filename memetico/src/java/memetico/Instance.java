package memetico;

public abstract class Instance {
    public final static int NONE = -1;
    public final static int GRAPH_TYPE = 0;

    int problemType = NONE;
    int dimension;

    abstract void readInstance(String fileName) throws Exception;

    public abstract void setDimension(int dim);

    public int getDimension() {
        return dimension;
    }

    public int getProblemType() {
        return problemType;
    }
}
