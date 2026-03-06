package com.example.quizapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.models.Question;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddQuestionActivity extends AppCompatActivity {

    private TextInputEditText etQuestionText, etOptionA, etOptionB, etOptionC, etOptionD;
    private RadioGroup rgCorrectAnswer;
    private Button btnSave, btnFinish;
    private FirebaseFirestore db;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);

        db = FirebaseFirestore.getInstance();
        quizId = getIntent().getStringExtra("QUIZ_ID");

        etQuestionText = findViewById(R.id.et_question_text);
        etOptionA = findViewById(R.id.et_option_a);
        etOptionB = findViewById(R.id.et_option_b);
        etOptionC = findViewById(R.id.et_option_c);
        etOptionD = findViewById(R.id.et_option_d);
        rgCorrectAnswer = findViewById(R.id.rg_correct_answer);
        btnSave = findViewById(R.id.btn_save_question);
        btnFinish = findViewById(R.id.btn_finish);

        btnSave.setOnClickListener(v -> saveQuestion());
        btnFinish.setOnClickListener(v -> finishAndPublish());
    }

    private void saveQuestion() {
        String questionText = etQuestionText.getText().toString().trim();
        String optionA = etOptionA.getText().toString().trim();
        String optionB = etOptionB.getText().toString().trim();
        String optionC = etOptionC.getText().toString().trim();
        String optionD = etOptionD.getText().toString().trim();

        int selectedId = rgCorrectAnswer.getCheckedRadioButtonId();
        if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(optionA) || TextUtils.isEmpty(optionB) || selectedId == -1) {
            Toast.makeText(this, "Please fill question, options and select correct answer", Toast.LENGTH_SHORT).show();
            return;
        }

        String correctAnswer = "";
        if (selectedId == R.id.rb_a) correctAnswer = "A";
        else if (selectedId == R.id.rb_b) correctAnswer = "B";
        else if (selectedId == R.id.rb_c) correctAnswer = "C";
        else if (selectedId == R.id.rb_d) correctAnswer = "D";

        String questionId = UUID.randomUUID().toString();

        Question question = new Question(questionId, quizId, questionText, optionA, optionB, optionC, optionD, correctAnswer);

        db.collection("quizzes").document(quizId).collection("questions").document(questionId).set(question)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Question Saved!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etQuestionText.setText("");
        etOptionA.setText("");
        etOptionB.setText("");
        etOptionC.setText("");
        etOptionD.setText("");
        rgCorrectAnswer.clearCheck();
    }

    private void finishAndPublish() {
        db.collection("quizzes").document(quizId).update("status", "Active")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Quiz Published Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}