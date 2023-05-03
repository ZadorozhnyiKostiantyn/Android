package com.example.sensor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private boolean color = false;
    private long lastUpdate;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        textView = findViewById(R.id.textView);
        textView.setBackgroundColor(getResources().getColor(R.color.peach_puff));
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            getAccelerometer(sensorEvent);
        }
    }

    private void getAccelerometer(SensorEvent sensorEvent){
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];

        float accelerationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = sensorEvent.timestamp;

        if(accelerationSquareRoot >= 2) {
            if(actualTime - lastUpdate < 200){
                return;
            }

            lastUpdate = actualTime;
            Toast.makeText(this,
                    "Device was shake",
                    Toast.LENGTH_SHORT).show();
            if(color){
                textView.setBackgroundColor(getResources().getColor(R.color.peach_puff));
            }
            else{
                textView.setBackgroundColor(getResources().getColor(R.color.light_sky_blue));
            }
            color = !color;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
