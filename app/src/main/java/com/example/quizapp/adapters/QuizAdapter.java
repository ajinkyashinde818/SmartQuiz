package com.example.quizapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.models.Quiz;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<Quiz> quizList;
    private OnQuizActionListener listener;

    public interface OnQuizActionListener {
        void onEdit(Quiz quiz);
        void onDelete(Quiz quiz);
        void onViewQuestions(Quiz quiz);
    }

    public QuizAdapter(List<Quiz> quizList, OnQuizActionListener listener) {
        this.quizList = quizList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.tvTitle.setText(quiz.getTitle());
        // Updated to use getTotalMarks() which stores the question count updated during upload
        holder.tvInfo.setText("Questions: " + quiz.getTotalMarks() + " | Time: " + quiz.getTimeLimit() + " min");
        holder.tvStatus.setText("Status: " + quiz.getStatus());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(quiz));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(quiz));
        holder.btnViewQuestions.setOnClickListener(v -> listener.onViewQuestions(quiz));
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInfo, tvStatus;
        Button btnEdit, btnDelete, btnViewQuestions;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_quiz_title);
            tvInfo = itemView.findViewById(R.id.tv_quiz_info);
            tvStatus = itemView.findViewById(R.id.tv_quiz_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnViewQuestions = itemView.findViewById(R.id.btn_view_questions);
        }
    }
}