package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AddCourse extends AppCompatActivity {

    TextInputEditText edtCourseName, edtCourseCode;
    MaterialButton btnAddCourse;

    Spinner spinnerGps, spinnerLecturer;

    ArrayList<String> venueCodes, lecturerEmails;

    ArrayAdapter<String> venueAdapter, lecturerAdapter;

    DatabaseReference gpsRef, userRef, courseRef;

    String selectedVenueCode, selectedLecturerEmail;

    LinearLayout btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        // Inputs
        edtCourseName = findViewById(R.id.edtCourseName);
        edtCourseCode = findViewById(R.id.edtCourseCode);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Spinners
        spinnerGps = findViewById(R.id.spinnerGps);
        spinnerLecturer = findViewById(R.id.spinnerLecturer);

        // Lists
        venueCodes = new ArrayList<>();
        lecturerEmails = new ArrayList<>();

        // ===== VENUE SPINNER =====
        venueAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                venueCodes
        );
        venueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGps.setAdapter(venueAdapter);

        gpsRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                .getReference("GPSVenues");

        loadVenues();

        spinnerGps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVenueCode = venueCodes.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ===== LECTURER SPINNER =====
        lecturerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                lecturerEmails
        );
        lecturerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLecturer.setAdapter(lecturerAdapter);

        userRef = FirebaseDatabase.getInstance(
                "https://my-application-98e9f-default-rtdb.firebaseio.com/"
        ).getReference("userDetails");

        loadLecturers();

        spinnerLecturer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLecturerEmail = lecturerEmails.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ===== COURSES NODE =====
        courseRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                .getReference("Courses");

        // ===== SAVE BUTTON =====
        btnAddCourse.setOnClickListener(v -> saveCourse());
    }

    // ================= SAVE COURSE =================
    private void saveCourse() {

        String courseName = edtCourseName.getText().toString().trim();
        String courseCode = edtCourseCode.getText().toString().trim();

        if (courseName.isEmpty() || courseCode.isEmpty()) {
            Toast.makeText(this, "Enter course details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedVenueCode == null || selectedVenueCode.isEmpty()) {
            Toast.makeText(this, "Please select a venue", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedLecturerEmail == null || selectedLecturerEmail.isEmpty()) {
            Toast.makeText(this, "Please select a lecturer", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("courseName", courseName);
        map.put("courseCode", courseCode);
        map.put("venueCode", selectedVenueCode);
        map.put("lecturerEmail", selectedLecturerEmail);
        map.put("createdAt", System.currentTimeMillis());

        courseRef.child(courseCode)
                .setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();

                    edtCourseName.setText("");
                    edtCourseCode.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ================= LOAD VENUES =================
    private void loadVenues() {

        gpsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                venueCodes.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String code = snap.getKey();
                    if (code != null) venueCodes.add(code);
                }

                venueAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ================= LOAD LECTURERS =================
    private void loadLecturers() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                lecturerEmails.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String role = snap.child("role").getValue(String.class);

                    if ("lecturer".equals(role)) {

                        String email = snap.child("email").getValue(String.class);

                        if (email != null) {
                            lecturerEmails.add(email);
                        }
                    }
                }

                lecturerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}