package com.example.final_project;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditPasswordActivity extends AppCompatActivity {

    private EditText currentPassword, newPassword, confirmNewPassword;
    private Button btnChangePassword, goBack;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String storedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_user);

        currentPassword = findViewById(R.id.current_password);
        newPassword = findViewById(R.id.enter_password);
        confirmNewPassword = findViewById(R.id.reset_password);
        btnChangePassword = findViewById(R.id.reset_Button);
        goBack = findViewById(R.id.goBack);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            fetchStoredPassword();
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleChangePassword();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchStoredPassword() {
        String userId = currentUser.getUid();
        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        storedPassword = document.getString("password");
                        Log.d("EditPasswordActivity", "Stored Password: " + storedPassword);
                    } else {
                        Toast.makeText(EditPasswordActivity.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditPasswordActivity.this, "Fetch failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleChangePassword() {
        String currentPwd = currentPassword.getText().toString().trim();
        String newPwd = newPassword.getText().toString().trim();
        String confirmPwd = confirmNewPassword.getText().toString().trim();

        if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!currentPwd.equals(storedPassword)) {
            Toast.makeText(this, "Current password incorrect", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update password in Firebase Authentication
        currentUser.updatePassword(newPwd)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password updated successfully
                            Toast.makeText(EditPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditPasswordActivity.this, "Password update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
