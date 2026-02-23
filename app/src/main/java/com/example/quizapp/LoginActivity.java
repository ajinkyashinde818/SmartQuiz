package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        progressBar = findViewById(R.id.progressBar);

        // Login Button Click
        btnLogin.setOnClickListener(v -> loginUser());

        // Redirect to Register
        tvGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) { 
            etEmail.setError("Email is required"); 
            return; 
        }
        if (password.isEmpty()) { 
            etPassword.setError("Password is required"); 
            return; 
        }

        showProgress(true);

        // Firebase Sign In
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fetchUserRole(mAuth.getCurrentUser());
                    } else {
                        showProgress(false);
                        Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserRole(FirebaseUser user) {
        if (user == null) return;
        
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String role = document.getString("role");
                            navigateToDashboard(role);
                        } else {
                            Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error fetching user role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("teacher".equals(role)) {
            intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}