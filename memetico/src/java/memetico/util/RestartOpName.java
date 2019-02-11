package memetico.util;

/**
 * Convenience wrapper around a string - for central documentation of possible options etc without modifying the core Memetico
 */
public enum RestartOpName {
    INSERTION("RestartInsertion"),
    CUT("RestartCut")
    ;
    private String asString;
    RestartOpName(String asString) {
        this.asString = asString;
    }
    @Override
    public String toString() {
        return asString;
    }
}
