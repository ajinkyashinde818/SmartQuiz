package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2-second delay using Handler
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserStatus();
            }
        }, 2000);
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, fetch role from Firestore
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String role = document.getString("role");
                                navigateToDashboard(role);
                            } else {
                                // User document doesn't exist, go to Login
                                goToLogin();
                            }
                        } else {
                            Toast.makeText(SplashActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                            goToLogin();
                        }
                    });
        } else {
            // No user is logged in
            goToLogin();
        }
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if ("teacher".equals(role)) {
            intent = new Intent(SplashActivity.this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }
}