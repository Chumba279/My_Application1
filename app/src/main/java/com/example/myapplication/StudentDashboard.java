package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentDashboard extends AppCompatActivity {

    Button btnLogout, btnMyCourses, btnMarkAttendance;
    TextView txtdeviceid, txtStatus, txtrealdeviceID, txtGps;
    TextView txtSessionStatus, txtCourseName, txtVenue, txtTimer, txtAttendanceDetected, txtLecturerEmail, txtVenueGpsInfo;

    RecyclerView recyclerMyCourses;
    CourseAdapter courseAdapter;
    ArrayList<Map<String, Object>> enrolledCoursesList;

    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    double studentLat = 0.0;
    double studentLong = 0.0;

    // Active Session Data
    String activeCourseCode = null;
    String activeSessionId = null;
    String activeVenueCode = null;
    double activeVenueLat = 0.0;
    double activeVenueLong = 0.0;
    double activeVenueRadius = 0.0;
    boolean isSessionActive = false;
    boolean isAttendanceMarked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize UI
        btnLogout = findViewById(R.id.btnLogout);
        btnMyCourses = findViewById(R.id.btnMyCourses);
        txtdeviceid = findViewById(R.id.txtdeviceid);
        txtStatus = findViewById(R.id.txtStatus);
        txtrealdeviceID = findViewById(R.id.txtrealdeviceID);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        txtGps = findViewById(R.id.txtGps);

        txtSessionStatus = findViewById(R.id.txtSessionStatus);
        txtCourseName = findViewById(R.id.txtCourseName);
        txtVenue = findViewById(R.id.txtVenue);
        txtTimer = findViewById(R.id.txtTimer);
        txtAttendanceDetected = findViewById(R.id.txtAttendanceDetected);
        txtLecturerEmail = findViewById(R.id.txtLecturerEmail);
        txtVenueGpsInfo = findViewById(R.id.txtVenueGpsInfo);

        // Recycler View Setup
        recyclerMyCourses = findViewById(R.id.recyclerMyCourses);
        enrolledCoursesList = new ArrayList<>();
        courseAdapter = new CourseAdapter(enrolledCoursesList);
        recyclerMyCourses.setLayoutManager(new LinearLayoutManager(this));
        recyclerMyCourses.setAdapter(courseAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get device ID
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        txtdeviceid.setText(deviceId);

        getCurrentLocation();
        loadActiveSession();
        loadEnrolledCourses();

        // Check Device ID in Database
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String loggedinuseremail = user.getEmail();
            txtStatus.setText(loggedinuseremail);
            String formatedemail = loggedinuseremail.replace(".", "_")
                    .replace("@", "_at_");

            DatabaseReference refdeviceid = FirebaseDatabase
                    .getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                    .getReference("userDetails");

            refdeviceid.child(formatedemail).child("deviceId")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Object idObj = snapshot.getValue();
                                String deviceidFromDb = (idObj != null) ? String.valueOf(idObj) : "";
                                
                                if (!deviceidFromDb.isEmpty() && deviceidFromDb.equals(deviceId)) {
                                    txtrealdeviceID.setText(deviceidFromDb);
                                } else {
                                    txtrealdeviceID.setText("Not your device");
                                    btnMarkAttendance.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                txtStatus.setText("No device id found");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(StudentDashboard.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // ================= MARK ATTENDANCE =================
        btnMarkAttendance.setOnClickListener(v -> markAttendance());

        // ================= MY COURSES =================
        btnMyCourses.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboard.this, StudentCourses.class);
            startActivity(intent);
        });

        // ================= LOGOUT =================
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent intent = new Intent(StudentDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadEnrolledCourses() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String studentId = user.getEmail().toLowerCase().replace(".", "_").replace("@", "_at_");
        DatabaseReference enrollRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("Enrollments");

        enrollRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                enrolledCoursesList.clear();
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    if (courseSnap.hasChild(studentId)) {
                        DataSnapshot studentEnrollSnap = courseSnap.child(studentId);
                        Map<String, Object> courseData = (Map<String, Object>) studentEnrollSnap.getValue();
                        if (courseData != null) {
                            enrolledCoursesList.add(courseData);
                        }
                    }
                }
                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadActiveSession() {
        DatabaseReference sessionRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("CourseSessions");

        sessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isSessionActive = false;
                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    for (DataSnapshot sessionSnap : courseSnap.getChildren()) {
                        
                        Object statusObj = sessionSnap.child("status").getValue();
                        String status = (statusObj != null) ? String.valueOf(statusObj) : "";
                        
                        if ("open".equals(status)) {
                            Object ccObj = sessionSnap.child("courseCode").getValue();
                            activeCourseCode = (ccObj != null) ? String.valueOf(ccObj) : "";
                            
                            activeSessionId = sessionSnap.getKey();
                            
                            Object vcObj = sessionSnap.child("venueCode").getValue();
                            activeVenueCode = (vcObj != null) ? String.valueOf(vcObj) : "";
                            
                            Object timeObj = sessionSnap.child("time").getValue();
                            String time = (timeObj != null) ? String.valueOf(timeObj) : "";
                            
                            Object lecObj = sessionSnap.child("lecturerEmail").getValue();
                            String lecturer = (lecObj != null) ? String.valueOf(lecObj) : "";

                            txtSessionStatus.setText("Status: LIVE");
                            txtCourseName.setText("Course: " + activeCourseCode);
                            txtVenue.setText("Venue: " + activeVenueCode);
                            txtTimer.setText("Time: " + time);
                            txtLecturerEmail.setText("Lecturer: " + lecturer);
                            
                            isSessionActive = true;
                            loadVenueDetails(activeVenueCode);
                            checkIfAttendanceMarked();
                            break;
                        }
                    }
                    if (isSessionActive) break;
                }

                if (!isSessionActive) {
                    txtSessionStatus.setText("Status: No Active Session");
                    txtCourseName.setText("Course: N/A");
                    txtVenue.setText("Venue: N/A");
                    txtTimer.setText("Time: --:--");
                    txtLecturerEmail.setText("Lecturer: N/A");
                    txtAttendanceDetected.setText("Attendance: N/A");
                    txtVenueGpsInfo.setText("Venue GPS: --, -- (Radius: --m)");
                    btnMarkAttendance.setEnabled(false);
                } else {
                    btnMarkAttendance.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadVenueDetails(String venueCode) {
        DatabaseReference venueRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("GPSVenues").child(venueCode);
        venueRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Safety check for Latitude
                    Object latObj = snapshot.child("latitude").getValue();
                    if (latObj instanceof Number) {
                        activeVenueLat = ((Number) latObj).doubleValue();
                    } else if (latObj != null) {
                        try { activeVenueLat = Double.parseDouble(String.valueOf(latObj)); } catch (Exception e) { activeVenueLat = 0.0; }
                    }

                    // Safety check for Longitude
                    Object lngObj = snapshot.child("longitude").getValue();
                    if (lngObj instanceof Number) {
                        activeVenueLong = ((Number) lngObj).doubleValue();
                    } else if (lngObj != null) {
                        try { activeVenueLong = Double.parseDouble(String.valueOf(lngObj)); } catch (Exception e) { activeVenueLong = 0.0; }
                    }

                    // Safety check for Radius
                    Object radObj = snapshot.child("radius").getValue();
                    if (radObj instanceof Number) {
                        activeVenueRadius = ((Number) radObj).doubleValue();
                    } else if (radObj != null) {
                        try {
                            activeVenueRadius = Double.parseDouble(String.valueOf(radObj));
                        } catch (Exception e) {
                            activeVenueRadius = 50.0;
                        }
                    } else {
                        activeVenueRadius = 50.0;
                    }

                    txtVenueGpsInfo.setText("Venue GPS: " + activeVenueLat + ", " + activeVenueLong + " (Radius: " + activeVenueRadius + "m)");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkIfAttendanceMarked() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || activeCourseCode == null || activeSessionId == null) return;

        String studentId = user.getEmail().replace(".", "_").replace("@", "_at_");
        DatabaseReference attRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                .getReference("Attendance").child(activeCourseCode).child(activeSessionId).child(studentId);

        attRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isAttendanceMarked = true;
                    txtAttendanceDetected.setText("Attendance: MARKED ✅");
                    btnMarkAttendance.setVisibility(View.GONE);
                } else {
                    isAttendanceMarked = false;
                    txtAttendanceDetected.setText("Attendance: NOT MARKED ❌");
                    btnMarkAttendance.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void markAttendance() {
        if (!isSessionActive) {
            Toast.makeText(this, "No active session", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isAttendanceMarked) return;

        if (studentLat == 0.0 || studentLong == 0.0) {
            Toast.makeText(this, "Waiting for GPS...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate Distance
        float[] results = new float[1];
        Location.distanceBetween(studentLat, studentLong, activeVenueLat, activeVenueLong, results);
        float distanceInMeters = results[0];

        if (distanceInMeters > activeVenueRadius) {
            Toast.makeText(this, "Too far! You are " + (int) distanceInMeters + "m away. Must be within " + (int) activeVenueRadius + "m", Toast.LENGTH_LONG).show();
            return;
        }

        // Check Enrollment
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String studentEmail = user.getEmail();
        String studentId = studentEmail.replace(".", "_").replace("@", "_at_");

        DatabaseReference enrollRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                .getReference("Enrollments").child(activeCourseCode).child(studentId);

        enrollRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Mark Attendance
                    DatabaseReference attRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                            .getReference("Attendance").child(activeCourseCode).child(activeSessionId).child(studentId);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("studentEmail", studentEmail);
                    map.put("timestamp", System.currentTimeMillis());
                    map.put("status", "present");
                    map.put("distance", distanceInMeters);
                    map.put("studentLat", studentLat);
                    map.put("studentLong", studentLong);
                    map.put("venueLat", activeVenueLat);
                    map.put("venueLong", activeVenueLong);

                    attRef.setValue(map).addOnSuccessListener(unused -> {
                        Toast.makeText(StudentDashboard.this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    studentLat = location.getLatitude();
                    studentLong = location.getLongitude();
                    txtGps.setText("Your Live GPS: " + studentLat + ", " + studentLong);

                    // AUTO MARK ATTENDANCE LOGIC
                    if (isSessionActive && !isAttendanceMarked && activeVenueLat != 0.0) {
                        float[] results = new float[1];
                        Location.distanceBetween(studentLat, studentLong, activeVenueLat, activeVenueLong, results);
                        float distance = results[0];
                        
                        // If within the venue's defined radius, mark attendance automatically
                        if (distance <= activeVenueRadius) {
                            markAttendance();
                        }
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
