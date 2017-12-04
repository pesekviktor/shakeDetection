package eu.vikky.shakedetection;

import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements ShakeListener {

    private static final String WAITING = "Waiting for shake";
    private ShakeDetector shakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shakeDetector = new ShakeDetector((SensorManager) getSystemService(SENSOR_SERVICE));
        try {
            shakeDetector.startListening(this);
        } catch (AccelometerNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onShakeStarted() {
    }

    @Override
    public void onShakeFinished() {
        final TextView shakeText = findViewById(R.id.shakeText);
        shakeText.setText("Shake detected");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                shakeText.setText(WAITING);
            }
        }, 2000);
    }
}
