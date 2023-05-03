package com.example.stepdetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private boolean running = false;
    private float totalSteps = 0f;
    private float previousTotalSteps = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadData();
        resetSteps();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor == null) {
            Toast.makeText(this,
                    "No sensor detected on this device",
                    Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        TextView stepsTaken = findViewById(R.id.stepsTakenTextView);

        if (running) {
            totalSteps = event.values[0];
            int currentSteps = (int) (totalSteps - previousTotalSteps);
            stepsTaken.setText(String.valueOf(currentSteps));
        }
    }

    public void resetSteps() {
        TextView stepsTaken = findViewById(R.id.stepsTakenTextView);

        stepsTaken.setOnClickListener(
                v -> Toast.makeText(this,
                        "Long tap to reset steps",
                        Toast.LENGTH_SHORT).show());

        stepsTaken.setOnLongClickListener(v -> {
            previousTotalSteps = totalSteps;
            stepsTaken.setText("0");
            saveData();
            return true;
        });
    }

    private void saveData() {
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("key1", previousTotalSteps);
        editor.apply();
    }

    private void loadData() {
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPreferences.getFloat("key1", 0f);

        Log.d("MainActivity", Float.toString(savedNumber));

        previousTotalSteps = savedNumber;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // We do not have to write anything in this function for this app
    }
}