package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboard extends AppCompatActivity {
    LinearLayout btnopenaddgps, btnopenaddcourse, btnopenaddstudent, btnopenaddlecturer, btnopenaddadmin;
    MaterialButton btnAdminLogout;
    TextView txtTotalStudents, txtTotalAdmins, txtTotalLecturers, txtTotalCourses, txtTotalGps;

    DatabaseReference dbRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);      
        btnopenaddgps = findViewById(R.id.btnopenaddgps) ;
        btnopenaddcourse = findViewById(R.id.btnopenaddcourse);
        btnopenaddstudent = findViewById(R.id.btnopenaddstudent);
        btnopenaddlecturer = findViewById(R.id.btnopenaddlecturer);
        btnopenaddadmin = findViewById(R.id.btnopenaddadmin);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        txtTotalStudents = findViewById(R.id.txtTotalStudents);
        txtTotalAdmins = findViewById(R.id.txtTotalAdmins);
        txtTotalLecturers = findViewById(R.id.txtTotalLecturers);
        txtTotalCourses = findViewById(R.id.txtTotalCourses);
        txtTotalGps = findViewById(R.id.txtTotalGps);

        dbRef = FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/").getReference();

        loadStats();

        btnopenaddgps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(AdminDashboard.this, AddGPS.class);
                startActivity(intent);
            }
        });

        btnopenaddcourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(AdminDashboard.this,AddCourse.class);
                startActivity(intent);
            }
        });

        btnopenaddstudent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AddUser.class);
            intent.putExtra("role", "student");
            startActivity(intent);
        });

        btnopenaddlecturer.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AddUser.class);
            intent.putExtra("role", "lecturer");
            startActivity(intent);
        });

        btnopenaddadmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboard.this, AddUser.class);
            intent.putExtra("role", "admin");
            startActivity(intent);
        });


        btnAdminLogout.setOnClickListener(v -> logoutUser());

    }

    private void loadStats() {
        // Users
        dbRef.child("userDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int students = 0, lecturers = 0, admins = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Object roleObj = snap.child("role").getValue();
                    String role = (roleObj != null) ? String.valueOf(roleObj) : "";
                    
                    if ("student".equalsIgnoreCase(role)) students++;
                    else if ("lecturer".equalsIgnoreCase(role)) lecturers++;
                    else if ("admin".equalsIgnoreCase(role)) admins++;
                }
                txtTotalStudents.setText(String.valueOf(students));
                txtTotalLecturers.setText(String.valueOf(lecturers));
                txtTotalAdmins.setText(String.valueOf(admins));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Courses
        dbRef.child("Courses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtTotalCourses.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // GPS
        dbRef.child("GPSVenues").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                txtTotalGps.setText(String.valueOf(snapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    // logout logic
    private void logoutUser() {

        // Firebase logout
        FirebaseAuth.getInstance().signOut();

        // Clear Remember Me
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear(); // removes remember + role
        editor.apply();

        // Go back to login screen
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
}
