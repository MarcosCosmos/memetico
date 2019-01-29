package memetico;

public abstract class MutationOperators {
    int mutationRate = 5;

    public abstract void runMutation(SolutionStructure child);
}