package com.example.quizapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.models.Question;
import com.example.quizapp.models.Quiz;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttemptQuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvTimer, tvQuestionNumber;
    private RadioGroup rgOptions;
    private RadioButton rbA, rbB, rbC, rbD;
    private Button btnNext;

    private FirebaseFirestore db;
    private String quizId, studentId, studentName;
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attempt_quiz);

        db = FirebaseFirestore.getInstance();
        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        quizId = getIntent().getStringExtra("QUIZ_ID");

        fetchStudentName();
        initViews();
        loadQuestions();
    }

    private void fetchStudentName() {
        db.collection("users").document(studentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    studentName = documentSnapshot.getString("name");
                });
    }

    private void initViews() {
        tvQuestion = findViewById(R.id.tv_question);
        tvTimer = findViewById(R.id.tv_timer);
        tvQuestionNumber = findViewById(R.id.tv_question_number);
        rgOptions = findViewById(R.id.rg_options);
        rbA = findViewById(R.id.rb_option_a);
        rbB = findViewById(R.id.rb_option_b);
        rbC = findViewById(R.id.rb_option_c);
        rbD = findViewById(R.id.rb_option_d);
        btnNext = findViewById(R.id.btn_next);

        btnNext.setOnClickListener(v -> checkAnswerAndNext());
    }

    private void loadQuestions() {
        questionList = new ArrayList<>();
        db.collection("quizzes").document(quizId).collection("questions").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        questionList.add(doc.toObject(Question.class));
                    }
                    if (!questionList.isEmpty()) {
                        startQuiz();
                    } else {
                        Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void startQuiz() {
        db.collection("quizzes").document(quizId).get().addOnSuccessListener(documentSnapshot -> {
            Quiz quiz = documentSnapshot.toObject(Quiz.class);
            if (quiz != null) {
                startTimer(quiz.getTimeLimit() * 60 * 1000);
                showQuestion();
            }
        });
    }

    private void startTimer(long durationMillis) {
        countDownTimer = new CountDownTimer(durationMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                submitQuiz();
            }
        }.start();
    }

    private void showQuestion() {
        rgOptions.clearCheck();
        Question q = questionList.get(currentQuestionIndex);
        tvQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + "/" + questionList.size());
        tvQuestion.setText(q.getQuestionText());
        rbA.setText(q.getOptionA());
        rbB.setText(q.getOptionB());
        rbC.setText(q.getOptionC());
        rbD.setText(q.getOptionD());

        if (currentQuestionIndex == questionList.size() - 1) {
            btnNext.setText("Submit Quiz");
        }
    }

    private void checkAnswerAndNext() {
        int selectedId = rgOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRb = findViewById(selectedId);
        String selectedAnswer = "";
        if (selectedRb == rbA) selectedAnswer = "A";
        else if (selectedRb == rbB) selectedAnswer = "B";
        else if (selectedRb == rbC) selectedAnswer = "C";
        else if (selectedRb == rbD) selectedAnswer = "D";

        if (selectedAnswer.equals(questionList.get(currentQuestionIndex).getCorrectAnswer())) {
            score++;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            showQuestion();
        } else {
            submitQuiz();
        }
    }

    private void submitQuiz() {
        if (countDownTimer != null) countDownTimer.cancel();

        int percentage = (score * 100) / questionList.size();

        Map<String, Object> attempt = new HashMap<>();
        attempt.put("quizId", quizId);
        attempt.put("studentId", studentId);
        attempt.put("studentName", studentName);
        attempt.put("score", percentage);
        attempt.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("attempts").add(attempt).addOnSuccessListener(documentReference -> {
            db.collection("quizzes").document(quizId).update("totalAttempts", com.google.firebase.firestore.FieldValue.increment(1));
            showResultDialog(percentage);
        });
    }

    private void showResultDialog(int percentage) {
        new AlertDialog.Builder(this)
                .setTitle("Quiz Completed")
                .setMessage("Your score: " + percentage + "%")
                .setCancelable(false)
                .setPositiveButton("View Leaderboard", (dialog, which) -> {
                    finish();
                    // Optional: Navigate to ResultActivity
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}