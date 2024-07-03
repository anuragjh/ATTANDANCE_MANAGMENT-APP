//package com.example.final_project;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.CollectionReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.Query;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//import com.google.firebase.firestore.QuerySnapshot;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class StudentActivity extends AppCompatActivity implements ClassesAdapter.OnAttendanceButtonClickListener {
//
//    private static final String TAG = "StudentActivity";
//    private RecyclerView recyclerView;
//    private TextView textViewWelcomeStudent;
//    private Button buttonEditProfile;
//    private FirebaseFirestore db;
//    private List<ClassModelR> classList;
//    private ClassesAdapter classesAdapter;
//    private SwipeRefreshLayout swipeRefreshLayout;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_student);
//
//        textViewWelcomeStudent = findViewById(R.id.textViewWelcomestd);
//        recyclerView = findViewById(R.id.recyclerViewClasses);
//        buttonEditProfile = findViewById(R.id.buttonEditProfile);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        classList = new ArrayList<>();
//        classesAdapter = new ClassesAdapter(classList, this); // Pass 'this' as listener
//        recyclerView.setAdapter(classesAdapter);
//
//        swipeRefreshLayout = findViewById(R.id.swiperefresh);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                fetchClasses();
//            }
//        });
//
//        db = FirebaseFirestore.getInstance();
//
//        String displayName = getIntent().getStringExtra("displayName");
//        textViewWelcomeStudent.setText("Hello, " + displayName);
//
//        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(StudentActivity.this, EditPasswordActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        fetchClasses();
//    }
//
//    private void fetchClasses() {
//        swipeRefreshLayout.setRefreshing(true);
//        CollectionReference classesRef = db.collection("classes");
//        Query query = classesRef.orderBy("subjectName", Query.Direction.ASCENDING);
//
//        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                swipeRefreshLayout.setRefreshing(false);
//
//                if (task.isSuccessful()) {
//                    classList.clear();
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        ClassModelR classModelR = document.toObject(ClassModelR.class);
//                        classModelR.setId(document.getId()); // Set the document ID as class ID
//                        checkAttendance(classModelR); // Check if attendance is already marked
//                        classList.add(classModelR);
//                    }
//                    classesAdapter.notifyDataSetChanged();
//                } else {
//                    Log.e(TAG, "Error fetching classes", task.getException());
//                }
//            }
//        });
//    }
//
//    private void checkAttendance(ClassModelR classModelR) {
//        String currentUser = getIntent().getStringExtra("displayName");
//        db.collection("attendance_user")
//                .document(classModelR.getId())
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document.exists()) {
//                                String teacherName = document.getString("teacherName");
//                                String subjectName = document.getString("subjectName");
//                                String status = document.getString("status");
//
//                                if (currentUser.equals(teacherName) && subjectName.equals(classModelR.getSubjectName()) && status.equals("Present")) {
//                                    classModelR.setAttendanceDone(true);
//                                }
//                            }
//                        } else {
//                            Log.e(TAG, "Error checking attendance", task.getException());
//                        }
//                    }
//                });
//    }
//
//    @Override
//    public void onAttendanceButtonClick(int position) {
//        ClassModelR selectedClass = classList.get(position);
//        if (!selectedClass.isAttendanceDone()) {
//            markAttendance(selectedClass);
//        }
//    }
//
//    private void markAttendance(ClassModelR selectedClass) {
//        String currentUser = getIntent().getStringExtra("displayName");
//        String classId = selectedClass.getId();
//
//        db.collection("attendance_user")
//                .document(classId)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document.exists()) {
//                                if (document.contains(currentUser)) {
//                                    Log.d(TAG, "Attendance already marked for current user in this class.");
//                                } else {
//                                    db.collection("attendance_user")
//                                            .document(classId)
//                                            .update(currentUser, true)
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    if (task.isSuccessful()) {
//                                                        selectedClass.setAttendanceDone(true);
//                                                        classesAdapter.notifyDataSetChanged();
//                                                    } else {
//                                                        Log.e(TAG, "Error marking attendance", task.getException());
//                                                    }
//                                                }
//                                            });
//                                }
//                            } else {
//                                Map<String, Object> attendanceData = new HashMap<>();
//                                attendanceData.put(currentUser, true);
//                                attendanceData.put("class_id", classId);
//
//                                db.collection("attendance_user")
//                                        .document(classId)
//                                        .set(attendanceData)
//                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                if (task.isSuccessful()) {
//                                                    selectedClass.setAttendanceDone(true);
//                                                    classesAdapter.notifyDataSetChanged();
//                                                } else {
//                                                    Log.e(TAG, "Error marking attendance", task.getException());
//                                                }
//                                            }
//                                        });
//                            }
//                        } else {
//                            Log.e(TAG, "Error checking attendance document", task.getException());
//                        }
//                    }
//                });
//    }
//
//    public void logout(View view) {
//        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        editor.apply();
//
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
//    }
//}



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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentActivity extends AppCompatActivity implements ClassesAdapter.OnAttendanceButtonClickListener {

    private static final String TAG = "StudentActivity";
    private RecyclerView recyclerView;
    private TextView textViewWelcomeStudent;
    private Button buttonEditProfile;
    private FirebaseFirestore db;
    private List<ClassModelR> classList;
    private ClassesAdapter classesAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchClasses();
            }
        });

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        String displayName = getIntent().getStringExtra("displayName");
        textViewWelcomeStudent.setText("Hello, " + displayName);

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentActivity.this, EditPasswordActivity.class);
                intent.putExtra("userId", currentUser.getUid());
                intent.putExtra("displayName",displayName);
                startActivity(intent);
            }
        });

        if (currentUser != null) {
            fetchClasses();
        } else {
            Log.e(TAG, "User not authenticated");
        }
    }

    private void fetchClasses() {
        swipeRefreshLayout.setRefreshing(true);
        CollectionReference classesRef = db.collection("classes");
        Query query = classesRef.orderBy("subjectName", Query.Direction.ASCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                swipeRefreshLayout.setRefreshing(false);

                if (task.isSuccessful()) {
                    classList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        ClassModelR classModelR = document.toObject(ClassModelR.class);
                        classModelR.setId(document.getId()); // Set the document ID as class ID
                        checkAttendance(classModelR); // Check if attendance is already marked
                        classList.add(classModelR);
                    }
                    classesAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error fetching classes", task.getException());
                }
            }
        });
    }

    private void checkAttendance(ClassModelR classModelR) {
        String currentUserName = currentUser.getDisplayName();
        db.collection("attendance_user")
                .document(classModelR.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String teacherName = document.getString("teacherName");
                                String subjectName = document.getString("subjectName");
                                String status = document.getString("status");

                                if (currentUserName.equals(teacherName) && subjectName.equals(classModelR.getSubjectName()) && status.equals("Present")) {
                                    classModelR.setAttendanceDone(true);
                                }
                            }
                        } else {
                            Log.e(TAG, "Error checking attendance", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onAttendanceButtonClick(int position) {
        ClassModelR selectedClass = classList.get(position);
        if (!selectedClass.isAttendanceDone()) {
            markAttendance(selectedClass);
        }
    }

    private void markAttendance(ClassModelR selectedClass) {
        String currentUserName = currentUser.getDisplayName();
        String classId = selectedClass.getId();

        db.collection("attendance_user")
                .document(classId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.contains(currentUserName)) {
                                    Log.d(TAG, "Attendance already marked for current user in this class.");
                                } else {
                                    db.collection("attendance_user")
                                            .document(classId)
                                            .update(currentUserName, true)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        selectedClass.setAttendanceDone(true);
                                                        classesAdapter.notifyDataSetChanged();
                                                    } else {
                                                        Log.e(TAG, "Error marking attendance", task.getException());
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Map<String, Object> attendanceData = new HashMap<>();
                                attendanceData.put(currentUserName, true);
                                attendanceData.put("class_id", classId);

                                db.collection("attendance_user")
                                        .document(classId)
                                        .set(attendanceData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    selectedClass.setAttendanceDone(true);
                                                    classesAdapter.notifyDataSetChanged();
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

