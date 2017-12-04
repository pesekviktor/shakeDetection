package eu.vikky.shakedetection.quadrant;

import android.hardware.SensorEvent;

import static eu.vikky.shakedetection.quadrant.Quadrants.*;
import static eu.vikky.shakedetection.quadrant.Quadrants.PLUS_PLUS_PLUS;

/**
 * @author Viktor Pesek (vikky@vikky.eu)
 * @Copyrigth 2017
 */
public class QuadrantUtil {
    public Quadrants getShakeVectorQuadrant(SensorEvent sensorEvent) {
        float vectorX = sensorEvent.values[0];
        float vectorY = sensorEvent.values[1];
        float vectorZ = sensorEvent.values[2];

        if (vectorX > 0 && vectorY > 0 && vectorZ > 0) {
            return PLUS_PLUS_PLUS;
        } else if (vectorX < 0 && vectorY > 0 && vectorZ > 0) {
            return MINUS_PLUS_PLUS;
        } else if (vectorX > 0 && vectorY < 0 && vectorZ > 0) {
            return PLUS_MINUS_PLUS;
        } else if (vectorX > 0 && vectorY > 0 && vectorZ < 0) {
            return PLUS_PLUS_MINUS;
        } else if (vectorX < 0 && vectorY < 0 && vectorZ > 0) {
            return MINUS_MINUS_PLUS;
        } else if (vectorX < 0 && vectorY > 0 && vectorZ < 0) {
            return MINUS_PLUS_MINUS;
        } else if (vectorX > 0 && vectorY < 0 && vectorZ < 0) {
            return PLUS_MINUS_MINUS;
        } else if (vectorX < 0 && vectorY < 0 && vectorZ < 0) {
            return MINUS_MINUS_MINUS;
        }

        throw new RuntimeException("Should not reach here");
    }
}
