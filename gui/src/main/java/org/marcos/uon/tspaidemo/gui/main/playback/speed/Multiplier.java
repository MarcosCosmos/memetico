package org.marcos.uon.tspaidemo.gui.main.playback.speed;

import javafx.beans.NamedArg;

import java.time.Duration;

public class Multiplier extends SpeedAdjustment {
    private final long factor;
    private final String string;
    public Multiplier(@NamedArg("factor") long factor) {
        this.factor = factor;
        this.string = String.valueOf(factor);
    }
    @Override
    public Duration apply(Duration duration) {
        return duration.multipliedBy(factor);
    }
    @Override
    public String toString() {
        return this.string;
    }
}
