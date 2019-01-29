package memetico;

public class Population {

    /*  VARIAVEIS CONSTANTES  */
    public int popSize,    /* Number of agents of the population */
            nrParents;  /* for a ternary tree topology, number of parent nodes */

    public Agent bestAgent,
            worstAgent;

    public int n_ary = 3, newBestSol = 0;

    public Agent pop[]; /* The population is declared as an array of agents */


    /* -------------------------------- Construtor -----------------------------*/
    public Population(Instance inst, int size, int agentType, int solutionStructureType) throws Exception {
        // we use a ternary tree of four levels (thus, with 40 agents), if
        // the instance size exceeds 1000 cities.
        // this it totally instance and problem dependent and should not be here...
        popSize = size;
        nrParents = (int) Math.floor(popSize / n_ary);

        //Get the type of the agent to be used in this population
        switch (agentType) {
            case Agent.POKET_CURRENT:
                pop = new PocCurAgent[popSize];
                break;
            default:
                System.out.println("Invalid base.Agent Type");
                System.exit(1);
                break;
        }


        for (int i = 0; i < popSize; i++) {
            switch (agentType) {
                case Agent.POKET_CURRENT:
                    pop[i] = new PocCurAgent(inst.dimension, solutionStructureType);
                    break;
                default:
                    throw new Exception("Invalid base.Agent Type");
            }
        }
    }

    /* ------------------------------------ EvaluatePop ------------------------------------*/
// This should be done differently, we should pass a base.Population index
// value of the population we would like to evaluate. Evaluating the
// population means assining a guiding function value to each agent
// in the population.

    // For NP problems, the guiding function is related with the objective
// function and is computable in polynomial time. For other type of
// problems, this may involve calling some other appropiate measure of
// quality. For instance, it may be the result of iteratively playing
// some sort of game (like when the task is to evolve winning strategies).
    public void evaluatePop(Instance inst) {
        double bestCost = Long.MAX_VALUE;

        for (int ind = 0; ind < popSize; ind++) {
            pop[ind].calculateCost(inst);

            if (pop[ind].bestCost < bestCost) {
                bestCost = pop[ind].bestCost;
                bestAgent = pop[ind];
            }
        }
        newBestSol++;
    }


    /* ------------------------------------ OrderChildren ------------------------------------*/
    public void orderChildren() {
        int i, j, firstCh, lastCh, parent;

        for (parent = nrParents - 1; parent >= 0; parent--) {
            firstCh = n_ary * parent + 1;
            lastCh = n_ary * parent + n_ary;
            for (i = firstCh; i < lastCh; i++) {
                for (j = (i + 1); j <= lastCh; j++) {
                    if (pop[i].cost > pop[j].cost) {
                        pop[i].exchangeSolutionStructures(pop[j]);
                    }
                }
            }
        }
    }


    /* ------------------------------------ IsNewPocket ------------------------------------*/
//public boolean isNewPocket (long cost) //?  isNewSolutionStructure already garantees that
//{                                      //   there will be no current equals to a pocket
//  for (int i=0; i < popSize; i++)      //since it is called before any time there is
//     if (cost == pop[i].pocket.cost)   // a new current insertSolutionStructure into an agent
//        return(false);
//
//  return(true);
//}


    /* ------------------------------------ IsNew ------------------------------------*/
    public boolean testValues(int values[][]) {
        for (int i = 0; i < popSize; i++) if (pop[i].testValues(values)) return true;
        return false;
    }


    /* ------------------------------------ IsNew ------------------------------------*/
    public boolean isNewSolutionStructure(double cost) {

        for (int i = 0; i < popSize; i++)
            if (!pop[i].isNewSolutionStructure(cost))
                return (false);

        return (true);
    }

    /* ------------------------------------ PocketProp ------------------------------------*/
    public void agentPropagation()
// PocketProp should be a problem independent component that
// propagates the best solutions up the tree.
    {
        // this should be changed for the MemePool, for instance here we
        // should have something like
        // Solution BestSolution=null
        // not something referencing `base.SolutionStructure'...
        Agent auxAgent = null;

        double minCost;
        int firstChild, minChild = 0;

        // we should avoid having these formulas to compute who are the
        // child nodes of the tree. The topology should be computed once
        // at the beginning and the PocketProp component should be general
        // for all hierarchical (directed acyclic graph) population strucutres

        for (int parent = nrParents - 1; parent >= 0; parent--) {

            firstChild = n_ary * parent + 1;
            minCost = Double.MAX_VALUE;

            for (int i = firstChild; i < firstChild + n_ary; i++) {
                if (pop[i].cost < minCost) {
                    minCost = pop[i].cost;
                    auxAgent = pop[i];
                    minChild = i;
                }
            }
            if (minCost < pop[parent].cost) {
                pop[parent].exchangeSolutionStructures(pop[minChild]);
            }
        }
        //Update population best base.Agent
        bestAgent = pop[0];

    }

    /* ------------------------------------ atualiza_pockets -----------------*/
    public void updateAgents(Instance inst) {
        int i;

        for (i = 0; i < popSize; i++) {
            pop[i].updateAgent(inst);
        }
    }

    /* ------------------------------------ Parent ------------------------------------*/
    private int[] getChildren(int x) {
        int i, child[] = new int[n_ary];

        for (i = 0; i < n_ary; i++)
            child[i] = x * n_ary + (i + 1);

        return (child);
    }


    /* ------------------------------------ Parent ------------------------------------*/
    private int getParent(int x) {
        float parent = x / n_ary - 1;

        if (parent != (int) parent)
            return ((int) parent + 1);
        else
            return ((int) parent);
    }

}
