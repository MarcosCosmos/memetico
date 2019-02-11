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
        public final int id;
        //todo: consider possibly using a thin solutionstructure here like for agent and algorithm?
        public final LightDiCycle pocket;          /* The "Pocket"  SolutionStructure    */
        public final LightDiCycle current;         /* The "Current" SolutionStructure    */
        //todo: possibly consider
        public AgentState(int id, DiCycle pocket, DiCycle current) {
            this.id = id;
            this.pocket = new LightDiCycle(pocket);
            this.current = new LightDiCycle(current);
        }
    }

    public final String instanceName;
    public final int generation;
    public final int nAry;
    /**
     * Measured in nanoseconds
     */
    public final long logTime;
    public final AgentState[] agents;

    /**
     * Note: the cost value (in the SolutionStructure class) is expected to be already computed for all srcs
     * @param src
     */
    public PCAlgorithmState(String instanceName, Population src, int generation, long logTime) {
        this.instanceName = instanceName;
        this.generation = generation;
        this.nAry = src.n_ary;
        this.logTime = logTime;
        this.agents = new AgentState[src.pop.length];
        for(int i=0; i<agents.length; ++i) {
            PocCurAgent each = (PocCurAgent)src.pop[i];
            agents[i] = new AgentState(i, (DiCycle)each.pocket, (DiCycle)each.current);
        }
    }
}
