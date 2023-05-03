package com.example.heartrateapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private OutputAnalyzer analyzer;
    private final int REQUEST_CODE_CAMERA = 0;
    public static final int MESSAGE_UPDATE_REALTIME = 1;
    public static final int MESSAGE_UPDATE_FINAL = 2;
    public static final int MESSAGE_CAMERA_NOT_AVAILABLE = 3;
    Dialog instructionDialog;

    // Class responsible for handling messages sent from background threads to the main (UI) thread.
    @SuppressLint("HandlerLeak")
    private final Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            // Processing messages
            // If the message is MESSAGE_UPDATE_REALTIME, then the text of the element displaying the pulse
            if (msg.what == MESSAGE_UPDATE_REALTIME) {
                ((TextView) findViewById(R.id.textView)).setText(msg.obj.toString());
            }
            // If the message is MESSAGE_UPDATE_FINAL, then we show the final results of pulse measurement
            if (msg.what == MESSAGE_UPDATE_FINAL) {
                setViewState(VIEW_STATE.SHOW_RESULTS);
            }
            // Notify the user that there is no access to the camera
            if (msg.what == MESSAGE_CAMERA_NOT_AVAILABLE) {
                Log.println(Log.WARN, "camera", msg.obj.toString());

                ((TextView) findViewById(R.id.textView)).setText(
                        R.string.camera_not_found
                );
                analyzer.stop();
            }
        }
    };

    // The service responsible for interacting with the camera in the Android device to measure heart rate.
    private final CameraService cameraService = new CameraService(this, mainHandler);

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraService.stop();
        if (analyzer != null) analyzer.stop();
        analyzer = new OutputAnalyzer(this, findViewById(R.id.graphTextureView), mainHandler);
    }

    // The onCreate method is triggered when the program starts, then a request to use the camera occurs and a dialog with instructions is displayed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request to use the camera
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE_CAMERA);
        // Call the function that displays instructions for using the application
        showDialog();
    }

    // Method for calling the dialog box
    private void showDialog(){
        // Initialize the instructionDialog to display it on the smartphone screen
        instructionDialog = new Dialog(MainActivity.this);
        // Setting parameters for the message
        instructionDialog.setContentView(R.layout.instruction_dialog);
        instructionDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.instruction_dialog_background));
        instructionDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        instructionDialog.setCancelable(false);
        instructionDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        // Initialize the button in the dialog box
        Button Okay = instructionDialog.findViewById(R.id.btn_okay);

        // Setting instructions for the button
        Okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instructionDialog.dismiss();
            }
        });

        instructionDialog.show();
    }

    // Create settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // The function that processes the selection of buttons in the settings menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            // R.id.info - an element that displays user instructions
            case R.id.info:
                showDialog();
                break;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Snackbar.make(
                        findViewById(R.id.constraintLayout),
                        getString(R.string.cameraPermissionRequired),
                        Snackbar.LENGTH_LONG
                ).show();
            }
        }
    }

    // Method for managing program states
    public void setViewState(VIEW_STATE state) {
        switch (state) {
            // State of heartbeat measurement
            case MEASUREMENT:
                // Make the elements of the heart rate measurement interface visible
                findViewById(R.id.text_instruction).setVisibility(View.VISIBLE);
                findViewById(R.id.gifImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_gif).setVisibility(View.VISIBLE);
                // Hide the button for a new heart rate measurement
                findViewById(R.id.floatingActionButton).setVisibility(View.INVISIBLE);
                break;
            // Results display state
            case SHOW_RESULTS:
                // Again show the button to start heart rate measurement
                findViewById(R.id.floatingActionButton).setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onClickNewMeasurement(View view) {
        onClickNewMeasurement();
    }

    // Method that starts the services for the camera and the analysis of the output data for the heart rate measurement.
    public void onClickNewMeasurement() {
        // Create a new instance of the OutputAnalyzer class, passing the current action, the TextureView that will display the graph, and the mainHandler.
        analyzer = new OutputAnalyzer(this, findViewById(R.id.graphTextureView), mainHandler);

        // Clear the text in the TextView by setting an empty character array to the text.
        char[] empty = new char[0];
        ((TextView) findViewById(R.id.textView)).setText(empty, 0, 0);

        // Set the pulse measurement start state
        setViewState(VIEW_STATE.MEASUREMENT);

        // cameraTextureView - responsible for displaying the camera preview.
        TextureView cameraTextureView = findViewById(R.id.textureView2);

        // Get the SurfaceTexture from the TextureView that we'll use to create the surface we'll use for the camera preview.
        SurfaceTexture previewSurfaceTexture = cameraTextureView.getSurfaceTexture();

        if (previewSurfaceTexture != null) {
            Surface previewSurface = new Surface(previewSurfaceTexture);
            cameraService.start(previewSurface);
            // start measuring heart rate by passing the service that works with the camera and the element that displays the camera readings
            analyzer.measurePulse(cameraTextureView, cameraService);
        }
    }
}