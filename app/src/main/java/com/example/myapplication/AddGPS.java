package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddGPS extends AppCompatActivity {

    TextView txtlat, txtlong;
    TextInputEditText edtVenueName, edtVenueCode, edtRadius;
    MaterialButton btnAddGps;
    RecyclerView recyclerGps;

    FusedLocationProviderClient fusedLocationClient;

    double latitude = 0.0;
    double longitude = 0.0;

    DatabaseReference gpsRef;

    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gps);

        txtlat = findViewById(R.id.txtlat);
        txtlong = findViewById(R.id.txtlong);

        edtVenueName = findViewById(R.id.edtVenueName);
        edtVenueCode = findViewById(R.id.edtVenueCode);
        edtRadius = findViewById(R.id.edtRadius);

        btnAddGps = findViewById(R.id.btnAddGps);

        recyclerGps = findViewById(R.id.recyclerGps);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        gpsRef = FirebaseDatabase.getInstance(
                        "https://my-application-98e9f-default-rtdb.firebaseio.com/")
                .getReference("GPSVenues");

        getCurrentLocation();

        btnAddGps.setOnClickListener(v -> saveVenue());
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    100
            );

            return;
        }

        LocationRequest locationRequest =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        5000
                ).build();

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(
                    @NonNull LocationResult locationResult) {

                super.onLocationResult(locationResult);

                for (Location location :
                        locationResult.getLocations()) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    txtlat.setText("Lat: " + latitude);
                    txtlong.setText("Long: " + longitude);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                getMainLooper()
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == 100
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
        }
    }

    private void saveVenue() {

        String venueName =
                edtVenueName.getText().toString().trim();

        String venueCode =
                edtVenueCode.getText().toString().trim();

        String radius =
                edtRadius.getText().toString().trim();

        if (venueName.isEmpty()
                || venueCode.isEmpty()
                || radius.isEmpty()) {

            Toast.makeText(
                    this,
                    "Fill all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        HashMap<String, Object> map = new HashMap<>();

        map.put("venueName", venueName);
        map.put("venueCode", venueCode);
        map.put("radius", radius);
        map.put("latitude", latitude);
        map.put("longitude", longitude);

        gpsRef.child(venueCode)
                .setValue(map)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "GPS Venue Saved",
                            Toast.LENGTH_SHORT
                    ).show();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (locationCallback != null) {

            fusedLocationClient.removeLocationUpdates(
                    locationCallback
            );
        }
    }
}