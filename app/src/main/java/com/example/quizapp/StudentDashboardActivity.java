package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvStudentName;
    private EditText etQuizCode;
    private Button btnJoinQuiz;
    private ImageButton btnLogout;
    private RecyclerView rvRecentQuizzes;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvStudentName = findViewById(R.id.tv_student_name);
        etQuizCode = findViewById(R.id.et_quiz_code);
        btnJoinQuiz = findViewById(R.id.btn_join_quiz);
        btnLogout = findViewById(R.id.btn_logout);
        rvRecentQuizzes = findViewById(R.id.rv_recent_quizzes);

        rvRecentQuizzes.setLayoutManager(new LinearLayoutManager(this));

        loadStudentData();

        btnJoinQuiz.setOnClickListener(v -> {
            String code = etQuizCode.getText().toString().trim();
            if (!code.isEmpty()) {
                // Logic to join quiz
                Toast.makeText(this, "Joining quiz: " + code, Toast.LENGTH_SHORT).show();
            } else {
                etQuizCode.setError("Enter code");
            }
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(StudentDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void loadStudentData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            tvStudentName.setText(name != null ? name : "Student");
                        }
                    });
        }
    }
}