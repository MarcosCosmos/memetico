package memetico.logging;

import memetico.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Takes log-safe information from data provided by memetico;
 * It is important to note that it is only truely safe in so far as no attempts are made to interiorly modify pocket/current.
 */
public class PCAlgorithmState {
    public static class LightDiCycle {
        public Arc arcArray[];
        public double cost;
        public LightDiCycle(DiCycle src) {
            this.arcArray = src.arcArray;
            this.cost = src.cost;
        }
    }

    public static class AgentState {
        //todo: consider possibly using a thin solutionstructure here like for agent and algorithm?
        public final LightDiCycle pocket;          /* The "Pocket"  base.SolutionStructure    */
        public final LightDiCycle current;         /* The "Current" base.SolutionStructure    */
        //todo: possibly consider
        public AgentState(DiCycle pocket, DiCycle current) {
            this.pocket = new LightDiCycle(pocket);
            this.current = new LightDiCycle(current);
        }
    }

    public final String instanceName;
    public final int generation;
    public final int nAry;
    public final double logTime;
    public final AgentState[] agents;

    /**
     * Note: the cost value (in the SolutionStructure class) is expected to be already computed for all srcs
     * @param src
     */
    public PCAlgorithmState(String instanceName, Population src, int generation, double logTime) {
        this.instanceName = instanceName;
        this.generation = generation;
        this.nAry = src.n_ary;
        this.logTime = logTime;
        this.agents = Arrays.stream(src.pop).map(
                each -> {
                    PocCurAgent _each = (PocCurAgent)each;
                    return new AgentState((DiCycle)_each.pocket, (DiCycle)_each.current);
                }
        ).toArray(AgentState[]::new);
    }
}
