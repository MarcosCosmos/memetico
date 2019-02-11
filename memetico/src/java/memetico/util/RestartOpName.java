package memetico.util;

/**
 * Convenience wrapper around a string - for central documentation of possible options etc without modifying the core Memetico
 */
public enum RestartOpName {
    INSERTION("Insertion"),
    CUT("Cut")
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
