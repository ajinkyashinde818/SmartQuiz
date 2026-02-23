package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private Spinner spinnerDept, spinnerYear;
    private LinearLayout layoutYear;
    private Button btnRegister;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        spinnerDept = findViewById(R.id.spinnerDept);
        spinnerYear = findViewById(R.id.spinnerYear);
        layoutYear = findViewById(R.id.layoutYear);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // Setup Spinners
        setupSpinners();

        // Handle Role Selection (Show/Hide Year)
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbStudent) {
                layoutYear.setVisibility(View.VISIBLE);
            } else {
                layoutYear.setVisibility(View.GONE);
            }
        });

        // Register Button Click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void setupSpinners() {
        // Sample Departments
        String[] departments = {"Computer Technology", "Information Technology", "Electronics & Telecommunication", "Mechanical", "Civil"};
        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDept.setAdapter(deptAdapter);

        // Sample Years
        String[] years = {"1st Year", "2nd Year", "3rd Year",};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
    }

    private void registerUser() {
        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        RadioButton rbRole = findViewById(selectedRoleId);
        final String role = rbRole.getText().toString().toLowerCase();
        
        final String dept = spinnerDept.getSelectedItem().toString();
        
        // Year only if student, otherwise set to "N/A"
        final String year = (selectedRoleId == R.id.rbStudent) ? 
                spinnerYear.getSelectedItem().toString() : "N/A";

        // Validation
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.GONE);

        // Firebase Create User
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 1. Save data to Firestore (Fire and Forget)
                        saveUserToFirestore(name, email, role, dept, year);
                        
                        // 2. Show toast instantly
                        Toast.makeText(RegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        
                        // 3. Move to login page instantly
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegister.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String name, String email, String role, String dept, String year) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;
        
        String uid = currentUser.getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", role);
        userMap.put("department", dept);
        userMap.put("year", year);
        userMap.put("profile_photo", "");

        // We trigger the save operation. Firestore handles persistence automatically, 
        // so it will sync in the background even if we navigate away.
        db.collection("users").document(uid).set(userMap);
    }
}