package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends AppCompatActivity implements ClassesAdapter.OnAttendanceButtonClickListener {
    private static final String TAG = "StudentActivity";
    private RecyclerView recyclerView;
    private TextView textViewWelcomeStudent;
    private Button buttonEditProfile;
    private FirebaseFirestore db;
    private List<ClassModelR> classList;
    private ClassesAdapter classesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        textViewWelcomeStudent = findViewById(R.id.textViewWelcomestd);
        recyclerView = findViewById(R.id.recyclerViewClasses);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classList = new ArrayList<>();
        classesAdapter = new ClassesAdapter(classList, this); // Pass 'this' as listener
        recyclerView.setAdapter(classesAdapter);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this::fetchClasses);

        db = FirebaseFirestore.getInstance();

        String displayName = getIntent().getStringExtra("displayName");
        textViewWelcomeStudent.setText("Hello, " + displayName);

        buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(StudentActivity.this, EditPasswordActivity.class);
            intent.putExtra("displayName", displayName);
            startActivity(intent);
            finish();
        });

        fetchClasses();
    }

    private void fetchClasses() {
        swipeRefreshLayout.setRefreshing(true);
        db.collection("classes")
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);

                    if (task.isSuccessful()) {
                        classList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            ClassModelR classModelR = document.toObject(ClassModelR.class);
                            classModelR.setId(document.getId());
                            checkAttendance(classModelR);
                            classList.add(classModelR);
                        }
                        classesAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error fetching classes", task.getException());
                    }
                });
    }

    private void checkAttendance(ClassModelR classModelR) {
        String currentUser = getIntent().getStringExtra("displayName");
        db.collection("students")
                .document(classModelR.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            StudentAttendanceModel attendanceModel = document.toObject(StudentAttendanceModel.class);
                            if (attendanceModel != null && attendanceModel.getPresent() != null &&
                                    attendanceModel.getPresent().contains(currentUser)) {
                                classModelR.setAttendanceDone(true);
                            }
                        }
                        classesAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error checking attendance", task.getException());
                    }
                });
    }

    @Override
    public void onAttendanceButtonClick(int position) {
        ClassModelR selectedClass = classList.get(position);
        markAttendance(selectedClass);
    }

    private void markAttendance(ClassModelR selectedClass) {
        String currentUser = getIntent().getStringExtra("displayName");
        String classId = selectedClass.getId();

        db.collection("students")
                .document(classId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                StudentAttendanceModel attendanceModel = document.toObject(StudentAttendanceModel.class);
                                if (attendanceModel != null && attendanceModel.getPresent() != null &&
                                        attendanceModel.getPresent().contains(currentUser)) {
                                    // If student is already present, update button text and UI
                                    selectedClass.setAttendanceDone(true);
                                    classesAdapter.updateAttendanceStatus(classId, true);
                                    Log.d(TAG, "Attendance already marked for current user in this class.");
                                } else {
                                    // Student is not marked present, add them to attendance list
                                    List<String> presentList = attendanceModel != null ? attendanceModel.getPresent() : new ArrayList<>();
                                    if (!presentList.contains(currentUser)) {
                                        presentList.add(currentUser);

                                        StudentAttendanceModel newAttendanceModel = new StudentAttendanceModel(
                                                selectedClass.getTeacherName(),
                                                selectedClass.getSubjectName(),
                                                presentList
                                        );

                                        db.collection("students")
                                                .document(classId)
                                                .set(newAttendanceModel)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            selectedClass.setAttendanceDone(true);
                                                            // Update button text and UI
                                                            classesAdapter.updateAttendanceStatus(classId, true);
                                                        } else {
                                                            Log.e(TAG, "Error marking attendance", task.getException());
                                                        }
                                                    }
                                                });
                                    }
                                }
                            } else {
                                // Document does not exist, create new attendance entry
                                List<String> presentList = new ArrayList<>();
                                presentList.add(currentUser);

                                StudentAttendanceModel newAttendanceModel = new StudentAttendanceModel(
                                        selectedClass.getTeacherName(),
                                        selectedClass.getSubjectName(),
                                        presentList
                                );

                                db.collection("students")
                                        .document(classId)
                                        .set(newAttendanceModel)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    selectedClass.setAttendanceDone(true);
                                                    // Update button text and UI
                                                    classesAdapter.updateAttendanceStatus(classId, true);
                                                } else {
                                                    Log.e(TAG, "Error marking attendance", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e(TAG, "Error checking attendance document", task.getException());
                        }
                    }
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
}
