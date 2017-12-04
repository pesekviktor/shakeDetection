package eu.vikky.shakedetection;

import java.util.HashSet;
import java.util.Set;

import eu.vikky.shakedetection.quadrant.Quadrants;


/**
 * @author Viktor Pesek (vikky@vikky.eu)
 * @Copyright 2017
 */
public class ShakeEvent {
    private final long shakeStartedTimestamp;
    private long lastShakeTimestamp;
    private Set<Quadrants> quadrantVectorsInvolved = new HashSet<>();

    public ShakeEvent(long shakeStartedTimestamp) {
        this.shakeStartedTimestamp = shakeStartedTimestamp;
        this.lastShakeTimestamp = shakeStartedTimestamp;
    }

    void setLastShakeTimestamp(long lastShakeTimestamp) {
        this.lastShakeTimestamp = lastShakeTimestamp;
    }

    public void addQuadrant(Quadrants quadrants) {
        quadrantVectorsInvolved.add(quadrants);
    }

    public Set<Quadrants> getQuadrantVectorsInvolved() {
        return quadrantVectorsInvolved;
    }

    public long getLastShakeTimestamp() {
        return lastShakeTimestamp;
    }

    public long getShakeStartedTimestamp() {
        return shakeStartedTimestamp;
    }
}
