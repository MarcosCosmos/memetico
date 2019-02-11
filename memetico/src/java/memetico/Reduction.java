package memetico;

public abstract class Reduction {
    //Constant Declarations
    final public static int NONE = -1;
    final public static int DHCP_TO_ATSP = 0;
    final public static int HCP_To_ATSP = 1;

    //Variables declarations
    public int reductionType;

    //Methods declarations
    public abstract Instance runReduction(Instance inst);

}/*end of Instance class*/