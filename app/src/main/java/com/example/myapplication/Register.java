package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    Button btnbacklogin, btncreate;
    EditText txtfname, txtlname, txtemail, txtpassword, txtconfirmpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnbacklogin = findViewById(R.id.btnbacklogin);
        btncreate = findViewById(R.id.btncreate);
        txtfname = findViewById(R.id.txtfname);
        txtlname = findViewById(R.id.txtlname);
        txtemail = findViewById(R.id.txtemail);
        txtpassword = findViewById(R.id.txtpassword);
        txtconfirmpassword = findViewById(R.id.txtconfirmpassword);

        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // event to open login
        btnbacklogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // end of create login


        btncreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //code to create account
                // first we check fname and email not empty
                // then we check if password match

                String fname = txtfname.getText().toString().trim();
                String lname = txtlname.getText().toString().trim();
                String email = txtemail.getText().toString().trim();
                String password = txtpassword.getText().toString().trim();
                String confirmPassword = txtconfirmpassword.getText().toString().trim();

                // Check empty fields
                if (fname.isEmpty()) {
                    txtfname.setError("Enter first name");
                    return;
                }

                if (email.isEmpty()) {
                    txtemail.setError("Enter email");
                    return;
                }

                // Check password match
                if (!password.equals(confirmPassword)) {
                    txtconfirmpassword.setError("Passwords do not match");
                    return;
                }

                // Create Firebase user
                FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Format email for Firebase key
                                String formattedEmail = email.toLowerCase()
                                        .replace(".", "_")
                                        .replace("@", "_at_");
                                // Current logged in admin/user email
                                // User details
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("firstName", fname);
                                map.put("lastName", lname);
                                map.put("email", email);
                                map.put("createdOn", System.currentTimeMillis());
                                map.put("createdBy", email);
                                map.put("status", "inactive");
                                map.put("role", "student");
                                map.put("deviceId",deviceId);
                                // Save to Realtime Database
                                FirebaseDatabase.getInstance("https://my-application-98e9f-default-rtdb.firebaseio.com/")
                                        .getReference("userDetails")
                                        .child(formattedEmail)
                                        .setValue(map)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(),
                                                        "User Created",
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(Register.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "Database Error",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });
        //end of create
    }
}