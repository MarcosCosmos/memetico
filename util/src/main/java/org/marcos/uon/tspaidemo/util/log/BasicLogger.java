package org.marcos.uon.tspaidemo.util.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BasicLogger<T> implements ILogger<T> {
    //todo: use a synchronised validity flag (with it's own read-write-lock) instead of potentially locking on isvalid checks on already dead views?
    public class View extends AbstractList<T> implements ILogger.View<T> {
        private final List<T> states;
        private transient ValidityFlag.ReadOnly validity;
        protected View() throws InterruptedException {
            states = new ArrayList<>();
            validity = ValidityFlag.INVALID;
            update();
        }

        /**
         * Non-locking reset, for internal use by extending classes that already took the lock in the caller
         */
        protected void _update() {
            if (!validity.isValid()) {
                states.clear();
                validity = currentValidity.getReadOnly();
            }
            //only add what we need to
            if (size() < BasicLogger.this.states.size()) {
                states.addAll(BasicLogger.this.states.subList(size(), BasicLogger.this.states.size()));
            }
        }

        /**
         * Non-locking reset, for internal use by extending classes that already took the lock in the caller
         */
        protected void _tryUpdate() {
            if(validity.isValid() && size() < BasicLogger.this.states.size()) {
                states.addAll(BasicLogger.this.states.subList(size(), BasicLogger.this.states.size()));
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean update() throws InterruptedException {
            boolean wasValid = validity.isValid();
            lock.withReadLock(this::_update);
            return wasValid;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean tryUpdate() throws InterruptedException {
            boolean wasValid = validity.isValid();
            lock.withReadLock(this::_tryUpdate);
            return wasValid;
        }

        /**
         * Non-locking check, for internal use by extending classes that already took the lock in the caller
         */
        protected boolean _isValid() {
            return validity.isValid();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isValid() throws InterruptedException {
            lock.acquireReadLock();
            boolean wasValid = validity.isValid();
            lock.releaseReadLock();
            return wasValid;
        }

        @Override
        public int size() {
            return states.size();
        }

        @Override
        public T get(int index) {
            return states.get(index);
        }

        @Override
        public JsonObject jsonify() {
            JsonObject result = new JsonObject();
            result.add("states", gson.toJsonTree(states));
            return result;
        }
    }

    protected transient Gson gson;
    protected transient GsonBuilder gsonBuilder;
    private final List<T> states;
    private transient ValidityFlag currentValidity; //invalidate views after a reset using a shared-pointer for validity that is invalidated and swapped for a new one on every reset
    protected final transient ReadWriteLock lock = new ReadWriteLock(); //never allow lock to be changed
    protected final Class<T> tClass;
    /**
     * Specifies the type to use for gson deserialisation
     * @param tClass
     */
    protected BasicLogger(Class<T> tClass) {
        this.tClass = tClass;
        gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        states = new ArrayList<>();
        currentValidity = new ValidityFlag.Synchronised();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View newView() throws InterruptedException {
        return new View();
    }

    /**
     * Non-locking version for extending classes...
     * @param state
     */
    protected void _log(T state) {
        states.add(state);
    }

    /**
     * {@inheritDoc}
     */
    public void log(T state) throws InterruptedException {
        lock.acquireWriteLock();
        _log(state);
        lock.releaseWriteLock();
    }

    public void _logAll(Collection<T> states) throws InterruptedException {
        this.states.addAll(states);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logAll(Collection<T> states) throws InterruptedException {
        lock.acquireWriteLock();
        _logAll(states);
        lock.releaseWriteLock();
    }

    /**
     * Non-locking reset, for internal use by extending classes that already took the lock in the caller
     */
    protected void _reset() {
        states.clear();
        //invalidate any views relying on existing states
        currentValidity.invalidate();
        currentValidity = new ValidityFlag();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() throws InterruptedException {
        lock.withWriteLock(this::_reset);
    }

    protected void _loadJson(JsonObject data) {
        data.get("states").getAsJsonArray().forEach(each -> states.add(gson.fromJson(each, tClass)));
    }

    @Override
    public void loadJson(JsonElement data) throws InterruptedException {
        lock.acquireWriteLock();
        _reset();
        _loadJson(data.getAsJsonObject());
        lock.releaseWriteLock();
    }
}
