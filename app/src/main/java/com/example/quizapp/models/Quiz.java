package com.example.quizapp.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

public class Quiz implements Serializable {
    private String quizId;
    private String title;
    private String subject;
    private int totalMarks;
    private int timeLimit; // in minutes
    private String teacherId;
    private String status; // "Draft" or "Active"
    private int totalAttempts;
    @ServerTimestamp
    private Date createdAt;

    public Quiz() {
    }

    public Quiz(String quizId, String title, String subject, int totalMarks, int timeLimit, String teacherId, String status) {
        this.quizId = quizId;
        this.title = title;
        this.subject = subject;
        this.totalMarks = totalMarks;
        this.timeLimit = timeLimit;
        this.teacherId = teacherId;
        this.status = status;
        this.totalAttempts = 0;
    }

    public String getQuizId() { return quizId; }
    public void setQuizId(String quizId) { this.quizId = quizId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}