package memetico;/*
 * File: Agent.java
 *
 * Date      Authors
 * 8/26/99   Luciana Buriol and Pablo Moscato
 *
 */

//package MemePool.Agent;


/**
 * A class for representing a generic agent solver
 * of the MemePool.
 * <p>
 * An agent should contain at least one valid
 * representation of the solution of the problem
 * it is addressing. Thus some types of agents
 * can contain several different representations.
 *
 * </p>The <a href="../Agent/Agent.java">source</a>.
 *
 * @author Pablo Moscato, Natalio Krasnogor, and Luciana Buriol
 **/

public abstract class Agent {
    //Constants Section
    final public static int NONE = -1;
    final public static int POKET_CURRENT = 1;

    //Variable Section
    static public int agentType = NONE;
    public double cost;
    //In case of more than one SolutionStructure by agent
    public double avgCost;
    public double bestCost;

    //Methods Section
    abstract double calculateCost(Instance inst);

    abstract boolean isNewSolutionStructure(double cost);

    abstract void insertSolutionStructure(SolutionStructure child);

    abstract void updateAgent(Instance isnt);

    abstract void exchangeSolutionStructures(Agent source);

    abstract boolean testValues(int values[][]);
}// end of class Agent
