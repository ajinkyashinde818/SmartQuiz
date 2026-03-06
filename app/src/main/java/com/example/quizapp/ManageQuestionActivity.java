package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.adapters.QuestionAdapter;
import com.example.quizapp.models.Question;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageQuestionActivity extends AppCompatActivity implements QuestionAdapter.OnQuestionActionListener {

    private RecyclerView rvQuestions;
    private QuestionAdapter adapter;
    private List<Question> questionList;
    private FirebaseFirestore db;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_quiz); // Reusing layout

        db = FirebaseFirestore.getInstance();
        quizId = getIntent().getStringExtra("QUIZ_ID");

        rvQuestions = findViewById(R.id.rv_quizzes);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("Manage Questions");

        questionList = new ArrayList<>();
        adapter = new QuestionAdapter(questionList, this);

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(adapter);

        loadQuestions();
    }

    private void loadQuestions() {
        db.collection("quizzes").document(quizId).collection("questions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        questionList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Question question = document.toObject(Question.class);
                            questionList.add(question);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onEdit(Question question) {
        Intent intent = new Intent(this, EditQuestionActivity.class);
        intent.putExtra("QUESTION", question);
        startActivity(intent);
    }

    @Override
    public void onDelete(Question question) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Question")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("quizzes").document(quizId).collection("questions").document(question.getQuestionId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                loadQuestions();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuestions();
    }
}