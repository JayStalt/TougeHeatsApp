// MainActivity.java
package com.example.tougeheatsapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private Button btnStartStop;  // Button to start/stop route tracking
    private TextView tvStats;     // TextView to display tracking information

    // Constants for permissions
    private static final int PERMISSION_REQUEST_LOCATION = 1;

    // Variables for tracking time
    private long startTime, endTime;
    private boolean isTracking = false; // Flag to toggle tracking status

    // Location and database components
    private LocationManager locationManager; // Manages GPS location services
    private LocationListener locationListener; // Listens for location changes
    private DatabaseHelper dbHelper; // Helper for database operations
    private StringBuilder gpsData = new StringBuilder(); // Stores GPS coordinates as a string

    // Add TAG as a constant
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: MainActivity launched successfully");

        setContentView(R.layout.activity_main); // Sets the UI layout for this activity

        // Link XML UI elements to Java code
        btnStartStop = findViewById(R.id.btnStartStop);
        tvStats = findViewById(R.id.tvStats);
        dbHelper = new DatabaseHelper(this); // Initialize database helper

        // Set up button click listener to toggle tracking state
        btnStartStop.setOnClickListener(v -> {
            if (!isTracking) {
                startTracking(); // Start location tracking
                btnStartStop.setText("Stop Route"); // Update button text
                tvStats.setText("Tracking started..."); // Update text view
                Log.d(TAG, "btnStartStop: Tracking started");
            } else {
                stopTracking(); // Stop location tracking
                btnStartStop.setText("Start Route"); // Update button text
                tvStats.setText("Tracking stopped."); // Update text view
                Log.d(TAG, "btnStartStop: Tracking stopped");
            }
            isTracking = !isTracking; // Toggle the tracking state
        });
    }

    // Method to start tracking route
    private void startTracking() {
        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not already granted
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISSION_REQUEST_LOCATION);
            return; // Exit method until permissions are granted
        }

        // Permission has been granted, proceed with starting tracking
        startTime = System.currentTimeMillis(); // Record start time
        gpsData.setLength(0); // Clear previous GPS data

        // Set up location manager and listener for GPS tracking
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Append latitude and longitude to gpsData
                gpsData.append(location.getLatitude())
                        .append(", ")
                        .append(location.getLongitude())
                        .append(";");

                // Update the TextView with real-time GPS coordinates and speed
                String speedText = "Speed: " + location.getSpeed() + " m/s\n";
                String coordinatesText = "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude();
                tvStats.setText(speedText + coordinatesText);

                // Log the updates for debugging
                Log.d(TAG, "onLocationChanged: " + coordinatesText);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };

        // Request location updates every 2 seconds or 5 meters
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, locationListener);
    }

    // Method to stop tracking route and save data
    private void stopTracking() {
        Log.d(TAG, "stopTracking: Called"); // Debug start of method

        endTime = System.currentTimeMillis(); // Record end time
        long elapsedTime = endTime - startTime; // Calculate elapsed time
        Log.d(TAG, "stopTracking: Elapsed Time = " + elapsedTime);

        // Stop receiving location updates
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            Log.d(TAG, "stopTracking: Location updates removed");
        }

        // Prepare data to save to the database
        String routeName = "My Route"; // Default name for the route
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Log.d(TAG, "stopTracking: Preparing to save data. GPS Data = " + gpsData.toString());

        try {
            // Save route data to the database
            dbHelper.insertRoute(routeName, startTime, endTime, gpsData.toString(), date);
            Log.d(TAG, "stopTracking: Route saved to database");
        } catch (Exception e) {
            Log.e(TAG, "stopTracking: Error saving route to database", e);
        }

        // Update the TextView
        tvStats.setText("Stopped tracking.\nElapsed Time: " + (elapsedTime / 1000) + " seconds");
        Log.d(TAG, "stopTracking: Method completed");
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, start tracking
                startTracking();
            } else {
                // Permission denied, notify the user
                tvStats.setText("Location permission denied. Tracking unavailable.");
            }
        }
    }
}
