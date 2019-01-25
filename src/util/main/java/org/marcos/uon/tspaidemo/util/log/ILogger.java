package org.marcos.uon.tspaidemo.util.log;

import java.util.Collection;
import java.util.List;

public interface ILogger<T> {
    /**
     * A view into the contents of the logger, which holds a seperate copy of the data in so far as it is necessary to provide non-blocking read-only access to the data for a single thread.
     * Note: calling update() to update the view to the current state of the logger will still block, but.
     * This provides a way for the user thread to control when blocking occurs whilst minimally impacting on writers (since this is set up as a producer-multiple-consumer scenario, with the reader not defined in this package/program)
     * Note that the view itself is not synchronised for it's own methods, and only blocks on update calls, synchronising with the logger.
     * Views should be considered externally unmodifiable
     */
    interface View<State> extends List<State> {
        void update() throws InterruptedException;
    }

    /**
     * Creates a new view into the data.
     * May block to initialise the view contents.
     * @return
     * @throws InterruptedException
     */
    View<T> newView() throws InterruptedException;

    /**
     * Adds the state to the log.
     * May cause the thread to wait/block.
     * @param state
     * @throws InterruptedException;
     * @see #logAll(Collection)
     */
    void log(T state) throws InterruptedException;

    /**
     * Adds all supplied states to the log, potentially synchronising more efficiently than multiple individual calls to {@link #log(T)}.
     * May cause the thread to wait/block.
     * @param states
     * @throws InterruptedException
     * @see #log(T)
     */
    void logAll(Collection<T> states) throws InterruptedException;
}
