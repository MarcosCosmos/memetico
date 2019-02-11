package memetico.util;

/**
 * Convenience wrapper around a string - for central documentation of possible options etc without modifying the core Memetico
 */
public enum CrossoverOpName {
    SAX("Strategic Arc Crossover - SAX"),
    DPX("Distance Preserving Crossover - DPX"),
    UNIFORM("Uniform")
    ;
    private String asString;
    CrossoverOpName(String asString) {
        this.asString = asString;
    }
    @Override
    public String toString() {
        return asString;
    }
}
