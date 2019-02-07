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
    interface View<T> extends List<T> {
        /**
         * Updates, ensuring that the end state is valid
         * @throws InterruptedException
         * @return a boolean indicating whether or not the view was valid at the beginning of the update (if false, the underlying logger has been reset)
         * @see #tryUpdate()
         * @see #isValid()
         */
        boolean update() throws InterruptedException;
        /**
         * Tries to update (succeeds only if the view is still valid); if the view is not valid, no changes will occur
         * @throws InterruptedException
         * @return a boolean indicating whether or not the view was valid at the beginning of the update (if false, the underlying logger has been reset)
         * @see #update()
         * @see #isValid()
         */
        boolean tryUpdate() throws InterruptedException;
        /**
         * Checks if the current state is valid (that the underlying logger has not been cleared.
         * This is useful if you want to keep a backup of the current view contents before updating (or use a separate view for the new data); (You can also use {@code #tryUpdate()} for this)
         * @see #update()
         * @see #tryUpdate()
         * @return
         */
        boolean isValid() throws InterruptedException;
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

    /**
     * Resets the logger to a valid initial state
     */
    void reset() throws InterruptedException;
}
