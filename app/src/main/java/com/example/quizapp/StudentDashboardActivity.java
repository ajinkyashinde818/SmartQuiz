package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class StudentDashboardActivity extends AppCompatActivity {

    private Button btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.btnLogout);

        // Simple Logout logic
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(StudentDashboardActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}