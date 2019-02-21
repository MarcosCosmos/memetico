package org.marcos.uon.tspaidemo.util.tsp;

import java.io.File;
import java.net.URL;

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
    public final URL problemFile;
    public final SolutionType solutionType;
    public final URL tourFile; //only if solution-type is tour
    public final long targetCost; //only if solution-type is cost.

    public ProblemConfiguration(URL instanceFile, URL tourFile) {
        this.problemFile = instanceFile;
        this.solutionType = SolutionType.TOUR;
        this.tourFile = tourFile;
        this.targetCost = -1;
    }
    public ProblemConfiguration(URL instanceFile, long targetCost) {
        this.problemFile = instanceFile;
        this.solutionType = SolutionType.COST;
        this.tourFile = null;
        this.targetCost = targetCost;
    }
}
