package com.example.quizapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditStudentProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone;
    private Spinner spinnerYear, spinnerDivision;
    private Button btnSave;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_profile);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerDivision = findViewById(R.id.spinnerDivision);
        btnSave = findViewById(R.id.btnSave);

        setupSpinners();
        loadUserData();

        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void setupSpinners() {
        String[] years = {"1st Year", "2nd Year", "3rd Year"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        String[] divisions = {"A", "B"};
        ArrayAdapter<String> divAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, divisions);
        divAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDivision.setAdapter(divAdapter);
    }

    private void loadUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etPhone.setText(documentSnapshot.getString("phone"));
                        
                        String year = documentSnapshot.getString("year");
                        String div = documentSnapshot.getString("division");

                        if (year != null) {
                            for (int i = 0; i < spinnerYear.getCount(); i++) {
                                if (spinnerYear.getItemAtPosition(i).toString().equals(year)) {
                                    spinnerYear.setSelection(i);
                                    break;
                                }
                            }
                        }

                        if (div != null) {
                            for (int i = 0; i < spinnerDivision.getCount(); i++) {
                                if (spinnerDivision.getItemAtPosition(i).toString().equals(div)) {
                                    spinnerDivision.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String year = spinnerYear.getSelectedItem().toString();
        String div = spinnerDivision.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("year", year);
        updates.put("division", div);

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}