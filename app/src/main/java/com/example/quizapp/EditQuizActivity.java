package com.example.quizapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.models.Quiz;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditQuizActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etSubject, etTimeLimit, etTotalMarks;
    private Button btnSave;
    private FirebaseFirestore db;
    private Quiz quiz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_quiz);

        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.et_quiz_title);
        etSubject = findViewById(R.id.et_subject);
        etTimeLimit = findViewById(R.id.et_time_limit);
        etTotalMarks = findViewById(R.id.et_total_marks);
        btnSave = findViewById(R.id.btn_save);

        quiz = (Quiz) getIntent().getSerializableExtra("QUIZ");

        if (quiz != null) {
            displayQuizData();
        }

        btnSave.setOnClickListener(v -> updateQuiz());
    }

    private void displayQuizData() {
        etTitle.setText(quiz.getTitle());
        etSubject.setText(quiz.getSubject());
        etTimeLimit.setText(String.valueOf(quiz.getTimeLimit()));
        etTotalMarks.setText(String.valueOf(quiz.getTotalMarks()));
    }

    private void updateQuiz() {
        String title = etTitle.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String timeStr = etTimeLimit.getText().toString().trim();
        String marksStr = etTotalMarks.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(subject) || TextUtils.isEmpty(timeStr) || TextUtils.isEmpty(marksStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        quiz.setTitle(title);
        quiz.setSubject(subject);
        quiz.setTimeLimit(Integer.parseInt(timeStr));
        quiz.setTotalMarks(Integer.parseInt(marksStr));

        db.collection("quizzes").document(quiz.getQuizId()).set(quiz)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Quiz updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}