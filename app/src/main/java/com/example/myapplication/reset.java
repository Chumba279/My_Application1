package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class reset extends AppCompatActivity {
    Button btnrest;
    EditText txtresetemail;
    TextView btnrestbacklogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        btnrest = findViewById(R.id.btnreset);
        txtresetemail = findViewById(R.id.txtresetemail);
        btnrestbacklogin = findViewById(R.id.btnrestbacklogin);
        // reset password logic
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        btnrest.setOnClickListener(v -> {

            String email = txtresetemail.getText().toString().trim();

            // Check empty
            if (email.isEmpty()) {

                txtresetemail.setError("Enter Email");
                return;
            }

            // Send reset email
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    getApplicationContext(),
                                    "Reset Email Sent",
                                    Toast.LENGTH_SHORT
                            ).show();

                        } else {

                            Toast.makeText(
                                    getApplicationContext(),
                                    task.getException().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });

        });



        // event to open login
        btnrestbacklogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(reset.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // end of create login
    }
}