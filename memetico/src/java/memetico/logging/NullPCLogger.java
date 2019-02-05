package memetico.logging;

import memetico.Instance;
import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.NullLogger;

public class NullPCLogger extends NullLogger<PCAlgorithmState> implements IPCLogger {
    @Override
    public void log(String instanceName, Population population, int generation) throws InterruptedException {
    }

    @Override
    public void tryLog(String instanceName, Population population, int generation) throws InterruptedException {
    }

    @Override
    public void startClock() {
    }

    @Override
    public double getStartTime() {
        return Double.NEGATIVE_INFINITY;
    }
}
