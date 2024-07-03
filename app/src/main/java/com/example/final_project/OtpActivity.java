
package com.example.final_project;

import android.content.Context;
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
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyOtpButton,goBack;
    private String verificationId;
    private String phoneNumber;
    private FirebaseFirestore db;
    private String userName;

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


        fetchPhoneNumberAndSendOtp();

//        goBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(OtpActivity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });

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
                            Intent intent = new Intent(OtpActivity.this, StudentActivity.class);
                            intent.putExtra("displayName", userName);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(OtpActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
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
