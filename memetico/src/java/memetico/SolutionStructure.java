package memetico;

public abstract class SolutionStructure {

    //Constants section
    public final static int NONE = -1;
    public final static int DICYCLE_TYPE = 0;

    //Variables section
    public double cost;
    public int size;

    public static int solutionStructureType = NONE;

    //Methods section
    public abstract double calculateCost(Instance inst);

    public abstract boolean isSameValues(int values[][]);

    public abstract SolutionStructure deepCopy();
}