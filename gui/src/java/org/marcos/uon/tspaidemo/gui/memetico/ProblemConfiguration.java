package org.marcos.uon.tspaidemo.gui.memetico;

import java.io.File;

public class ProblemConfiguration {
    public enum SolutionType {
        TOUR("Tour"),
        COST("Cost");

        private String asString;

        SolutionType(String asString) {
            this.asString = asString;
        }
        @Override
        public String toString() {
            return asString;
        }
    }
    public final File problemFile;
    public final SolutionType solutionType;
    public final File tourFile; //only if solution-type is tour
    public final long targetCost; //only if solution-type is cost.

    public ProblemConfiguration(File instanceFile, File tourFile) {
        this.problemFile = instanceFile;
        this.solutionType = SolutionType.TOUR;
        this.tourFile = tourFile;
        this.targetCost = -1;
    }
    public ProblemConfiguration(File instanceFile, long targetCost) {
        this.problemFile = instanceFile;
        this.solutionType = SolutionType.COST;
        this.tourFile = null;
        this.targetCost = targetCost;
    }
}
