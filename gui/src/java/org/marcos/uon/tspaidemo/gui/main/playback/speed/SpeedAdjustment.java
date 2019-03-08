package org.marcos.uon.tspaidemo.gui.main.playback.speed;

import javafx.beans.NamedArg;

import java.time.Duration;
import java.util.function.Function;

public abstract class SpeedAdjustment implements Function<Duration, Duration> {
    @Override
    public abstract Duration apply(Duration duration);

    @Override
    public abstract String toString();

}
