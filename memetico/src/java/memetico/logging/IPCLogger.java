package memetico.logging;

import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.ILogger;

public interface IPCLogger extends ILogger<MemeticoSnapshot> {
    interface View extends ILogger.View<MemeticoSnapshot> {
        long getStartTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    View newView() throws InterruptedException;
    void log(String instanceName, Population population, int generation, boolean isFinal) throws InterruptedException;

    void tryLog(String instanceName, Population population, int generation, boolean isFinal) throws InterruptedException;
}
