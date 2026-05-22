package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class StudentCourses extends AppCompatActivity {
    AutoCompleteTextView edtSearchCourse;

    TextView txtCourseName, txtCourseCode, txtCourseLecturer, txtCourseVenue;
    Button btnEnroll;

    DatabaseReference courseRef, enrollRef;

    ArrayList<String> courseCodes;
    ArrayAdapter<String> adapter;

    String selectedCourseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_courses);




        // UI
        edtSearchCourse = findViewById(R.id.edtSearchCourse);

        txtCourseName = findViewById(R.id.txtCourseName);
        txtCourseCode = findViewById(R.id.txtCourseCode);
        txtCourseLecturer = findViewById(R.id.txtCourseLecturer);
        txtCourseVenue = findViewById(R.id.txtCourseVenue);

        btnEnroll = findViewById(R.id.btnEnroll);

        // Firebase
        courseRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("Courses");
        enrollRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference("Enrollments");

        // List
        courseCodes = new ArrayList<>();

        // ADAPTER (attach FIRST)
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                courseCodes
        );

        edtSearchCourse.setAdapter(adapter);

        // Load data
        loadCourseList();

        // Select course
        edtSearchCourse.setOnItemClickListener((parent, view, position, id) -> {

            selectedCourseCode = adapter.getItem(position);

            loadCourseDetails(selectedCourseCode);
        });

        // Enroll button
        btnEnroll.setOnClickListener(v -> enrollStudent());
    }
    // ================= LOAD COURSE LIST =================
    private void loadCourseList() {

        courseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseCodes.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {

                    String code = snap.getKey();
                    if (code != null) {
                        courseCodes.add(code);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // ================= LOAD COURSE DETAILS =================
    private void loadCourseDetails(String courseCode) {

        courseRef.child(courseCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("courseName").getValue() != null
                                ? snapshot.child("courseName").getValue().toString()
                                : "N/A";
                        String code = snapshot.child("courseCode").getValue() != null
                                ? snapshot.child("courseCode").getValue().toString()
                                : "N/A";
                        String lecturer = snapshot.child("lecturerEmail").getValue() != null
                                ? snapshot.child("lecturerEmail").getValue().toString()
                                : "N/A";

                        String venue = snapshot.child("venueCode").getValue() != null
                                ? snapshot.child("venueCode").getValue().toString()
                                : "N/A";
                        txtCourseName.setText(name);
                        txtCourseCode.setText("Code: " + code);
                        txtCourseLecturer.setText("Lecturer: " + lecturer);
                        txtCourseVenue.setText("Venue: " + venue);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // ================= ENROLL STUDENT =================
    private void enrollStudent() {

        if (selectedCourseCode == null) {
            Toast.makeText(this, "Select a course first", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentEmail = user.getEmail();
        if (studentEmail == null) return;
        
        String studentId = studentEmail.toLowerCase().replace(".", "_")
                .replace("@", "_at_");

        HashMap<String, Object> map = new HashMap<>();
        map.put("courseCode", selectedCourseCode);
        map.put("studentEmail", studentEmail);
        map.put("status", "enrolled");
        map.put("enrolledAt", System.currentTimeMillis());

        enrollRef.child(selectedCourseCode)
                .child(studentId)
                .setValue(map)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Enrolled successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to dashboard automatically
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}