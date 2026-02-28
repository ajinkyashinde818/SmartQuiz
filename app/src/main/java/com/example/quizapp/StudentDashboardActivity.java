package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextView tvWelcome, tvNoQuizzes, tvNavName, tvNavEmail;
    private RecyclerView rvQuizzes;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<Map<String, Object>> quizList = new ArrayList<>();
    private QuizAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        tvWelcome = findViewById(R.id.tvWelcome);
        tvNoQuizzes = findViewById(R.id.tvNoQuizzes);
        rvQuizzes = findViewById(R.id.rvQuizzes);

        // Header view components
        tvNavName = navigationView.getHeaderView(0).findViewById(R.id.tvNavName);
        tvNavEmail = navigationView.getHeaderView(0).findViewById(R.id.tvNavEmail);

        rvQuizzes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizAdapter(quizList);
        rvQuizzes.setAdapter(adapter);

        fetchStudentData();
        loadAvailableQuizzes();
    }

    private void fetchStudentData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();
        
        tvNavEmail.setText(email);

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvWelcome.setText("Welcome, " + name);
                        tvNavName.setText(name);
                    }
                });
    }

    private void loadAvailableQuizzes() {
        db.collection("quizzes")
                .orderBy("quizTitle", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    quizList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        quizList.add(doc.getData());
                    }
                    if (quizList.isEmpty()) {
                        tvNoQuizzes.setVisibility(View.VISIBLE);
                    } else {
                        tvNoQuizzes.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder> {
        private List<Map<String, Object>> list;

        public QuizAdapter(List<Map<String, Object>> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> quiz = list.get(position);
            holder.title.setText((String) quiz.get("quizTitle"));
            holder.subject.setText((String) quiz.get("subject"));
            holder.difficulty.setText((String) quiz.get("difficulty"));
            holder.duration.setText(quiz.get("durationMinutes") + " Mins");
            
            Object totalQ = quiz.get("totalQuestions");
            holder.questions.setText(totalQ.toString() + " Questions");

            holder.btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(StudentDashboardActivity.this, QuizAttemptActivity.class);
                intent.putExtra("quizId", (String) quiz.get("quizId"));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title, subject, difficulty, duration, questions;
            Button btnStart;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tvQuizTitle);
                subject = itemView.findViewById(R.id.tvSubject);
                difficulty = itemView.findViewById(R.id.tvDifficulty);
                duration = itemView.findViewById(R.id.tvDuration);
                questions = itemView.findViewById(R.id.tvQuestions);
                btnStart = itemView.findViewById(R.id.btnStartQuiz);
            }
        }
    }
}