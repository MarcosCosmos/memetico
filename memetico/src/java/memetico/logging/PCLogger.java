package memetico.logging;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import memetico.Memetico;
import memetico.Population;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;
import sun.plugin2.message.Serializer;

import java.lang.reflect.Type;

public class PCLogger extends BasicLogger<MemeticoSnapshot> implements IPCLogger {
    private long startTime;
    protected transient double logFrequency;
    public class View extends BasicLogger<MemeticoSnapshot>.View implements IPCLogger.View {
        protected long startTime;

        protected View() throws InterruptedException {
            super();
            startTime = PCLogger.this.startTime;
        }

        /**
         * Need to override the full update to also update start time
         */
        @Override
        protected void _update() {
            //update the start time if that's necessary
            if(!_isValid()) {
                startTime = PCLogger.this.startTime;
            }
            super._update();
        }

        @Override
        public long getStartTime() {
            return startTime;
        }

        @Override
        public JsonObject jsonify() {
            JsonObject result = super.jsonify();
            result.addProperty("startTime", startTime);
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View newView() throws InterruptedException {
        return new View();
    }

    public PCLogger(int logFrequency) {
        super(MemeticoSnapshot.class);
        this.logFrequency = logFrequency;
        //give the clock a sane-ish default start; non-locking since there can be no other lock users until the end of this
        startTime=System.nanoTime();
        gsonBuilder.registerTypeHierarchyAdapter(MemeticoSnapshot.LightTour.class, new MemeticoSnapshot.LightTour.Deserializer());
//        gsonBuilder.registerTypeHierarchyAdapter(MemeticoSnapshot.AgentSnapshot.class, new MemeticoSnapshot.AgentSnapshot.Deserializer());
//        gsonBuilder.registerTypeAdapter(MemeticoSnapshot.class, new MemeticoSnapshot.Deserializer());
        gson = gsonBuilder.create();
    }

    /**
     * force log regardless of interval (e.g. because it's the end state)
     */
    protected void _log(String instanceName, Population population, int generation) throws InterruptedException {
        _log(new MemeticoSnapshot(instanceName, population, generation, System.nanoTime()));
    }


    public void log(String instanceName, Population population, int generation) throws InterruptedException {
        log(new MemeticoSnapshot(instanceName, population, generation, System.nanoTime())); //we only need the lock after creating the new object;
    }

    /**
     * log, only stored if at the correct generation
     */
    protected void _tryLog(String instanceName, Population population, int generation) throws InterruptedException {
        if (generation % logFrequency == 0) {
            _log(instanceName, population, generation);
        }
    }

    public void tryLog(String instanceName, Population population, int generation) throws InterruptedException {
        //for this we actually need the lock the whole time, in case the frequency changes
        lock.acquireWriteLock();
        _tryLog(instanceName,population,generation);
        lock.releaseWriteLock();
    }

    /**
     * Non-locking version for use by extending classes who already have the lock
     */
    @Override
    protected void _reset() {
        super._reset();
        startTime=System.nanoTime();
    }

    /**
     *
     * @return The new start time
     */
    public void reset() throws InterruptedException {
        lock.withWriteLock(this::_reset);
    }

    public double getLogFrequency() throws InterruptedException {
        double result;
        lock.acquireReadLock();
        result = logFrequency;
        lock.releaseReadLock();
        return result;
    }

    /**
     *
     * @param logFrequency
     * @return the old log frequency
     * @throws InterruptedException
     */
    public double setLogFrequency(double logFrequency) throws InterruptedException {
        double result = this.logFrequency;
        lock.acquireWriteLock();
        this.logFrequency = logFrequency;
        lock.releaseWriteLock();
        return result;
    }

    protected void _loadJson(JsonObject data) {
        super._loadJson(data);
        startTime = data.get("startTime").getAsLong();
    }

    public void loadJson(JsonElement data) throws InterruptedException {
        lock.acquireWriteLock();
        super._reset();
        _loadJson(data.getAsJsonObject());
        lock.releaseWriteLock();
    }
}
