package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private static final String TAG = "OtpActivity";

    private EditText otpEditText;
    private Button verifyOtpButton;
    private String verificationId;
    private String phoneNumber;
    private FirebaseFirestore db;
    private String userName;
    private String classId;
    private String teacherName;
    private String subjectName;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    signInWithPhoneAuthCredential(phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(OtpActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    verificationId = s;
                    Toast.makeText(OtpActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp);

        otpEditText = findViewById(R.id.otpEditText);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);

        db = FirebaseFirestore.getInstance();

        userName = getIntent().getStringExtra("displayName");
        classId = getIntent().getStringExtra("classId");
        teacherName = getIntent().getStringExtra("teacherName");
        subjectName = getIntent().getStringExtra("subjectName");

        fetchPhoneNumberAndSendOtp();

        verifyOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otpCode = otpEditText.getText().toString().trim();
                if (otpCode.isEmpty()) {
                    Toast.makeText(OtpActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    verifyVerificationCode(otpCode);
                }
            }
        });
    }

    private void fetchPhoneNumberAndSendOtp() {
        db.collection("users").whereEqualTo("name", userName).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                phoneNumber = document.getString("phoneNumber");
                                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                    sendVerificationCode(phoneNumber);
                                } else {
                                    Toast.makeText(OtpActivity.this, "Phone number not found in database", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(OtpActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(OtpActivity.this, "Error fetching phone number: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                30,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    private void verifyVerificationCode(String otp) {
        if (verificationId == null || verificationId.isEmpty()) {
            Toast.makeText(this, "Verification ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(OtpActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
                            markAttendance();
                        } else {
                            Toast.makeText(OtpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void markAttendance() {
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
                                List<String> presentList = attendanceModel != null ? attendanceModel.getPresent() : new ArrayList<>();
                                if (!presentList.contains(userName)) {
                                    presentList.add(userName);
                                    StudentAttendanceModel newAttendanceModel = new StudentAttendanceModel(
                                            teacherName,
                                            subjectName,
                                            presentList
                                    );
                                    db.collection("students")
                                            .document(classId)
                                            .set(newAttendanceModel)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(OtpActivity.this, "Attendance marked successfully.", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    } else {
                                                        Toast.makeText(OtpActivity.this, "Error marking attendance: " + task.getException(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(OtpActivity.this, "Attendance already marked.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                List<String> presentList = new ArrayList<>();
                                presentList.add(userName);
                                StudentAttendanceModel newAttendanceModel = new StudentAttendanceModel(
                                        teacherName,
                                        subjectName,
                                        presentList
                                );
                                db.collection("students")
                                        .document(classId)
                                        .set(newAttendanceModel)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(OtpActivity.this, "Attendance marked successfully.", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(OtpActivity.this, "Error marking attendance: " + task.getException(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(OtpActivity.this, "Error fetching attendance document: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
