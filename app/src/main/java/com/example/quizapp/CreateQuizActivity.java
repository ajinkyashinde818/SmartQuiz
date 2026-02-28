package com.example.quizapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateQuizActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etDuration, etPassing;
    private AutoCompleteTextView spinnerSubject, spinnerYear, spinnerDifficulty;
    private Button btnNext;
    private List<Map<String, Object>> questionsList = new ArrayList<>();

    private static final int PICK_FILE_REQUEST_CODE = 105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_quiz_step1);

        initViews();
        setupDropdowns();

        btnNext.setOnClickListener(v -> validateAndProceed());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etTitle = findViewById(R.id.etQuizTitle);
        etDescription = findViewById(R.id.etDescription);
        etDuration = findViewById(R.id.etDuration);
        etPassing = findViewById(R.id.etPassing);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        btnNext = findViewById(R.id.btnNextStep);
    }

    private void setupDropdowns() {
        String[] subjects = {"Computer Science", "Mathematics", "Physics", "English", "General Knowledge"};
        spinnerSubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjects));

        String[] years = {"1st Year", "2nd Year", "3rd Year", "4th Year"};
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, years));

        String[] levels = {"Easy", "Medium", "Hard"};
        spinnerDifficulty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levels));
    }

    private void validateAndProceed() {
        String title = etTitle.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String passingStr = etPassing.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(passingStr)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = Integer.parseInt(durationStr);
        int passing = Integer.parseInt(passingStr);

        if (duration <= 0) {
            etDuration.setError("Duration must be > 0");
            return;
        }

        if (passing > 100) {
            etPassing.setError("Passing % cannot exceed 100");
            return;
        }

        Intent intent = new Intent(this, QuestionBuilderActivity.class);
        intent.putExtra("quizTitle", title);
        intent.putExtra("description", etDescription.getText().toString().trim());
        intent.putExtra("subject", spinnerSubject.getText().toString());
        intent.putExtra("year", spinnerYear.getText().toString());
        intent.putExtra("difficulty", spinnerDifficulty.getText().toString());
        intent.putExtra("duration", duration);
        intent.putExtra("passing", passing);
        startActivity(intent);
    }
}