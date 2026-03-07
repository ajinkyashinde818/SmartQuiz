package com.example.quizapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private String quizId, quizTitle;
    private TextView tvQuizTitle, tvNoResults;
    private RecyclerView rvOtherStudents;
    
    // Top 3 Views
    private View layoutRank1, layoutRank2, layoutRank3;
    private TextView tvRank1Name, tvRank1Score, tvRank2Name, tvRank2Score, tvRank3Name, tvRank3Score;

    private FirebaseFirestore db;
    private List<Attempt> allAttempts;
    private ResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        quizId = getIntent().getStringExtra("QUIZ_ID");
        quizTitle = getIntent().getStringExtra("QUIZ_TITLE");

        db = FirebaseFirestore.getInstance();
        allAttempts = new ArrayList<>();

        initViews();
        loadResults();
    }

    private void initViews() {
        tvQuizTitle = findViewById(R.id.tv_quiz_title);
        tvNoResults = findViewById(R.id.tv_no_results);
        rvOtherStudents = findViewById(R.id.rv_other_students);

        if (quizTitle != null) tvQuizTitle.setText(quizTitle);

        // Top 3 layouts
        layoutRank1 = findViewById(R.id.layout_rank1);
        layoutRank2 = findViewById(R.id.layout_rank2);
        layoutRank3 = findViewById(R.id.layout_rank3);

        tvRank1Name = findViewById(R.id.tv_rank1_name);
        tvRank1Score = findViewById(R.id.tv_rank1_score);
        tvRank2Name = findViewById(R.id.tv_rank2_name);
        tvRank2Score = findViewById(R.id.tv_rank2_score);
        tvRank3Name = findViewById(R.id.tv_rank3_name);
        tvRank3Score = findViewById(R.id.tv_rank3_score);

        rvOtherStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultAdapter(new ArrayList<>());
        rvOtherStudents.setAdapter(adapter);
    }

    private void loadResults() {
        if (quizId == null) {
            Toast.makeText(this, "No quiz selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("attempts")
                .whereEqualTo("quizId", quizId)
                .orderBy("score", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allAttempts.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Attempt attempt = doc.toObject(Attempt.class);
                        allAttempts.add(attempt);
                    }

                    if (allAttempts.isEmpty()) {
                        tvNoResults.setVisibility(View.VISIBLE);
                    } else {
                        tvNoResults.setVisibility(View.GONE);
                        displayTop3();
                        displayOthers();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayTop3() {
        if (allAttempts.size() >= 1) {
            layoutRank1.setVisibility(View.VISIBLE);
            tvRank1Name.setText(allAttempts.get(0).studentName);
            tvRank1Score.setText(allAttempts.get(0).score + "%");
        }
        if (allAttempts.size() >= 2) {
            layoutRank2.setVisibility(View.VISIBLE);
            tvRank2Name.setText(allAttempts.get(1).studentName);
            tvRank2Score.setText(allAttempts.get(1).score + "%");
        }
        if (allAttempts.size() >= 3) {
            layoutRank3.setVisibility(View.VISIBLE);
            tvRank3Name.setText(allAttempts.get(2).studentName);
            tvRank3Score.setText(allAttempts.get(2).score + "%");
        }
    }

    private void displayOthers() {
        List<Attempt> others = new ArrayList<>();
        if (allAttempts.size() > 3) {
            others.addAll(allAttempts.subList(3, allAttempts.size()));
        }
        adapter.updateList(others);
    }

    // Attempt Model Class
    public static class Attempt {
        public String studentName, quizId;
        public int score;
        public Attempt() {} // Required for Firestore
    }

    // Adapter for Other Students
    private static class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private List<Attempt> attempts;

        public ResultAdapter(List<Attempt> attempts) {
            this.attempts = attempts;
        }

        public void updateList(List<Attempt> newList) {
            this.attempts = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Attempt attempt = attempts.get(position);
            holder.tvName.setText(attempt.studentName);
            holder.tvScore.setText(attempt.score + "%");
            holder.tvDate.setText("Participant #" + (position + 4));
        }

        @Override
        public int getItemCount() {
            return attempts.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvScore, tvDate;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_quiz_name);
                tvScore = itemView.findViewById(R.id.tv_score);
                tvDate = itemView.findViewById(R.id.tv_quiz_date);
            }
        }
    }
}
