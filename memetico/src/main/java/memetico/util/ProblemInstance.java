package memetico.util;



import org.marcos.uon.tspaidemo.util.tsplib.TSPLibInstance;

import java.io.IOException;
import java.io.InputStream;

public class ProblemInstance {
    private ProblemConfiguration configuration;
    private TSPLibInstance tspLibInstance;
    private String name;
    private long targetCost; //note that this isn't garaunteed to match the config (if for example, the config is based on a template, and wants a custom cost,

//    public ProblemInstance(ProblemConfiguration config) {
//
//    }

    public ProblemInstance(ProblemConfiguration config, TSPLibInstance tspLibInstance, String name, long targetCost) {
        configuration = config;
        this.tspLibInstance = tspLibInstance;
        this.name = name;
        this.targetCost = targetCost;
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

    public static ProblemInstance create(ProblemConfiguration config) {
        ProblemInstance result;
        TSPLibInstance tspLibInstance;
        long targetCost;
        try {
            InputStream tmp;
            tmp = config.problemFile.openStream();
            tspLibInstance = new TSPLibInstance(tmp);
            tmp.close();
        } catch (Exception e) {
            e.printStackTrace();
            tspLibInstance = null;
        }
        switch (config.solutionType) {
            case TOUR:
                try {
                    InputStream tmp;
                    tmp = config.tourFile.openStream();
                    tspLibInstance.addTour(tmp);
                    tmp.close();
                    targetCost = (long) tspLibInstance.getTours().get(tspLibInstance.getTours().size()-1).distance(tspLibInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                    targetCost = -1;
                }
                break;
            case COST:
                targetCost = config.targetCost;
                break;
            default:
                targetCost = 0;
        }

        return new ProblemInstance(config, tspLibInstance, tspLibInstance != null ? tspLibInstance.getName() : null, targetCost);
    }
}
