package org.marcos.uon.tspaidemo.util.log;

/**
 * Used to invalidate any current views in the event of a reset (and thus trigger them to reset and re-fill their internalStates list instead of just adding missing elements)
 * Validation can only occur one-way (starts valid, gets invalidated, never the other way around)
 */
public class ValidityFlag {
    private boolean isValid;
    @FunctionalInterface
    public interface ReadOnly {
        boolean isValid();
    }
    /**
     * A simple wrapper that overrides the two isValid() and invalidate() methods with synchronised versions; thus creating a thread-safe monitor (getReadOnly() should naturally return the synchronised version)
     */
    public static class Synchronised extends ValidityFlag {
        @Override
        public synchronized boolean isValid() {
            return super.isValid();
        }
        @Override
        public synchronized void invalidate() {
            super.invalidate();
        }
    }

    public ValidityFlag() {
        isValid = true;
    }

    public static final ReadOnly INVALID = () -> false;


    public boolean isValid() {
        return isValid;
    }

    public void invalidate() {
        isValid = false;
    }

    /**
     *
     * @return a reference to this::isValid();
     */
    public ReadOnly getReadOnly() {
        return this::isValid;
    }
}
