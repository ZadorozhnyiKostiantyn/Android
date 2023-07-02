package com.example.gps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_FILE_LOCATION = 99;
    private static final int DEFAULT_UPDATE_INTERVAL = 30;
    TextView tvLat;
    TextView tvLon;
    TextView tvAltitude;
    TextView tvAccuracy;
    TextView tvSpeed;
    TextView tvSensor;
    TextView tvUpdates;
    TextView tvAddress;
    TextView tvWaypointCounts;

    Switch swLocationUpdates;
    Switch swGps;

    Button btnNewWaypoint;
    Button btnShowWaypointList;
    Button btnShowMap;

    // Google's API for location services. The majority of the application using this class
    FusedLocationProviderClient fusedLocationProviderClient;

    // variable to remember if we tracking location or not
    boolean updateOn = false;

    // current location
    Location currentLocation;

    // list of saved locations
    List<Location> savedLocations;

    // Location request is a config file for all settings related to FusedLocationProviderClient
    LocationRequest locationRequest;
    LocationCallback locationCallBack;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // give each UI variable a value

        tvLat = findViewById(R.id.tv_lat);
        tvLon = findViewById(R.id.tv_lon);
        tvAltitude = findViewById(R.id.tv_altitude);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvSpeed = findViewById(R.id.tv_speed);
        tvSensor = findViewById(R.id.tv_sensor);
        tvUpdates = findViewById(R.id.tv_updates);
        tvAddress = findViewById(R.id.tv_address);
        tvWaypointCounts = findViewById(R.id.tv_countOfCrumbs);

        swLocationUpdates = findViewById(R.id.sw_locationsupdates);
        swGps = findViewById(R.id.sw_gps);

        btnNewWaypoint = findViewById(R.id.btn_newWayPoint);
        btnShowWaypointList = findViewById(R.id.btn_showWayPointList);
        btnShowMap = findViewById(R.id.btn_showMap);

        btnNewWaypoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the gps location

                // add the new location to the global list
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.add(currentLocation);
                tvWaypointCounts.setText(Integer.toString(savedLocations.size()));
            }
        });

        btnShowWaypointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationList.class);
                startActivity(i);
            }
        });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        // Set all properties of LocationRequest

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000 * DEFAULT_UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(100)
                .build();

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                updateUIValues(locationResult.getLastLocation());
            }
        };

        swGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(swGps.isChecked()) {
                    // most accurate - use GPS
                    locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
                    tvSensor.setText("Using GPS sensors");
                }
                else {
                    locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);
                    tvSensor.setText("Using Towers + WIFI");
                }
            }
        });


        swLocationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(swLocationUpdates.isChecked()) {
                    // Turn on location tracking
                    startLocationUpdates();
                }
                else {
                    stopLocationUpdates();
                }
            }
        });

        updateGps();
    }

    private void stopLocationUpdates() {
        tvUpdates.setText("Location is NOT being tracking");
        tvLon.setText("Not tracking location");
        tvLat.setText("Not tracking location");
        tvAccuracy.setText("Not tracking location");
        tvSpeed.setText("Not tracking location");
        tvAltitude.setText("Not tracking location");
        tvAddress.setText("Not tracking location");
        tvSensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    private void startLocationUpdates() {
        tvUpdates.setText("Location is being tracking");
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        }

        updateGps();
    }

    private void updateGps(){
        // Get permissions from the user to track GPS
        // Get current location  from the fused client
        // Update UI - i.e set all properties in their associated text view items

        fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(MainActivity.this);

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // User provided the permission

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // We got permission. Put the values of location. XXX into the UI components.
                    updateUIValues(location);
                    currentLocation = location;
                }
            });
        }
        else {
            // Permission not granted yet
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION

                }, PERMISSION_FILE_LOCATION);
            }
        }
    }

    private void updateUIValues(Location location) {
        // update all of the text view objects with a new location

        tvLat.setText(String.valueOf(location.getLatitude()));
        tvLon.setText(String.valueOf(location.getLongitude()));
        tvAccuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()) {
            tvAltitude.setText(String.valueOf(location.getAltitude()));
        }
        else {
            tvAltitude.setText("Not available");
        }

        if(location.hasSpeed()) {
            tvSpeed.setText(String.valueOf(location.getSpeed()));
        }
        else {
            tvSpeed.setText("Not available");
        }


        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
            tvAddress.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception ex) {
            tvAddress.setText("Unable to get street address");
        }

        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();

        // show the number of waypoints saved

        tvWaypointCounts.setText(Integer.toString(savedLocations.size()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case PERMISSION_FILE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGps();
                }
                else {
                    Toast.makeText(this,
                            "This app requires permission to be granted in order to work properly" ,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
}