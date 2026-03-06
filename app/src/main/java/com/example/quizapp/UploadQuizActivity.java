package com.example.quizapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UploadQuizActivity extends AppCompatActivity {

    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_quiz);

        quizId = getIntent().getStringExtra("QUIZ_ID");

        TextView tvTitle = findViewById(R.id.tv_title);
        Button btnSelectFile = findViewById(R.id.btn_select_file);
        Button btnUpload = findViewById(R.id.btn_upload);

        btnSelectFile.setOnClickListener(v -> {
            Toast.makeText(this, "File selection coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnUpload.setOnClickListener(v -> {
            Toast.makeText(this, "Upload functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
}
