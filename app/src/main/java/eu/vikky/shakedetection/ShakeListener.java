package eu.vikky.shakedetection;

/**
 * @author Viktor Pesek (vikky@vikky.eu)
 * @Copyright 2017
 */
public interface ShakeListener {

    /**
     * Called when shaking was started. Note that shake finished may not be called
     * for shake started, if the shake gets discarded for not fulfilling a property:
     * @see ShakeDetectorProperties#minShakeTime
     */
    void onShakeStarted();

    /**
     * Called when shaking is finished.
     */
    void onShakeFinished();
}
