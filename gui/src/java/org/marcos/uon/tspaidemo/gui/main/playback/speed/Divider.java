package org.marcos.uon.tspaidemo.gui.main.playback.speed;

import javafx.beans.NamedArg;

import java.time.Duration;

public class Divider extends SpeedAdjustment {
    private final long factor;
    private final String string;
    public Divider(@NamedArg("factor")long factor) {
        this.factor = factor;
        this.string = String.format("1/%d", factor);
    }
    @Override
    public Duration apply(Duration duration) {
        return duration.dividedBy(factor);
    }
    @Override
    public String toString() {
        return this.string;
    }
}
