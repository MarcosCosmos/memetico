package memetico.logging;

import memetico.Instance;
import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

public class PCLogger extends BasicLogger<PCAlgorithmState> implements IPCLogger {
    double startTime;
    public PCLogger() {
        //give the clock a sane-ish default start
        startClock();
    }
    public void log(String instanceName, Population population, int generation) throws InterruptedException {
        log(new PCAlgorithmState(instanceName, population, generation, System.currentTimeMillis()));
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
