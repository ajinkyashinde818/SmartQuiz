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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
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

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.GONE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateFCMTokenAndFetchRole();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFCMTokenAndFetchRole() {
        String uid = mAuth.getCurrentUser().getUid();
        
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        db.collection("users").document(uid).update("fcmToken", token);
                    }
                    fetchUserRole();
                });
    }

    private void fetchUserRole() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String role = document.getString("role");
                            navigateToDashboard(role);
                        } else {
                            Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setVisibility(View.VISIBLE);
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
}