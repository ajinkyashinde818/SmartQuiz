package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class QuizModeSelectionActivity extends AppCompatActivity {

    private CardView btnManualMode, btnFileMode;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_mode_selection);

        quizId = getIntent().getStringExtra("QUIZ_ID");

        btnManualMode = findViewById(R.id.btn_manual_mode);
        btnFileMode = findViewById(R.id.btn_file_mode);

        btnManualMode.setOnClickListener(v -> {
            Intent intent = new Intent(QuizModeSelectionActivity.this, AddQuestionActivity.class);
            intent.putExtra("QUIZ_ID", quizId);
            startActivity(intent);
        });

        btnFileMode.setOnClickListener(v -> {
            Intent intent = new Intent(QuizModeSelectionActivity.this, UploadQuizActivity.class);
            intent.putExtra("QUIZ_ID", quizId);
            startActivity(intent);
        });
    }
}