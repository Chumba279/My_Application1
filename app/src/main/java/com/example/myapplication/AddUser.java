package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddUser extends AppCompatActivity {

    TextView txtTitle;
    TextInputEditText edtFirstName, edtLastName, edtEmail, edtPassword;
    MaterialButton btnAddUser, btnBack;

    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        role = getIntent().getStringExtra("role");
        if (role == null) role = "student";

        txtTitle = findViewById(R.id.txtTitle);
        edtFirstName = findViewById(R.id.edtFirstName);
        edtLastName = findViewById(R.id.edtLastName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnAddUser = findViewById(R.id.btnAddUser);
        btnBack = findViewById(R.id.btnBack);

        txtTitle.setText("Add " + role.substring(0, 1).toUpperCase() + role.substring(1));

        btnAddUser.setOnClickListener(v -> saveUser());
        btnBack.setOnClickListener(v -> finish());
    }

    private void saveUser() {
        String fname = edtFirstName.getText().toString().trim();
        String lname = edtLastName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (fname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize secondary app to avoid logging out the current admin
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApiKey("AIzaSyDS0Vq7A1MX6K_7vyFue_0Gk5seNiT_HBQ")
                .setApplicationId("1:846954442204:android:8a1e3704181179bbebb319")
                .setDatabaseUrl("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                .build();

        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("secondary");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.initializeApp(this, options, "secondary");
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String formattedEmail = email.toLowerCase().replace(".", "_").replace("@", "_at_");

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("firstName", fname);
                        map.put("lastName", lname);
                        map.put("email", email);
                        map.put("role", role);
                        map.put("status", "active");
                        map.put("createdOn", System.currentTimeMillis());

                        FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                                .getReference("userDetails")
                                .child(formattedEmail)
                                .setValue(map)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, role + " added successfully", Toast.LENGTH_SHORT).show();
                                    secondaryAuth.signOut(); // Sign out the new user from secondary instance
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Auth Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
