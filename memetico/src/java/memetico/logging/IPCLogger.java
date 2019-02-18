package memetico.logging;

import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.ILogger;

public interface IPCLogger extends ILogger<MemeticoSnapshot> {
    interface View extends ILogger.View<MemeticoSnapshot> {
        double getStartTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    View newView() throws InterruptedException;
    void log(String instanceName, Population population, int generation) throws InterruptedException;

    void tryLog(String instanceName, Population population, int generation) throws InterruptedException;
}
