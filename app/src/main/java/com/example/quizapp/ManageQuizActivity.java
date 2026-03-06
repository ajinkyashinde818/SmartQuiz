package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.adapters.QuizAdapter;
import com.example.quizapp.models.Quiz;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class ManageQuizActivity extends AppCompatActivity implements QuizAdapter.OnQuizActionListener {

    private RecyclerView rvQuizzes;
    private TextView tvNoQuizzes;
    private QuizAdapter adapter;
    private List<Quiz> quizList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_quiz);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvQuizzes = findViewById(R.id.rv_quizzes);
        tvNoQuizzes = findViewById(R.id.tv_no_quizzes);

        quizList = new ArrayList<>();
        adapter = new QuizAdapter(quizList, this);

        rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
        rvQuizzes.setAdapter(adapter);

        loadQuizzes();
    }

    private void loadQuizzes() {
        if (mAuth.getCurrentUser() == null) return;
        
        String teacherId = mAuth.getCurrentUser().getUid();
        db.collection("quizzes")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        quizList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Quiz quiz = document.toObject(Quiz.class);
                            quizList.add(quiz);
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (quizList.isEmpty()) {
                            tvNoQuizzes.setVisibility(View.VISIBLE);
                        } else {
                            tvNoQuizzes.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "Error loading quizzes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEdit(Quiz quiz) {
        Intent intent = new Intent(this, EditQuizActivity.class);
        intent.putExtra("QUIZ", quiz);
        startActivity(intent);
    }

    @Override
    public void onDelete(Quiz quiz) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Quiz")
                .setMessage("Are you sure you want to delete this quiz and all its questions?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteQuizWithQuestions(quiz.getQuizId());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteQuizWithQuestions(String quizId) {
        // First delete all questions in the sub-collection
        db.collection("quizzes").document(quizId).collection("questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(doc.getReference());
                    }
                    
                    // Then delete the quiz document itself
                    batch.delete(db.collection("quizzes").document(quizId));
                    
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Quiz and questions deleted", Toast.LENGTH_SHORT).show();
                        loadQuizzes();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching questions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onViewQuestions(Quiz quiz) {
        Intent intent = new Intent(this, ManageQuestionActivity.class);
        intent.putExtra("QUIZ_ID", quiz.getQuizId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadQuizzes();
    }
}