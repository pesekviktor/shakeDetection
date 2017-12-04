package eu.vikky.shakedetection;

import android.hardware.SensorManager;

import static eu.vikky.shakedetection.ShakeUtil.NANOSECONDS_IN_MILISECOND;


/**
 * @author Viktor Pesek (vikky@vikky.eu)
 * @Copyright 2017
 */
class ShakeDetectorProperties {
    public static final ShakeDetectorProperties DEFAULT =
            new ShakeDetectorProperties(
                    SensorManager.SENSOR_DELAY_GAME,
                    100,
                    100 * NANOSECONDS_IN_MILISECOND,
                    150 * NANOSECONDS_IN_MILISECOND,
                    2000 * NANOSECONDS_IN_MILISECOND,
                    500 * NANOSECONDS_IN_MILISECOND,
                    2);

    /**
     * Creates properties for shake detection. For defaults see ShakeDetectorProperties.DEFAULT.
     *
     * @param samplingPeriodUs                         @see android.hardware.SensorManager#registerListener(SensorEventListener, Sensor, int)
     * @param moveSensitivity                          Squared difference to accelaration vector, where the move is considered 'significant'
     *                                                 or acceleration.
     * @param noAccelarationDelay                      Time in ns after which no acceleration is considered an end to current shake event.
     * @param minShakeTime                             Minimum time in ns where constant acceleration will be considered as a shake.
     * @param maxShakeTime                             Maximum shake time in ns for one shake. After this time new shake will be started.
     *                                                 Set to 0 to disable max time.
     * @param idleDelayAfterShake                      Delay after last shake reached before new shake is listened to.
     * @param numberOfQuadrantShakeDirectionsNecessary The acceleration must be present to this number of quadrants to qualify as a shake. From 1 - 9
     */
    public ShakeDetectorProperties(int samplingPeriodUs, float moveSensitivity,
                                   long noAccelarationDelay, long minShakeTime,
                                   long maxShakeTime, long idleDelayAfterShake,
                                   int numberOfQuadrantShakeDirectionsNecessary) {
        if (numberOfQuadrantShakeDirectionsNecessary < 1 || 8 < numberOfQuadrantShakeDirectionsNecessary) {
            throw new IllegalArgumentException("Number of quadrants must be between 1 and 8 inclusive");
        }
        this.samplingPeriodUs = samplingPeriodUs;
        this.moveSensitivity = moveSensitivity;
        this.noAccelarationDelay = noAccelarationDelay;
        this.minShakeTime = minShakeTime;
        this.maxShakeTime = maxShakeTime;
        this.idleDelayAfterShake = idleDelayAfterShake;
        this.numberOfQuadrantShakeDirectionsNecessary = numberOfQuadrantShakeDirectionsNecessary;
    }

    int samplingPeriodUs = SensorManager.SENSOR_DELAY_GAME;

    float moveSensitivity = 0;

    long noAccelarationDelay = 0;

    long minShakeTime = 0;

    long maxShakeTime = 0;

    long idleDelayAfterShake = 0;

    int numberOfQuadrantShakeDirectionsNecessary;
}
