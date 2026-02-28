package com.example.quizapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestionBuilderActivity extends AppCompatActivity {

    private RecyclerView rvQuestions;
    private FloatingActionButton fabAdd;
    private TextView tvTotalQuestions, tvTotalMarks;
    private Button btnPublish, btnSaveDraft;

    private List<Map<String, Object>> questionsList = new ArrayList<>();
    private QuestionAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String quizTitle, description, subject, year, difficulty;
    private int duration, passing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_builder);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        getIntentData();
        initViews();

        fabAdd.setOnClickListener(v -> showAddQuestionDialog());
        btnPublish.setOnClickListener(v -> saveQuiz("PUBLISHED"));
        btnSaveDraft.setOnClickListener(v -> saveQuiz("DRAFT"));
    }

    private void getIntentData() {
        Intent intent = getIntent();
        quizTitle = intent.getStringExtra("quizTitle");
        description = intent.getStringExtra("description");
        subject = intent.getStringExtra("subject");
        year = intent.getStringExtra("year");
        difficulty = intent.getStringExtra("difficulty");
        duration = intent.getIntExtra("duration", 0);
        passing = intent.getIntExtra("passing", 0);
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvQuestions = findViewById(R.id.rvQuestions);
        fabAdd = findViewById(R.id.fabAddQuestion);
        tvTotalQuestions = findViewById(R.id.tvTotalQuestions);
        tvTotalMarks = findViewById(R.id.tvTotalMarks);
        btnPublish = findViewById(R.id.btnPublish);
        btnSaveDraft = findViewById(R.id.btnSaveDraft);

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionAdapter();
        rvQuestions.setAdapter(adapter);
    }

    private void showAddQuestionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_question_pro, null);
        builder.setView(view);

        EditText etQ = view.findViewById(R.id.etQuestion);
        EditText etA = view.findViewById(R.id.etOptA);
        EditText etB = view.findViewById(R.id.etOptB);
        EditText etC = view.findViewById(R.id.etOptC);
        EditText etD = view.findViewById(R.id.etOptD);
        RadioGroup rg = view.findViewById(R.id.rgCorrect);

        AlertDialog dialog = builder.create();
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String q = etQ.getText().toString().trim();
            String a = etA.getText().toString().trim();
            String b = etB.getText().toString().trim();
            String c = etC.getText().toString().trim();
            String d = etD.getText().toString().trim();
            int selectedId = rg.getCheckedRadioButtonId();

            if (TextUtils.isEmpty(q) || TextUtils.isEmpty(a) || TextUtils.isEmpty(b) || TextUtils.isEmpty(c) || TextUtils.isEmpty(d) || selectedId == -1) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> question = new HashMap<>();
            question.put("question", q);
            question.put("optionA", a);
            question.put("optionB", b);
            question.put("optionC", c);
            question.put("optionD", d);
            
            if (selectedId == R.id.rbA) question.put("correctAnswer", a);
            else if (selectedId == R.id.rbB) question.put("correctAnswer", b);
            else if (selectedId == R.id.rbC) question.put("correctAnswer", c);
            else question.put("correctAnswer", d);

            questionsList.add(question);
            adapter.notifyItemInserted(questionsList.size() - 1);
            updateStats();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateStats() {
        tvTotalQuestions.setText("Total Questions: " + questionsList.size());
        tvTotalMarks.setText("Total Marks: " + questionsList.size());
    }

    private void saveQuiz(String status) {
        if (questionsList.isEmpty()) {
            Toast.makeText(this, "Please add at least one question", Toast.LENGTH_SHORT).show();
            return;
        }

        String quizId = UUID.randomUUID().toString();
        Map<String, Object> quiz = new HashMap<>();
        quiz.put("quizId", quizId);
        quiz.put("teacherUid", mAuth.getCurrentUser().getUid());
        quiz.put("quizTitle", quizTitle);
        quiz.put("description", description);
        quiz.put("subject", subject);
        quiz.put("year", year);
        quiz.put("difficulty", difficulty);
        quiz.put("durationMinutes", duration);
        quiz.put("passingPercentage", passing);
        quiz.put("status", status);
        quiz.put("totalQuestions", questionsList.size());
        quiz.put("questions", questionsList);

        db.collection("quizzes").document(quizId).set(quiz)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Quiz " + status + " successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, TeacherDashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question_pro, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> q = questionsList.get(position);
            holder.tvNum.setText("Q" + (position + 1));
            holder.tvText.setText((String) q.get("question"));
            holder.btnDelete.setOnClickListener(v -> {
                questionsList.remove(position);
                notifyDataSetChanged();
                updateStats();
            });
        }

        @Override
        public int getItemCount() { return questionsList.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNum, tvText;
            ImageView btnDelete;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvNum = v.findViewById(R.id.tvQuestionNum);
                tvText = v.findViewById(R.id.tvQuestionText);
                btnDelete = v.findViewById(R.id.btnDeleteQuestion);
            }
        }
    }
}