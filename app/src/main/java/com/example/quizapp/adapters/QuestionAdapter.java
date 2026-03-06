package com.example.quizapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.models.Question;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> questionList;
    private OnQuestionActionListener listener;

    public interface OnQuestionActionListener {
        void onEdit(Question question);
        void onDelete(Question question);
    }

    public QuestionAdapter(List<Question> questionList, OnQuestionActionListener listener) {
        this.questionList = questionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questionList.get(position);
        holder.tvText.setText((position + 1) + ". " + question.getQuestionText());
        holder.tvOptions.setText("A) " + question.getOptionA() + "\nB) " + question.getOptionB() + 
                               "\nC) " + question.getOptionC() + "\nD) " + question.getOptionD());
        holder.tvCorrect.setText("Correct: " + question.getCorrectAnswer());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(question));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(question));
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvOptions, tvCorrect;
        Button btnEdit, btnDelete;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_question_text);
            tvOptions = itemView.findViewById(R.id.tv_options);
            tvCorrect = itemView.findViewById(R.id.tv_correct_answer);
            btnEdit = itemView.findViewById(R.id.btn_edit_question);
            btnDelete = itemView.findViewById(R.id.btn_delete_question);
        }
    }
}