package memetico;/*
 * File: PocCurAgent.java
 *
 * Date      Authors
 * 8/20/99   Luciana Buriol and Pablo Moscato
 *
 */

//package MemePool.AgentTypes;

/**
 * A class for representing an agent of the type
 * Pocket-Current. The agent contains two solutions of
 * the problem. The Pocket solution can be viewed as a
 * kind of memory, a good solution previously found.
 * The Current solution is the one being considered.
 * It also contains reference to the Guiding Function
 * value which, is this case, is the objective function
 * value.
 * </p>The <a href="../AgentTypes/PocCurAgent.java">source</a>.
 *
 * @author Luciana Buriol and Pablo Moscato
 **/

public class PocCurAgent extends Agent {
    public SolutionStructure pocket;          /* The "Pocket"  SolutionStructure    */
    public SolutionStructure current;         /* The "Current" SolutionStructure    */
    public int noChangeCounter = 0;


    public PocCurAgent(int size, int solutionStructureType) throws Exception {
        switch (solutionStructureType) {
            case SolutionStructure.DICYCLE_TYPE:
                current = new DiCycle(size);
                pocket = new DiCycle(size);
                break;
            default:
                throw new Exception("Invalid SolutionStructure Type");
        }
    }


    public boolean testValues(int values[][]) {
        if (!pocket.isSameValues(values) && !current.isSameValues(values)) return true;
        return false;
    }


    public double calculateCost(Instance inst) {
        pocket.calculateCost(inst);
        current.calculateCost(inst);

        super.avgCost = (pocket.cost + current.cost) / 2;
        super.cost = pocket.cost;

        if (pocket.cost < current.cost)
            super.bestCost = pocket.cost;
        else
            super.bestCost = current.cost;
        return super.bestCost;
    }

    public boolean isNewSolutionStructure(double cost) {

        boolean result = true;

        if (pocket.cost == cost || current.cost == cost) {
            if (pocket.cost == cost) {
                for (int i = 0; i < 5; i++) {

                }
            } else {
            }
            result = false;
        }
        return result;
    }

    /*
      Given a new SolutionStructure The it inserts it into Pocket Current agent
    */
    public void insertSolutionStructure(SolutionStructure child) {
        current = child;

        if (pocket.cost < current.cost)
            super.bestCost = pocket.cost;
        else
            super.bestCost = current.cost;

    }

// if the current solution is better than the pocket solution
// they switch places. In the TSP since the objective function is
// just the length of the SolutionStructure, we just check the ``Cost'' field.
// For multiobjective optimization this must be changed to
// some kind of dominance criteria (Pareto optimality, for instance).

    // In this implementation, we use Cost to update the pockets, in
// general we should refer to a certain guiding function, not necessary
// identical to the objective function.
    public void updateAgent(Instance inst) {
        SolutionStructure auxSolutionStructure;
        long auxCost;
        int j, i;

        // we calculate the cost of each current solution
        current.calculateCost(inst);

        //updates Agent's avgCost
        super.avgCost = (current.cost + pocket.cost) / 2;

        // if the the Current solution is strictly better than the Pocket
        if (current.cost < pocket.cost) {
            auxSolutionStructure = pocket;        // a typical triangulation
            pocket = current;       // Pocket and Current
            current = auxSolutionStructure;    // places
            super.cost = pocket.cost;   //updates agent cost
            super.bestCost = pocket.cost;
            // we reset the counter
            noChangeCounter = 1;
        } else
            noChangeCounter++;
    }

    public void exchangeSolutionStructures(Agent source) {
        PocCurAgent pcSource = (PocCurAgent) source;
        SolutionStructure auxSolutionStructure;

        auxSolutionStructure = pocket;
        pocket = pcSource.pocket;
        pcSource.pocket = auxSolutionStructure;
        //updates Agent�s cost
        pcSource.cost = pcSource.pocket.cost;
        super.cost = pocket.cost;
        //updates Agent�s bestCost
        if (pcSource.pocket.cost < pcSource.current.cost)
            pcSource.bestCost = pcSource.pocket.cost;
        else
            pcSource.bestCost = pcSource.current.cost;

        if (pocket.cost < current.cost)
            super.bestCost = pocket.cost;
        else
            super.bestCost = current.cost;
        //updates Agent�s avgCost
        pcSource.avgCost = (pcSource.current.cost + pcSource.pocket.cost) / 2;
        super.avgCost = (current.cost + pocket.cost) / 2;
    }
}// end of classPocCurAgent
