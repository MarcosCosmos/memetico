package memetico.logging;

import memetico.Arc;
import memetico.DiCycle;
import memetico.PocCurAgent;
import memetico.Population;

/**
 * Takes log-safe information from data provided by memetico;
 * It is important to note that it is only truely safe in so far as no attempts are made to interiorly modify pocket/current.
 */
public class MemeticoSnapshot {
    public static class LightTour {
        public int[] tour;
        public double cost;
        public LightTour(DiCycle src) {
            this.tour = new int[src.arcArray.length];
            int city = 0;
            int i=0;
            do {
                tour[i++] = city;
                city = src.arcArray[city].tip;
            } while (city != 0);
            this.cost = src.cost;
        }
    }

    public static class AgentSnapshot {
        public final int id;
        //todo: consider possibly using a thin solutionstructure here like for agent and algorithm?
        public final LightTour pocket;          /* The "Pocket"  SolutionStructure    */
        public final LightTour current;         /* The "Current" SolutionStructure    */
        //todo: possibly consider
        public AgentSnapshot(int id, DiCycle pocket, DiCycle current) {
            this.id = id;
            this.pocket = new LightTour(pocket);
            this.current = new LightTour(current);
        }
    }

    public final String instanceName;
    public final int generation;
    public final int nAry;
    /**
     * Measured in nanoseconds
     */
    public final long logTime;
    public final AgentSnapshot[] agents;

    /**
     * Note: the cost value (in the SolutionStructure class) is expected to be already computed for all srcs
     * @param src
     */
    public MemeticoSnapshot(String instanceName, Population src, int generation, long logTime) {
        this.instanceName = instanceName;
        this.generation = generation;
        this.nAry = src.n_ary;
        this.logTime = logTime;
        this.agents = new AgentSnapshot[src.pop.length];
        for(int i=0; i<agents.length; ++i) {
            PocCurAgent each = (PocCurAgent)src.pop[i];
            agents[i] = new AgentSnapshot(i, (DiCycle)each.pocket, (DiCycle)each.current);
        }
    }
}
