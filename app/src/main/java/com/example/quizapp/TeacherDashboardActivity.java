package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.quizapp.models.Quiz;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherName, tvTotalQuizzes, tvTotalQuestions, tvTotalAttempts;
    private CardView btnCreateQuiz, btnManageQuizzes, btnViewResults, btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews
        tvTeacherName = findViewById(R.id.tv_teacher_name);
        tvTotalQuizzes = findViewById(R.id.tv_total_quizzes);
        tvTotalQuestions = findViewById(R.id.tv_total_questions);
        tvTotalAttempts = findViewById(R.id.tv_total_attempts);

        // Initialize Cards
        btnCreateQuiz = findViewById(R.id.btn_create_quiz);
        btnManageQuizzes = findViewById(R.id.btn_manage_quizzes);
        btnViewResults = findViewById(R.id.btn_view_results);
        btnLogout = findViewById(R.id.btn_logout);

        loadTeacherData();
        setupClickListeners();
    }

    private void loadTeacherData() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            tvTeacherName.setText(name != null ? name : "Teacher");
                        }
                    });

            // Fetch real counts
            db.collection("quizzes")
                    .whereEqualTo("teacherId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int quizCount = queryDocumentSnapshots.size();
                        int totalQuestions = 0;
                        int totalAttempts = 0;

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Quiz quiz = doc.toObject(Quiz.class);
                            if (quiz != null) {
                                // totalMarks is used to store the question count
                                totalQuestions += quiz.getTotalMarks();
                                totalAttempts += quiz.getTotalAttempts();
                            }
                        }

                        tvTotalQuizzes.setText(String.valueOf(quizCount));
                        tvTotalQuestions.setText(String.valueOf(totalQuestions));
                        tvTotalAttempts.setText(String.valueOf(totalAttempts));
                    });
        }
    }

    private void setupClickListeners() {
        btnCreateQuiz.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, CreateQuizActivity.class));
        });

        btnManageQuizzes.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, ManageQuizActivity.class));
        });

        btnViewResults.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, ResultActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(TeacherDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherData(); // Refresh stats when returning to dashboard
    }
}