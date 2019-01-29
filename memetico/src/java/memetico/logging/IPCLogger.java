package memetico.logging;

import memetico.Instance;
import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.ILogger;

public interface IPCLogger extends ILogger<PCAlgorithmState> {
    void log(String instanceName, Population population, int generation) throws InterruptedException;

    /**
     * Causes the current cpu time to be recorded for the purposes of logging, may influence future logs.
     */
    void startClock();
    //gets the time the clock was started
    double getStartTime();
}
