package memetico;

public abstract class CrossoverOperators {
    abstract void runCrossover(SolutionStructure parentA, SolutionStructure parentB, SolutionStructure child, Instance inst) throws Exception;
}