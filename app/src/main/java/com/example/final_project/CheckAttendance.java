package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckAttendance extends AppCompatActivity {

    private static final String TAG = "CheckAttendance";
    private TableLayout tableLayoutAttendance;
    private FirebaseFirestore db;
    private String displayName;

    private Button  goBack;
    private Map<String, Integer> attendanceMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_attendance);

        tableLayoutAttendance = findViewById(R.id.tableLayoutAttendance);
        db = FirebaseFirestore.getInstance();
        goBack = findViewById(R.id.go_back);
        attendanceMap = new HashMap<>();

        displayName = getIntent().getStringExtra("displayName");

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckAttendance.this,StudentActivity.class);
                intent.putExtra("displayName", displayName);
                startActivity(intent);
                finish();
            }
        });

        fetchAttendance();
    }

    private void fetchAttendance() {
        db.collection("students")
                .whereArrayContains("present", displayName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for (DocumentSnapshot document : documents) {
                                StudentAttendanceModel attendanceModel = document.toObject(StudentAttendanceModel.class);
                                if (attendanceModel != null) {
                                    String subjectName = attendanceModel.getSubjectName();
                                    int currentCount = attendanceMap.containsKey(subjectName) ? attendanceMap.get(subjectName) : 0;
                                    attendanceMap.put(subjectName, currentCount + 1);
                                }
                            }
                            populateAttendanceTable();
                        } else {
                            Log.e(TAG, "Error fetching attendance", task.getException());
                        }
                    }
                });
    }

    private void populateAttendanceTable() {
        // Ensure that all subjects are shown even if attendance is zero
        String[] allSubjects = {"OPERATING SYSTEMS", "DATABASE MANAGEMENT SYSTEM", "COMPUTER NETWORK", "SOFTWARE ENGINEERING", "OOP USING JAVA"};

        for (String subject : allSubjects) {
            TableRow tableRow = new TableRow(this);
            tableRow.setBackgroundResource(R.drawable.table_row_border);

            TextView textViewSubject = new TextView(this);
            textViewSubject.setText(subject);
            textViewSubject.setGravity(Gravity.CENTER);
            textViewSubject.setPadding(16, 16, 16, 16);
            textViewSubject.setBackgroundResource(R.drawable.table_row_border);
            tableRow.addView(textViewSubject);

            TextView textViewTotalPresents = new TextView(this);
            textViewTotalPresents.setGravity(Gravity.CENTER);
            textViewTotalPresents.setPadding(16, 16, 16, 16);
            textViewTotalPresents.setBackgroundResource(R.drawable.table_row_border);
            tableRow.addView(textViewTotalPresents);

            // Set the attendance count if available, otherwise set it to 0
            if (attendanceMap.containsKey(subject)) {
                textViewTotalPresents.setText(String.valueOf(attendanceMap.get(subject)));
            } else {
                textViewTotalPresents.setText("0");
            }

            tableLayoutAttendance.addView(tableRow);
        }
    }
}
