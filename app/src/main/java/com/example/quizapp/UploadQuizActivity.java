package com.example.quizapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.models.Quiz;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class UploadQuizActivity extends AppCompatActivity {

    private String quizId;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_quiz);

        quizId = getIntent().getStringExtra("QUIZ_ID");
        db = FirebaseFirestore.getInstance();

        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tv_status);
        Button btnSelectFile = findViewById(R.id.btn_select_file);
        Button btnUpload = findViewById(R.id.btn_upload);

        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        fileUri = result.getData().getData();
                        String fileName = fileUri.getLastPathSegment();
                        tvStatus.setText("File Selected: " + fileName);
                        btnUpload.setVisibility(View.VISIBLE);
                    }
                }
        );

        btnSelectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/*");
            String[] mimeTypes = {
                    "application/msword", // .doc
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(intent);
        });

        btnUpload.setOnClickListener(v -> {
            if (fileUri != null) {
                if (fileUri.toString().endsWith(".docx") || getContentResolver().getType(fileUri).contains("wordprocessingml")) {
                    parseAndUploadDocx(fileUri);
                } else {
                    Toast.makeText(this, "Only .docx format is currently supported for parsing.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void parseAndUploadDocx(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Processing file...");

        try (InputStream is = getContentResolver().openInputStream(uri);
             XWPFDocument document = new XWPFDocument(is)) {

            List<Map<String, Object>> questions = new ArrayList<>();
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            Map<String, Object> currentQuestion = null;
            
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText().trim();
                if (text.isEmpty()) continue;

                if (text.startsWith("Q:") || text.matches("^\\d+\\..*")) {
                    if (currentQuestion != null) questions.add(currentQuestion);
                    currentQuestion = new HashMap<>();
                    currentQuestion.put("questionId", UUID.randomUUID().toString());
                    currentQuestion.put("quizId", quizId);
                    currentQuestion.put("questionText", text.replaceAll("^Q:\\s*|^\\d+\\.\\s*", ""));
                } else if (text.startsWith("A)")) {
                    if (currentQuestion != null) currentQuestion.put("optionA", text.substring(2).trim());
                } else if (text.startsWith("B)")) {
                    if (currentQuestion != null) currentQuestion.put("optionB", text.substring(2).trim());
                } else if (text.startsWith("C)")) {
                    if (currentQuestion != null) currentQuestion.put("optionC", text.substring(2).trim());
                } else if (text.startsWith("D)")) {
                    if (currentQuestion != null) currentQuestion.put("optionD", text.substring(2).trim());
                } else if (text.startsWith("Ans:")) {
                    if (currentQuestion != null) currentQuestion.put("correctAnswer", text.substring(4).trim().toUpperCase());
                }
            }
            if (currentQuestion != null) questions.add(currentQuestion);

            uploadToFirestore(questions);

        } catch (Exception e) {
            Log.e("UploadQuiz", "Error parsing docx", e);
            tvStatus.setText("Error: " + e.getMessage());
            progressBar.setVisibility(View.GONE);
        }
    }

    private void uploadToFirestore(List<Map<String, Object>> questions) {
        if (questions.isEmpty()) {
            tvStatus.setText("No questions found in file.");
            progressBar.setVisibility(View.GONE);
            return;
        }

        WriteBatch batch = db.batch();
        for (Map<String, Object> q : questions) {
            String qId = (String) q.get("questionId");
            batch.set(db.collection("quizzes").document(quizId).collection("questions").document(qId), q);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            db.collection("quizzes").document(quizId).update("status", "Active", "totalMarks", questions.size())
                    .addOnSuccessListener(aVoid1 -> {
                        sendNotification();
                        Toast.makeText(this, "Uploaded and Published!", Toast.LENGTH_LONG).show();
                        finish();
                    });
        }).addOnFailureListener(e -> {
            tvStatus.setText("Upload Failed: " + e.getMessage());
            progressBar.setVisibility(View.GONE);
        });
    }

    private void sendNotification() {
        db.collection("quizzes").document(quizId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Quiz quiz = documentSnapshot.toObject(Quiz.class);
                    if (quiz != null) {
                        String topic = quiz.getYear().replace(" ", "") + "_" + quiz.getDivision();
                        String title = "New Quiz Available!";
                        String body = "Quiz: " + quiz.getTitle() + "\n" +
                                     "Subject: " + quiz.getSubject() + "\n" +
                                     "Teacher: " + quiz.getTeacherName() + "\n" +
                                     "Class: " + quiz.getYear() + " - Division " + quiz.getDivision() + "\n" +
                                     "Time Limit: " + quiz.getTimeLimit() + " minutes\n\n" +
                                     "Tap to attempt the quiz.";
                        
                        // Note: For real production, use Firebase Admin SDK or a backend.
                        // This is a placeholder for the notification logic.
                        Log.d("FCM", "Sending notification to topic: " + topic);
                        Log.d("FCM", "Message: " + body);
                    }
                });
    }
}
