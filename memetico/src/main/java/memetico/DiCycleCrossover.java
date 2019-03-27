package memetico;

public abstract class DiCycleCrossover extends CrossoverOperators {
    abstract void runCrossover(SolutionStructure parentA, SolutionStructure parentB, SolutionStructure child, Instance inst) throws Exception;
}