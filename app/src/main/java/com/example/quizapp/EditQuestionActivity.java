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

public class EditQuestionActivity extends AppCompatActivity {

    private TextInputEditText etQuestionText, etOptionA, etOptionB, etOptionC, etOptionD;
    private RadioGroup rgCorrectAnswer;
    private Button btnUpdate;
    private FirebaseFirestore db;
    private Question question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_question);

        db = FirebaseFirestore.getInstance();
        question = (Question) getIntent().getSerializableExtra("QUESTION");

        etQuestionText = findViewById(R.id.et_question_text);
        etOptionA = findViewById(R.id.et_option_a);
        etOptionB = findViewById(R.id.et_option_b);
        etOptionC = findViewById(R.id.et_option_c);
        etOptionD = findViewById(R.id.et_option_d);
        rgCorrectAnswer = findViewById(R.id.rg_correct_answer);
        btnUpdate = findViewById(R.id.btn_update_question);

        if (question != null) {
            loadQuestionData();
        }

        btnUpdate.setOnClickListener(v -> updateQuestion());
    }

    private void loadQuestionData() {
        etQuestionText.setText(question.getQuestionText());
        etOptionA.setText(question.getOptionA());
        etOptionB.setText(question.getOptionB());
        etOptionC.setText(question.getOptionC());
        etOptionD.setText(question.getOptionD());

        String correct = question.getCorrectAnswer();
        if ("A".equals(correct)) rgCorrectAnswer.check(R.id.rb_a);
        else if ("B".equals(correct)) rgCorrectAnswer.check(R.id.rb_b);
        else if ("C".equals(correct)) rgCorrectAnswer.check(R.id.rb_c);
        else if ("D".equals(correct)) rgCorrectAnswer.check(R.id.rb_d);
    }

    private void updateQuestion() {
        String questionText = etQuestionText.getText().toString().trim();
        String optionA = etOptionA.getText().toString().trim();
        String optionB = etOptionB.getText().toString().trim();
        String optionC = etOptionC.getText().toString().trim();
        String optionD = etOptionD.getText().toString().trim();

        int selectedId = rgCorrectAnswer.getCheckedRadioButtonId();
        if (TextUtils.isEmpty(questionText) || TextUtils.isEmpty(optionA) || TextUtils.isEmpty(optionB) || selectedId == -1) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String correctAnswer = "";
        if (selectedId == R.id.rb_a) correctAnswer = "A";
        else if (selectedId == R.id.rb_b) correctAnswer = "B";
        else if (selectedId == R.id.rb_c) correctAnswer = "C";
        else if (selectedId == R.id.rb_d) correctAnswer = "D";

        question.setQuestionText(questionText);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectAnswer(correctAnswer);

        db.collection("quizzes").document(question.getQuizId())
                .collection("questions").document(question.getQuestionId())
                .set(question)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Question Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}