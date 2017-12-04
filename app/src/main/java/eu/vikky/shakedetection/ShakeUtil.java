package eu.vikky.shakedetection;

import android.hardware.SensorEvent;

/**
 * @author Viktor Pesek (vikky@vikky.eu)
 * @Copyright 2017
 */
public class ShakeUtil {
    public static int NANOSECONDS_IN_MILISECOND = 1000000;

    public String getShakeDurationPrettyPrint(SensorEvent sensorEvent, ShakeEvent shakeEvent) {
        long duration = getShakeDuration(sensorEvent, shakeEvent);
        float durationInMs = duration/NANOSECONDS_IN_MILISECOND;
        return "" + durationInMs + "ms";
    }

    private long getShakeDuration(SensorEvent sensorEvent, ShakeEvent shakeEvent) {
        return sensorEvent.timestamp - shakeEvent.getShakeStartedTimestamp();
    }
}
