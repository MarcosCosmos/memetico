package memetico.logging;

import memetico.Instance;
import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

public class PCLogger extends BasicLogger<PCAlgorithmState> implements IPCLogger {
    double startTime;
    int logFrequency;
    public PCLogger(int logFrequency) {
        this.logFrequency = logFrequency;
        //give the clock a sane-ish default start
        startClock();
    }

    //force log regardless of interval (e.g. because it's the end state)
    public void log(String instanceName, Population population, int generation) throws InterruptedException {
        log(new PCAlgorithmState(instanceName, population, generation, System.currentTimeMillis()));
    }

    //log, only stored if at the correct generation
    public void tryLog(String instanceName, Population population, int generation) throws InterruptedException {
        if (generation % logFrequency == 0) {
            log(instanceName, population, generation);
        }
    }

    @Override
    public void startClock() {
        startTime=System.currentTimeMillis();
    }

    @Override
    public double getStartTime() {
        return 0;
    }
}
