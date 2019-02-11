package org.marcos.uon.tspaidemo.gui.memetico;

import memetico.logging.IPCLogger;
import memetico.util.CrossoverOpName;
import memetico.util.LocalSearchOpName;
import memetico.util.RestartOpName;

import java.io.DataOutputStream;

public class MemeticoConfiguration {
    //these two are unused in the current implementation; their usage may not even implemented or may not be working
    public final String solutionStructure = "DiCycle";
    public final String populationStructure = "Ternary Tree";
    //there was only one option (nearest neighbour)
    public final String constructionAlgorithm = "Nearest Neighbour";

    public final int populationSize;
    public final int mutationRate;

    public final String localSearchOp; //buscaLocal
    public final String crossoverOp;
    public final String restartOp;

    //there was only one option (insertion)
    public final String mutationOp = "MutationInsertion";

    //unused; instead relying on maxGenerations
    public final long maxTime = 100;
    public final long maxGenerations; //can be (and defaults to) 0 to automatically compute it
    public final long numReplications; //defaults to 1


    public MemeticoConfiguration(int populationSize, int mutationRate, String localSearchOp, String crossoverOp, String restartOp, long maxGenerations, long numReplications) {
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.localSearchOp = localSearchOp;
        this.crossoverOp = crossoverOp;
        this.restartOp = restartOp;
        this.maxGenerations = maxGenerations;
        this.numReplications = numReplications;
    }

    public MemeticoConfiguration(int populationSize, int mutationRate, String localSearchOp, String crossoverOp, String restartOp, long maxGenerations) {
        this(populationSize, mutationRate, localSearchOp, crossoverOp, restartOp, maxGenerations, 1);
    }
    public MemeticoConfiguration(int populationSize, int mutationRate, String localSearchOp, String crossoverOp, String restartOp) {
        this(populationSize, mutationRate, localSearchOp, crossoverOp, restartOp, 0, 1);
    }
//    public MemeticoConfiguration(int populationSize, int mutationRate, LocalSearchOpName localSearchOp, CrossoverOpName crossoverOp, RestartOpName restartOp, long maxTime, long maxGenerations, long numReplications) {
//        this(populationSize, mutationRate, localSearchOp.toString(), crossoverOp.toString(), restartOp.toString(), maxTime, maxGenerations, numReplications);
//    }
}
