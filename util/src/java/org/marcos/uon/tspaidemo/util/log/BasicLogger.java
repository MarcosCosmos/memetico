package org.marcos.uon.tspaidemo.util.log;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BasicLogger<T> implements ILogger<T> {
    //todo: maybe add non-locking version methods to this as well?
    public class View extends AbstractList<T> implements ILogger.View<T> {
        private final List<T> internalStates;
        private ValidityFlag.ReadOnly internalValidity;
        protected View() throws InterruptedException {
            internalStates = new ArrayList<>();
            internalValidity = ValidityFlag.INVALID;
            update();
        }

        /**
         * Non-locking reset, for internal use by extending classes that already took the lock in the caller
         */
        protected void _update() {
            if (!internalValidity.isValid()) {
                internalStates.clear();
                internalValidity = currentValidity::isValid;
            }
            //only add what we need to
            if (size() < states.size()) {
                internalStates.addAll(states.subList(size(), states.size()));
            }
        }

        /**
         * Non-locking reset, for internal use by extending classes that already took the lock in the caller
         */
        protected void _tryUpdate() {
            if(internalValidity.isValid() && size() < states.size()) {
                internalStates.addAll(states.subList(size(), states.size()));
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean update() throws InterruptedException {
            boolean wasValid = internalValidity.isValid();
            lock.withReadLock(this::_update);
            return wasValid;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean tryUpdate() throws InterruptedException {
            boolean wasValid = internalValidity.isValid();
            lock.withReadLock(this::_tryUpdate);
            return wasValid;
        }

        /**
         * Non-locking reset, for internal use by extending classes that already took the lock in the caller
         */
        protected boolean _isValid() {
            return internalValidity.isValid();
        }

        /**
         * {@inheritDoc}
         */
        public boolean isValid() throws InterruptedException {
            lock.acquireReadLock();
            boolean wasValid = internalValidity.isValid();
            lock.releaseReadLock();
            return wasValid;
        }


        @Override
        public int size() {
            return internalStates.size();
        }

        @Override
        public T get(int index) {
            return internalStates.get(index);
        }
    }
    private final List<T> states;
    private ValidityFlag currentValidity; //invalidate views after a reset using a shared-pointer for validity that is invalidated and swapped for a new one on every reset
    protected final ReadWriteLock lock = new ReadWriteLock(); //never allow lock to be changed

    public BasicLogger() {
        states = new ArrayList<>();
        currentValidity = new ValidityFlag();
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
        states.addAll(states);
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
}
