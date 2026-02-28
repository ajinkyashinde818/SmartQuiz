package com.example.quizapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class QuizAttemptActivity extends AppCompatActivity {

    private TextView tvTimer, tvQuestionNumber, tvQuestion;
    private RadioGroup rgOptions;
    private RadioButton rbOptionA, rbOptionB, rbOptionC, rbOptionD;
    private Button btnNext;
    private ProgressBar progressBar, loadingSpinner;

    private FirebaseFirestore db;
    private String quizId;
    private List<Map<String, Object>> questionsList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_attempt);

        db = FirebaseFirestore.getInstance();
        quizId = getIntent().getStringExtra("quizId");

        initViews();
        loadQuizData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quiz Attempt");
        }

        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        rgOptions = findViewById(R.id.rgOptions);
        rbOptionA = findViewById(R.id.rbOptionA);
        rbOptionB = findViewById(R.id.rbOptionB);
        rbOptionC = findViewById(R.id.rbOptionC);
        rbOptionD = findViewById(R.id.rbOptionD);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        loadingSpinner = findViewById(R.id.loadingSpinner);

        btnNext.setOnClickListener(v -> handleNextQuestion());
    }

    private void loadQuizData() {
        if (quizId == null) {
            Toast.makeText(this, "Quiz ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingSpinner.setVisibility(View.VISIBLE);
        db.collection("quizzes").document(quizId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    loadingSpinner.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        questionsList = (List<Map<String, Object>>) documentSnapshot.get("questions");
                        long durationMinutes = documentSnapshot.getLong("durationMinutes");
                        if (questionsList != null && !questionsList.isEmpty()) {
                            startTimer(durationMinutes * 60 * 1000);
                            displayQuestion();
                        } else {
                            Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading quiz", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void startTimer(long durationMillis) {
        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("Time: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizAttemptActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                finishQuiz();
            }
        }.start();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < questionsList.size()) {
            Map<String, Object> questionData = questionsList.get(currentQuestionIndex);
            tvQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + "/" + questionsList.size());
            tvQuestion.setText((String) questionData.get("question"));
            rbOptionA.setText((String) questionData.get("optionA"));
            rbOptionB.setText((String) questionData.get("optionB"));
            rbOptionC.setText((String) questionData.get("optionC"));
            rbOptionD.setText((String) questionData.get("optionD"));

            rgOptions.clearCheck();
            progressBar.setProgress((int) (((float) (currentQuestionIndex + 1) / questionsList.size()) * 100));

            if (currentQuestionIndex == questionsList.size() - 1) {
                btnNext.setText("Submit");
            } else {
                btnNext.setText("Next");
            }
        }
    }

    private void handleNextQuestion() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedButton = findViewById(selectedId);
        String selectedAnswer = selectedButton.getText().toString();
        String correctAnswer = (String) questionsList.get(currentQuestionIndex).get("correctAnswer");

        if (selectedAnswer.equals(correctAnswer)) {
            score++;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < questionsList.size()) {
            displayQuestion();
        } else {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Toast.makeText(this, "Quiz Finished! Your score: " + score + "/" + questionsList.size(), Toast.LENGTH_LONG).show();
        // Here you could save the result to Firestore
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}