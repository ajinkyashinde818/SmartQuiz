package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateQuizActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etSubject, etTimeLimit, etTotalMarks;
    private Button btnNext;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.et_quiz_title);
        etSubject = findViewById(R.id.et_subject);
        etTimeLimit = findViewById(R.id.et_time_limit);
        etTotalMarks = findViewById(R.id.et_total_marks);
        btnNext = findViewById(R.id.btn_next);

        btnNext.setOnClickListener(v -> validateAndProceed());
    }

    private void validateAndProceed() {
        String title = etTitle.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String timeStr = etTimeLimit.getText().toString().trim();
        String marksStr = etTotalMarks.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(timeStr) || TextUtils.isEmpty(marksStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int timeLimit = Integer.parseInt(timeStr);
            int totalMarks = Integer.parseInt(marksStr);
            String quizId = UUID.randomUUID().toString();
            String teacherId = mAuth.getCurrentUser().getUid();

            Map<String, Object> quiz = new HashMap<>();
            quiz.put("quizId", quizId);
            quiz.put("title", title);
            quiz.put("subject", subject);
            quiz.put("teacherId", teacherId);
            quiz.put("timeLimit", timeLimit);
            quiz.put("totalMarks", totalMarks);
            quiz.put("status", "Draft");
            quiz.put("totalAttempts", 0);
            quiz.put("createdAt", FieldValue.serverTimestamp());

            db.collection("quizzes").document(quizId).set(quiz)
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(CreateQuizActivity.this, QuizModeSelectionActivity.class);
                        intent.putExtra("QUIZ_ID", quizId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}
