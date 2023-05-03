package com.example.compas;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.MessageFormat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private ImageView compass;
    private float degreeStart = 0f;
    TextView degreeTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compass = findViewById(R.id.compass_image);
        // TextView that will display the degree
        degreeTV = findViewById(R.id.degreeTV);
        // initialize your android device sensor capabilities
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // code for system's orientation sensor registered listeners
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        // get angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        degreeTV.setText(MessageFormat.format("Heading: {0} degrees", Float.toString(degree)));
        // rotation animation - reverse turn degree degrees
        RotateAnimation rotateAnimation = new RotateAnimation(
                degreeStart,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        // set the compass animation after the end of the reservation status
        rotateAnimation.setFillAfter(true);
        // set how long the animation for the compass image will take place
        rotateAnimation.setDuration(210);
        // Start animation of compass image
        compass.startAnimation(rotateAnimation);
        degreeStart = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}