package memetico.util;

/**
 * Convenience wrapper around a string - for central documentation of possible options etc without modifying the core Memetico
 */
public enum LocalSearchOpName {
    RAI("Recursive Arc Insertion"),
    THREE_OPT("3Opt")
    ;
    private String asString;
    LocalSearchOpName(String asString) {
        this.asString = asString;
    }
    @Override
    public String toString() {
        return asString;
    }
}
