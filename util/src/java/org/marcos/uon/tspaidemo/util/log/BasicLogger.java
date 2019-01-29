package org.marcos.uon.tspaidemo.util.log;

import java.util.*;

public class BasicLogger<T> implements ILogger<T> {
    public class View extends AbstractList<T> implements ILogger.View<T> {
        private List<T> internal;
        private View() throws InterruptedException {
            internal = new ArrayList<>();
            update();
        }
        public void update() throws InterruptedException {
            lock.acquireReadLock();
            if(size() < states.size()) {
                internal.addAll(states.subList(size(), states.size()));
            }
            lock.releaseReadLock();
        }

        @Override
        public int size() {
            return internal.size();
        }

        @Override
        public T get(int index) {
            return internal.get(index);
        }
    }
    private List<T> states;
    private ReadWriteLock lock = new ReadWriteLock();

    public BasicLogger() {
        states = new ArrayList<>();
    }

    @Override
    public View newView() throws InterruptedException {
        return new View();
    }

    public void log(T state) throws InterruptedException {
        lock.acquireWriteLock();
        states.add(state);
        lock.releaseWriteLock();
    }

    @Override
    public void logAll(Collection<T> states) throws InterruptedException {
        lock.acquireWriteLock();
        states.addAll(states);
        lock.releaseWriteLock();
    }

}
