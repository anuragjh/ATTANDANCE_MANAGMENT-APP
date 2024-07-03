




package com.example.final_project;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private static final String TAG = "TeacherActivity";

    private FirebaseFirestore db;
    private TextView textViewWelcomeTeacher;
    private Button buttonCreateClass;
    private Button buttonShowStudents;
    private String ongoingClassId;
    private LinearLayout layoutOngoingClasses;
    private TextView textViewClassName;
    private RecyclerView recyclerViewStudents;
    private StudentAdapter studentAdapter;
    private List<String> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);



        db = FirebaseFirestore.getInstance();

        textViewWelcomeTeacher = findViewById(R.id.textViewWelcomeTeacher);
        buttonCreateClass = findViewById(R.id.buttonCreateClass);
        buttonShowStudents = findViewById(R.id.buttonShowStudents);
        layoutOngoingClasses = findViewById(R.id.layoutOngoingClasses);
        textViewClassName = findViewById(R.id.textViewClassName);
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);

        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList);
        recyclerViewStudents.setAdapter(studentAdapter);

        // Set welcome message
        String displayName = getIntent().getStringExtra("displayName");
        textViewWelcomeTeacher.setText("Hello, " + displayName);

        // Check for ongoing class
        checkOngoingClass();

        buttonCreateClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ongoingClassId != null) {
                    endOngoingClass();
                } else {
                    showAddClassDialog();
                }
            }
        });

        buttonShowStudents.setOnClickListener(view -> {
            if (ongoingClassId != null) {
                showStudents();
            } else {
                Snackbar.make(findViewById(android.R.id.content), "No ongoing class to show students for.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void checkOngoingClass() {
        db.collection("classes")
                .whereEqualTo("ongoing", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            ongoingClassId = document.getId();
                            String className = document.getString("className");
                            textViewClassName.setText("Ongoing Class: " + className);
                            buttonCreateClass.setText("End Class");
                        } else {
                            textViewClassName.setText("No ongoing class");
                        }
                    }
                });
    }

    private void showAddClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_class, null);
        builder.setView(dialogView);

        final Spinner spinnerSubjectName = dialogView.findViewById(R.id.spinnerSubjectName);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonAction = dialogView.findViewById(R.id.buttonAdd);

        // Set up the spinner with sample subjects
        List<String> subjects = Arrays.asList("OPERATING SYSTEMS", "DATABASE MANAGEMENT SYSTEM", "COMPUTER NETWORK", "SOFTWARE ENGINEERING", "OOP USING JAVA");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubjectName.setAdapter(adapter);

        final AlertDialog dialog = builder.create();

        buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String subjectName = spinnerSubjectName.getSelectedItem().toString();

                if (!subjectName.isEmpty()) {
                    createClass(subjectName, subjectName);
                    dialog.dismiss();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void createClass(String className, String subjectName) {
        ClassModel newClass = new ClassModel(className, subjectName);
        db.collection("classes")
                .add(newClass)
                .addOnSuccessListener(documentReference -> {
                    ongoingClassId = documentReference.getId();
                    textViewClassName.setText("Ongoing Class: " + className);
                    buttonCreateClass.setText("End Class");
                    Snackbar.make(findViewById(android.R.id.content), "Class added successfully", Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to add class", Snackbar.LENGTH_LONG).show();
                });
    }

    public void endOngoingClass() {
        db.collection("classes").document(ongoingClassId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    textViewClassName.setText("No ongoing class");
                    buttonCreateClass.setText("Create Class");
                    Snackbar.make(findViewById(android.R.id.content), "Class ended successfully", Snackbar.LENGTH_LONG).show();
                    ongoingClassId = null;
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to end class", Snackbar.LENGTH_LONG).show();
                });
    }

    public void logout(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showStudents() {
        if (ongoingClassId == null) {
            Snackbar.make(findViewById(android.R.id.content), "No ongoing class to show students for.", Snackbar.LENGTH_LONG).show();
            return;
        }

        db.collection("attendance_user")
                .whereEqualTo("class_id", ongoingClassId)
                .get()
                .addOnCompleteListener(task -> {
                    Snackbar.make(findViewById(android.R.id.content), "Fetching student data...", Snackbar.LENGTH_SHORT).show();
                    if (task.isSuccessful()) {
                        List<String> students = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            for (String key : document.getData().keySet()) {
                                if (!key.equals("class_id")) { // Exclude the class_id field
                                    students.add(key); // Assuming keys are student names
                                }
                            }
                        }
                        if (!students.isEmpty()) {
                            // Update UI with student data
                            studentList.clear();
                            studentList.addAll(students);
                            studentAdapter.notifyDataSetChanged();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content), "No students present for the ongoing class.", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Error getting students: ", task.getException());
                        Snackbar.make(findViewById(android.R.id.content), "Error fetching students", Snackbar.LENGTH_LONG).show();
                    }
                });
    }



    public boolean isOngoingClass() {
        return ongoingClassId != null;
    }
}
