package eu.vikky.shakedetection;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import eu.vikky.shakedetection.quadrant.QuadrantUtil;
import eu.vikky.shakedetection.quadrant.Quadrants;


/**
 * @author Viktor Pesek (vikky@vikky.eu)
 * @Copyright 2017
 */
public class ShakeDetector implements SensorEventListener {
    private final String LOG_TAG = ShakeDetector.class.getSimpleName();
    private ShakeDetectorProperties properties;
    private ShakeListener shakeListener;

    private float gravitySquared = SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH;

    private final ShakeUtil shakeUtil;
    private final QuadrantUtil quadrantUtil;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private ShakeEvent shakeEvent;

    private State status = State.WAITING_FOR_SHAKE;

    private enum State {
        WAITING_FOR_SHAKE, SHAKING, IDLE_PERIOD_AFTER_LAST_SHAKE
    }

    public ShakeDetector(SensorManager sensorManager) {
        this(sensorManager, ShakeDetectorProperties.DEFAULT);
    }


    public ShakeDetector(SensorManager sensorManager,
                         ShakeDetectorProperties properties) {
        this.properties = properties;
        this.sensorManager = sensorManager;
        this.shakeUtil = new ShakeUtil();
        this.quadrantUtil = new QuadrantUtil();
    }

    public void startListening(ShakeListener listener) throws AccelometerNotFoundException {
        shakeListener = listener;
        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null)
            throw new AccelometerNotFoundException("Device does not have accelometer.");

        sensorManager.registerListener(this, accelerometer, properties.samplingPeriodUs);
    }


    private boolean isDeviceAccelerating(SensorEvent sensorEvent) {
        float vectorX = sensorEvent.values[0];
        float vectorY = sensorEvent.values[1];
        float vectorZ = sensorEvent.values[2];

        //Get vector move vector length squared and deduct gravity squared
        float accelerationVectorSquared = vectorX * vectorX + vectorY * vectorY + vectorZ * vectorZ
                - gravitySquared;

        return accelerationVectorSquared > properties.moveSensitivity;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (status.equals(State.IDLE_PERIOD_AFTER_LAST_SHAKE)) {
            handleMaxShakeTimeEnded(sensorEvent);
        } else if (reachedMaxShakeTime(sensorEvent)) {
            Log.v(LOG_TAG, "Shake ending, reached maxShakeTime");
            stopShaking(sensorEvent);
        } else if (isDeviceAccelerating(sensorEvent)) {
            startOrUpdateShakeEvent(sensorEvent);
        } else {
            if (shouldStopShakeEvent(sensorEvent)) {
                stopShaking(sensorEvent);
            }
        }
    }

    private void handleMaxShakeTimeEnded(SensorEvent sensorEvent) {
        if (shakeEvent.getLastShakeTimestamp() + properties.idleDelayAfterShake
                < sensorEvent.timestamp) {
            Log.v(LOG_TAG, "Idle period after last shake ended.");
            status = State.WAITING_FOR_SHAKE;
        }
    }

    private boolean reachedMaxShakeTime(SensorEvent sensorEvent) {
        return isShaking()
                && properties.maxShakeTime != 0
                && sensorEvent.timestamp
                > shakeEvent.getShakeStartedTimestamp() + properties.maxShakeTime;

    }

    private void stopShaking(SensorEvent sensorEvent) {
        if (hasMinReachedMinShakeTime(sensorEvent) && hasShakedInRequiredDirections()) {
            Log.d(LOG_TAG, "Shake finished; " + printShakeDuration(sensorEvent));
            status = State.IDLE_PERIOD_AFTER_LAST_SHAKE;
            shakeListener.onShakeFinished();

        } else {
            status = State.WAITING_FOR_SHAKE;

            Log.v(LOG_TAG, "Shake discarded, under minShakeTime; "
                    + printShakeDuration(sensorEvent));
        }
    }

    private boolean hasShakedInRequiredDirections() {
        return shakeEvent.getQuadrantVectorsInvolved().size() >= properties.numberOfQuadrantShakeDirectionsNecessary;
    }

    private boolean hasMinReachedMinShakeTime(SensorEvent sensorEvent) {
        return shakeEvent.getShakeStartedTimestamp() + properties.minShakeTime
                < sensorEvent.timestamp;
    }


    private boolean shouldStopShakeEvent(SensorEvent sensorEvent) {
        return isShaking() &&
                shakeEvent.getLastShakeTimestamp() + properties.noAccelarationDelay
                        < sensorEvent.timestamp;
    }

    private void startOrUpdateShakeEvent(SensorEvent sensorEvent) {
        if (!isShaking()) {
            shakeListener.onShakeStarted();
            shakeEvent = new ShakeEvent(sensorEvent.timestamp);
            status = State.SHAKING;

            Log.d(LOG_TAG, "Shake started; timestamp: " + sensorEvent.timestamp);
        } else {
            shakeEvent.setLastShakeTimestamp(sensorEvent.timestamp);

            Log.v(LOG_TAG, "Shake continues; " + printShakeDuration(sensorEvent));
        }
        Quadrants shakeVectorQuadrant = quadrantUtil.getShakeVectorQuadrant(sensorEvent);
        shakeEvent.addQuadrant(shakeVectorQuadrant);
    }

    private String printShakeDuration(SensorEvent sensorEvent) {
        return " duration: " + shakeUtil.getShakeDurationPrettyPrint(sensorEvent, shakeEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean isShaking() {
        return status.equals(State.SHAKING);
    }

}
