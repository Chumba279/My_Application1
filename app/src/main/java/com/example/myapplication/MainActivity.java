package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button btnforgot,btnlogin,btnbackaccount;
    EditText txtuseremail,txtuserpassword;
    CheckBox chremember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnforgot = findViewById(R.id.btnforgot);
        btnlogin = findViewById(R.id.btnlogin);
        btnbackaccount = findViewById(R.id.btnbackaccount);
        txtuseremail = findViewById(R.id.txtuseremail);
        txtuserpassword = findViewById(R.id.txtuserpassword);
        chremember = findViewById(R.id.chremember);

        // login logic
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        SharedPreferences prefs =
                getSharedPreferences("MyPrefs", MODE_PRIVATE);

// Remember Me Auto Login
        if (prefs.getBoolean("remember", false)) {

            String role = prefs.getString("role", "");

            if (role.equals("admin")) {
                Intent intent  = new Intent(MainActivity.this,AdminDashboard.class);
                startActivity(intent);
                finish();

                // admin

            } else if (role.equals("student")) {

                Intent intent  = new Intent(MainActivity.this,StudentDashboard.class);
                startActivity(intent);
                finish();
                // student


            } else if (role.equals("lecturer")) {

                Intent intent  = new Intent(MainActivity.this,LecturerDashboard.class);
                startActivity(intent);
                finish();
                // lec

            }
        }

        btnlogin.setOnClickListener(v -> {

            String email = txtuseremail.getText().toString().trim();
            String password = txtuserpassword.getText().toString().trim();

            // Empty checks
            if (email.isEmpty()) {
                txtuseremail.setError("Enter Email");
                return;
            }

            if (password.isEmpty()) {
                txtuserpassword.setError("Enter Password");
                return;
            }

            // Login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Format email
                            String formattedEmail = email
                                    .replace(".", "_")
                                    .replace("@", "_at_");
                            // Get role from Firebase
                            FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                                    .getReference("userDetails")
                                    .child(formattedEmail)
                                    .child("role")
                                    .get()
                                    .addOnCompleteListener(roleTask -> {
                                        if (roleTask.isSuccessful()) {
                                            String role = String.valueOf(
                                                    roleTask.getResult().getValue()
                                            );
                                            // Save Remember Me
                                            if (chremember.isChecked()) {
                                                SharedPreferences.Editor editor =
                                                        prefs.edit();
                                                editor.putBoolean("remember", true);
                                                editor.putString("role", role);
                                                editor.apply();
                                            }
                                            // Open activity by role
                                            if (role.equals("admin")) {
                                                // admin
                                                Intent intent  = new Intent(MainActivity.this,AdminDashboard.class);
                                                startActivity(intent);
                                                finish();
                                            } else if (role.equals("student")) {
                                                //student
                                                Intent intent  = new Intent(MainActivity.this,StudentDashboard.class);
                                                startActivity(intent);
                                                finish();

                                            } else if (role.equals("lecturer")) {
                                                //lec
                                                Intent intent  = new Intent(MainActivity.this,LecturerDashboard.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Role not found",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        } else {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Failed to get role",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

        });

        // end of login

        // event to open create account
        btnbackaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this,Register.class);
                startActivity(intent);
                finish();
            }
        });
        // end of create account  event
        btnforgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this,reset.class);
                startActivity(intent);
                finish();
            }
        });
    }
}