package memetico.logging;

import memetico.Instance;
import memetico.PocCurAgent;
import memetico.Population;
import memetico.SolutionStructure;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Takes log-safe information from data provided by memetico;
 * It is important to note that it is only truely safe in so far as no attempts are made to interiorly modify pocket/current.
 */
public class PCAlgorithmState {
    public static class AgentState {
        //todo: consider possibly using a thin solutionstructure here like for agent and algorithm?
        public final SolutionStructure pocket;          /* The "Pocket"  base.SolutionStructure    */
        public final SolutionStructure current;         /* The "Current" base.SolutionStructure    */
        //todo: possibly consider
        public AgentState(SolutionStructure pocket, SolutionStructure current) {
            this.pocket = pocket;
            this.current = current;
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
                    return new AgentState(_each.pocket, _each.current);
                }
        ).toArray(AgentState[]::new);
    }
}
