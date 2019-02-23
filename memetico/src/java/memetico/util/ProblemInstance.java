package memetico.util;

import org.jorlib.io.tspLibReader.TSPLibInstance;

import java.io.IOException;
import java.io.InputStream;

public class ProblemInstance {
    private ProblemConfiguration configuration;
    private TSPLibInstance tspLibInstance;
    private String name;
    private long targetCost; //note that this isn't garaunteed to match the config (if for example, the config is based on a template, and wants a custom cost,

    public ProblemInstance(ProblemConfiguration config) throws IOException {
        configuration = config;
        InputStream tmp;
        tmp = configuration.problemFile.openStream();
        tspLibInstance = new TSPLibInstance(tmp);
        tmp.close();
        name = tspLibInstance.getName();
        switch (configuration.solutionType) {
            case TOUR:
                tmp = configuration.tourFile.openStream();
                tspLibInstance.addTour(tmp);
                tmp.close();
                targetCost = (long)tspLibInstance.getTours().get(tspLibInstance.getTours().size()-1).distance(tspLibInstance);
                break;
            case COST:
                targetCost = config.targetCost;
                break;
            default:
                targetCost = 0;
        }
    }

    public ProblemInstance(ProblemConfiguration config, TSPLibInstance tspLibInstance, String name) throws IOException {
        configuration = config;
        InputStream tmp;
        tmp = configuration.problemFile.openStream();
        tspLibInstance = new TSPLibInstance(tmp);
        this.name = name;
        switch (configuration.solutionType) {
            case TOUR:
                targetCost = (long)tspLibInstance.getTours().get(tspLibInstance.getTours().size()-1).distance(tspLibInstance);
                break;
            case COST:
                targetCost = config.targetCost;
                break;
            default:
                targetCost = 0;
        }
    }

    public ProblemInstance(ProblemInstance src) {
        this.configuration = src.configuration;
        this.tspLibInstance = src.getTspLibInstance();
        this.name = src.name;
        this.targetCost = src.targetCost;
    }

    public ProblemConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ProblemConfiguration configuration) {
        this.configuration = configuration;
    }

    public TSPLibInstance getTspLibInstance() {
        return tspLibInstance;
    }

    public void setTspLibInstance(TSPLibInstance tspLibInstance) {
        this.tspLibInstance = tspLibInstance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTargetCost() {
        return targetCost;
    }

    public void setTargetCost(long targetCost) {
        this.targetCost = targetCost;
    }
}
