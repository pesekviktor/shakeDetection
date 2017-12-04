package eu.vikky.shakedetection;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;

import static eu.vikky.pivnicek.util.shake.ShakeUtil.NANOSECONDS_IN_MILISECOND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Viktor Pesek (vikky@vikky.eu)
 * @Copyrigth 2017
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class ShakeDetectorTest {


    @Mock
    SensorManager sensorManager;

    @Mock
    Sensor accelometerSensor;
    ShakeDetector shakeDetector;

    private ShakeDetectorProperties testProperties;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Log.class);
        testProperties =
                new ShakeDetectorProperties(
                        SensorManager.SENSOR_DELAY_GAME,
                        100,
                        100 * NANOSECONDS_IN_MILISECOND,
                        200 * NANOSECONDS_IN_MILISECOND,
                        2000 * NANOSECONDS_IN_MILISECOND,
                        500 * NANOSECONDS_IN_MILISECOND,
                        1);
    }


    private void callSensorChange(float x, float y, float z, long timestamp) throws Exception {
        SensorEvent sensorEvent = mock(SensorEvent.class);

        Field valuesField = SensorEvent.class.getField("values");
        valuesField.setAccessible(true);
        float[] sensorValue = {x, y, z};
        valuesField.set(sensorEvent, sensorValue);

        Field timestampField = SensorEvent.class.getField("timestamp");
        timestampField.setAccessible(true);
        timestampField.set(sensorEvent, timestamp);

        shakeDetector.onSensorChanged(sensorEvent);
    }

    private ShakeListener startShakeDetection(ShakeDetectorProperties properties)
            throws AccelometerNotFoundException {
        ShakeListener listener = mock(ShakeListener.class);
        when(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
                .thenReturn(accelometerSensor);
        shakeDetector = new ShakeDetector(sensorManager, properties);
        shakeDetector.startListening(listener);
        return listener;
    }

    private ShakeListener startShakeDetection() throws AccelometerNotFoundException {
       return  startShakeDetection(testProperties);
    }

    @Test(expected = AccelometerNotFoundException.class)
    public void startWithNoAccelometerShouldThrowException() throws AccelometerNotFoundException {
        shakeDetector = new ShakeDetector(sensorManager);
        shakeDetector.startListening(mock(ShakeListener.class));
    }

    @Test
    public void simpleShakeDetection() throws Exception {
        ShakeListener shakeListener = startShakeDetection();

        long timeStamp = 0;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 10;
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates
        timeStamp = timeStamp + testProperties.minShakeTime + 10; //above shake time, so gets noticed
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 1, timeStamp);//No move

        verify(shakeListener, times(1)).onShakeStarted();
        verify(shakeListener, times(1)).onShakeFinished();
    }

    @Test
    public void shakeShouldNotBeDetectedUnderMinShakeTime() throws Exception {
        ShakeListener shakeListener = startShakeDetection();

        long timeStamp = 0;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates
        timeStamp += 5; //under shake Time, should not get noticed
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 1, timeStamp);//No move

        verify(shakeListener, times(1)).onShakeStarted();
        verify(shakeListener, never()).onShakeFinished();
    }


    @Test
    public void breakInAccelerationWhenShakingConsideredStillSameShake() throws Exception {
        ShakeListener shakeListener = startShakeDetection();

        long timeStamp = 0;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates

        /**
         * No move, but shake should not get discarded because of noAccelarationDelay delay
         */
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 1, 0, timeStamp);//No move
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 1, 51, timeStamp);//accelerates
        timeStamp = timeStamp + testProperties.minShakeTime;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 2, timeStamp);//No move

        verify(shakeListener, times(1)).onShakeStarted();
        verify(shakeListener, times(1)).onShakeFinished();
    }

    @Test
    public void gettingOverMaxShakeTimeStopsFirstShakeAndStartsAnother() throws Exception {
        ShakeListener shakeListener = startShakeDetection();

        long timeStamp = 0;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates
        timeStamp += testProperties.maxShakeTime + 1; //over max shake Time, should start new shake
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates


        verify(shakeListener, times(1)).onShakeStarted();
        verify(shakeListener, times(1)).onShakeFinished();
    }

    @Test
    public void unsetMaxShakeTimeDoesNotStopsFirstShake() throws Exception {
        testProperties.maxShakeTime = 0;
        ShakeListener shakeListener = startShakeDetection(testProperties);

        long timeStamp = 0;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates
        timeStamp += testProperties.maxShakeTime + 1; //over max shake Time, should start new shake
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates

        timeStamp += 5;//Should start another shake, though still accelerating
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates
        timeStamp += testProperties.minShakeTime + 10; //should stop another shake
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move

        verify(shakeListener, times(1)).onShakeStarted();
        verify(shakeListener, times(1)).onShakeFinished();
    }


    @Test
    public void inIdlePeriodAfterShake() throws Exception {
        testProperties.idleDelayAfterShake = 50;
        ShakeListener shakeListener = startShakeDetection();

        long timeStamp = 0;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 5;
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates
        timeStamp += testProperties.minShakeTime + 1; //over min shake Time
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 1, timeStamp);//No move

        timeStamp += 5;//Within idleDelayAfterShake, should not start a new shake
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates

        verify(shakeListener, times(1)).onShakeStarted();
        verify(shakeListener, times(1)).onShakeFinished();
    }

    @Test
    public void notEnoughQuadrantsVisited() throws Exception {
        testProperties.numberOfQuadrantShakeDirectionsNecessary = 2;
        ShakeListener shakeListener = startShakeDetection();

        long timeStamp = 0;
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 0, timeStamp);//No move
        timeStamp += 10;
        callSensorChange(SensorManager.GRAVITY_EARTH, 50, 1, timeStamp);//accelerates
        timeStamp = timeStamp + testProperties.minShakeTime + 10; //above shake time, so gets noticed
        callSensorChange(SensorManager.GRAVITY_EARTH, 0, 1, timeStamp);//No move

        verify(shakeListener, times(1)).onShakeStarted();
        verify(shakeListener, never()).onShakeFinished();
    }
}
