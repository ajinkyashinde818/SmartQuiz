package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

    private TextInputEditText etTitle, etSubject, etTimeLimit, etTotalQuestions;
    private Spinner spinnerYear, spinnerDivision;
    private Button btnNext;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String teacherName = "Teacher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.et_quiz_title);
        etSubject = findViewById(R.id.et_subject);
        etTimeLimit = findViewById(R.id.et_time_limit);
        etTotalQuestions = findViewById(R.id.et_total_questions);
        spinnerYear = findViewById(R.id.spinner_year);
        spinnerDivision = findViewById(R.id.spinner_division);
        btnNext = findViewById(R.id.btn_next);

        setupSpinners();
        fetchTeacherName();

        btnNext.setOnClickListener(v -> validateAndProceed());
    }

    private void fetchTeacherName() {
        if (mAuth.getCurrentUser() != null) {
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            teacherName = documentSnapshot.getString("name");
                        }
                    });
        }
    }

    private void setupSpinners() {
        String[] years = {"1st Year", "2nd Year", "3rd Year"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        String[] divisions = {"A", "B"};
        ArrayAdapter<String> divAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
        divAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivision.setAdapter(divAdapter);
    }

    private void validateAndProceed() {
        if (etTitle == null || etSubject == null || etTimeLimit == null || etTotalQuestions == null) {
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String timeStr = etTimeLimit.getText().toString().trim();
        String questionsStr = etTotalQuestions.getText().toString().trim();
        String year = spinnerYear.getSelectedItem().toString();
        String division = spinnerDivision.getSelectedItem().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(timeStr) || TextUtils.isEmpty(questionsStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int timeLimit = Integer.parseInt(timeStr);
            int totalQuestions = Integer.parseInt(questionsStr);
            String quizId = UUID.randomUUID().toString();
            String teacherId = mAuth.getCurrentUser().getUid();

            Map<String, Object> quiz = new HashMap<>();
            quiz.put("quizId", quizId);
            quiz.put("title", title);
            quiz.put("subject", subject);
            quiz.put("year", year);
            quiz.put("division", division);
            quiz.put("teacherId", teacherId);
            quiz.put("teacherName", teacherName);
            quiz.put("timeLimit", timeLimit);
            quiz.put("totalMarks", 0); // Will be updated during upload
            quiz.put("status", "Draft");
            quiz.put("totalAttempts", 0);
            quiz.put("createdAt", FieldValue.serverTimestamp());

            db.collection("quizzes").document(quizId).set(quiz)
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(CreateQuizActivity.this, QuizModeSelectionActivity.class);
                        intent.putExtra("QUIZ_ID", quizId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}