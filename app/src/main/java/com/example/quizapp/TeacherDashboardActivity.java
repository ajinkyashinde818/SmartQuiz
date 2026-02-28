package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherDashboardActivity extends AppCompatActivity {

    private RecyclerView rvTeacherQuizzes;
    private MaterialButton btnCreateNewQuiz;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Map<String, Object>> quizList = new ArrayList<>();
    private QuizAdapter adapter;

    private View statTotal, statActive, statDraft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnCreateNewQuiz = findViewById(R.id.btnCreateNewQuiz);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        rvTeacherQuizzes = findViewById(R.id.rvTeacherQuizzes);
        
        statTotal = findViewById(R.id.statTotalQuizzes);
        statActive = findViewById(R.id.statActiveQuizzes);
        statDraft = findViewById(R.id.statDraftQuizzes);

        setupStatCards();

        rvTeacherQuizzes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizAdapter(quizList);
        rvTeacherQuizzes.setAdapter(adapter);

        if (btnCreateNewQuiz != null) {
            btnCreateNewQuiz.setOnClickListener(v -> {
                startActivity(new Intent(TeacherDashboardActivity.this, CreateQuizActivity.class));
            });
        }

        setupBottomNavigation();
        loadTeacherQuizzes();
    }

    private void setupStatCards() {
        ((TextView) statTotal.findViewById(R.id.tvStatLabel)).setText("Total Quizzes");
        ((ImageView) statTotal.findViewById(R.id.ivStatIcon)).setImageResource(android.R.drawable.ic_menu_agenda);
        
        ((TextView) statActive.findViewById(R.id.tvStatLabel)).setText("Active");
        ((ImageView) statActive.findViewById(R.id.ivStatIcon)).setImageResource(android.R.drawable.ic_menu_send);
        
        ((TextView) statDraft.findViewById(R.id.tvStatLabel)).setText("Drafts");
        ((ImageView) statDraft.findViewById(R.id.ivStatIcon)).setImageResource(android.R.drawable.ic_menu_edit);
    }

    private void loadTeacherQuizzes() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("quizzes")
                .whereEqualTo("teacherUid", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizList.clear();
                    int activeCount = 0;
                    int draftCount = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        quizList.add(data);
                        String status = (String) data.get("status");
                        if ("PUBLISHED".equals(status)) {
                            activeCount++;
                        } else if ("DRAFT".equals(status)) {
                            draftCount++;
                        }
                    }
                    adapter.notifyDataSetChanged();
                    updateStats(quizList.size(), activeCount, draftCount);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStats(int total, int active, int draft) {
        ((TextView) statTotal.findViewById(R.id.tvStatValue)).setText(String.valueOf(total));
        ((TextView) statActive.findViewById(R.id.tvStatValue)).setText(String.valueOf(active));
        ((TextView) statDraft.findViewById(R.id.tvStatValue)).setText(String.valueOf(draft));
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) return;
        
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                return true;
            } else if (id == R.id.nav_profile) {
                logout();
                return true;
            }
            return false;
        });
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder> {
        private List<Map<String, Object>> list;

        public QuizAdapter(List<Map<String, Object>> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_quiz_new, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> quiz = list.get(position);
            holder.title.setText((String) quiz.get("quizTitle"));
            holder.subject.setText((String) quiz.get("subject"));
            holder.duration.setText(quiz.get("durationMinutes") + " Mins");
            holder.level.setText((String) quiz.get("difficulty"));
            holder.status.setText((String) quiz.get("status"));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, subject, duration, level, status;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvQuizTitle);
                subject = itemView.findViewById(R.id.tvSubject);
                duration = itemView.findViewById(R.id.tvDuration);
                level = itemView.findViewById(R.id.tvLevel);
                status = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}