package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class LecturerDashboard extends AppCompatActivity {

    TextView edtDate, txtTime, txtSessionStatus, txtAttendanceCount;
    Button btnLogout, btnOpenSession;

    Spinner spinnerCourse, spinnerDuration;

    ArrayList<String> courseList;
    ArrayAdapter<String> courseAdapter, durationAdapter;

    RecyclerView recyclerMarkedAttendance;
    AttendanceAdapter attendanceAdapter;
    ArrayList<Map<String, Object>> attendanceList;

    DatabaseReference courseRef, sessionRef, attendanceRef;

    String selectedCourse, selectedDate, selectedTime;
    int selectedDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_dashboard);

        // ================= UI =================
        edtDate = findViewById(R.id.edtDate);
        txtTime = findViewById(R.id.txtTime);
        txtSessionStatus = findViewById(R.id.txtSessionStatus);
        txtAttendanceCount = findViewById(R.id.txtAttendanceCount);

        btnLogout = findViewById(R.id.btnLogout);
        btnOpenSession = findViewById(R.id.btnOpenSession);

        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerDuration = findViewById(R.id.spinnerDuration);

        // ================= RECYCLER VIEW =================
        recyclerMarkedAttendance = findViewById(R.id.recyclerMarkedAttendance);
        attendanceList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        recyclerMarkedAttendance.setLayoutManager(new LinearLayoutManager(this));
        recyclerMarkedAttendance.setAdapter(attendanceAdapter);

        // ================= FIREBASE =================
        courseRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("Courses");
        sessionRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("CourseSessions");
        attendanceRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("Attendance");

        // ================= COURSE LIST =================
        courseList = new ArrayList<>();

        courseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                courseList
        );
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(courseAdapter);

        loadCourses();
        loadActiveSessionAndAttendance();

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courseList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ================= DATE PICKER =================
        edtDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            DatePickerDialog dp = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDate = day + "/" + (month + 1) + "/" + year;
                        edtDate.setText(selectedDate);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));

            dp.show();
        });

        // ================= TIME PICKER =================
        txtTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();

            TimePickerDialog tp = new TimePickerDialog(this,
                    (view, hour, minute) -> {
                        selectedTime = String.format("%02d:%02d", hour, minute);
                        txtTime.setText(selectedTime);
                    },
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    true);

            tp.show();
        });

        // ================= DURATION =================
        String[] durations = {"10", "20", "30", "40", "50", "60"};

        durationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                durations
        );

        spinnerDuration.setAdapter(durationAdapter);

        spinnerDuration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDuration = Integer.parseInt(durations[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ================= OPEN SESSION =================
        btnOpenSession.setOnClickListener(v -> openSession());

        // ================= LOGOUT =================
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    // ================= LOAD ACTIVE SESSION & ATTENDANCE =================
    private void loadActiveSessionAndAttendance() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String lecturerEmail = user.getEmail();

        sessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean activeSessionFound = false;
                String activeCourseCode = "";
                String activeSessionId = "";

                for (DataSnapshot courseSnap : snapshot.getChildren()) {
                    for (DataSnapshot sessionSnap : courseSnap.getChildren()) {
                        
                        Object statusObj = sessionSnap.child("status").getValue();
                        String status = (statusObj != null) ? String.valueOf(statusObj) : "";
                        
                        Object emailObj = sessionSnap.child("lecturerEmail").getValue();
                        String email = (emailObj != null) ? String.valueOf(emailObj) : "";

                        if ("open".equals(status) && lecturerEmail != null && lecturerEmail.equalsIgnoreCase(email)) {
                            Object ccObj = sessionSnap.child("courseCode").getValue();
                            activeCourseCode = (ccObj != null) ? String.valueOf(ccObj) : "";
                            
                            activeSessionId = sessionSnap.getKey();
                            activeSessionFound = true;
                            
                            txtSessionStatus.setText("Status: LIVE (" + activeCourseCode + ")");
                            txtSessionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            break;
                        }
                    }
                    if (activeSessionFound) break;
                }

                if (activeSessionFound) {
                    listenForAttendance(activeCourseCode, activeSessionId);
                } else {
                    txtSessionStatus.setText("Status: No Active Session");
                    txtSessionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    txtAttendanceCount.setText("Total Present: 0");
                    attendanceList.clear();
                    attendanceAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenForAttendance(String courseCode, String sessionId) {
        attendanceRef.child(courseCode).child(sessionId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                attendanceList.clear();
                for (DataSnapshot studentSnap : snapshot.getChildren()) {
                    Map<String, Object> data = (Map<String, Object>) studentSnap.getValue();
                    if (data != null) {
                        attendanceList.add(data);
                    }
                }
                txtAttendanceCount.setText("Total Present: " + attendanceList.size());
                attendanceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ================= LOAD COURSES =================
    private void loadCourses() {

        courseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                courseList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String code = snap.getKey();
                    if (code != null) courseList.add(code);
                }

                courseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ================= OPEN SESSION =================
    private void openSession() {

        if (selectedCourse == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String lecturerEmail = user.getEmail();

        courseRef.child(selectedCourse)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Object venueObj = snapshot.child("venueCode").getValue();
                        String venueCode = (venueObj != null) ? String.valueOf(venueObj) : "";

                        String sessionId = "Session_" + System.currentTimeMillis();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("courseCode", selectedCourse);
                        map.put("lecturerEmail", lecturerEmail);
                        map.put("venueCode", venueCode);
                        map.put("date", selectedDate);
                        map.put("time", selectedTime);
                        map.put("duration", selectedDuration);
                        map.put("status", "open");
                        map.put("openedAt", System.currentTimeMillis());

                        sessionRef.child(selectedCourse)
                                .child(sessionId)
                                .setValue(map)
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(LecturerDashboard.this,
                                                "Session Opened",
                                                Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(LecturerDashboard.this,
                                                e.getMessage(),
                                                Toast.LENGTH_SHORT).show()
                                );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // ================= LOGOUT =================
    private void logoutUser() {

        FirebaseAuth.getInstance().signOut();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
}
