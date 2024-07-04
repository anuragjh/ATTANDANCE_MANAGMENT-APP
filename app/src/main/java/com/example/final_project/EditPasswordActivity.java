package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditPasswordActivity extends AppCompatActivity {

    private EditText currentPassword, newPassword, confirmNewPassword;
    private Button btnChangePassword, goBack;
    private FirebaseFirestore db;
    private String storedPassword;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_user);

        // Retrieve the userName from the Intent
        userName = getIntent().getStringExtra("displayName");
        if (userName == null) {
            Toast.makeText(this, "User name not provided", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if userName is not provided
            return;
        }

        currentPassword = findViewById(R.id.current_password);
        newPassword = findViewById(R.id.enter_password);
        confirmNewPassword = findViewById(R.id.reset_password);
        btnChangePassword = findViewById(R.id.reset_button);
        goBack = findViewById(R.id.go_back);

        db = FirebaseFirestore.getInstance();

        fetchStoredPassword();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditPasswordActivity.this,StudentActivity.class);
                intent.putExtra("displayName", userName);
                startActivity(intent);
                finish();
            }
        });
    }

    private void fetchStoredPassword() {
        Query query = db.collection("users").whereEqualTo("name", userName);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            storedPassword = document.getString("password");
                            break; // Assuming names are unique, we can break after finding the first match
                        }
                    } else {
                        Toast.makeText(EditPasswordActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditPasswordActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void changePassword() {
        String currentPass = currentPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirmPass = confirmNewPassword.getText().toString().trim();

        if (storedPassword.equals(currentPass)) {
            if (newPass.equals(confirmPass)) {
                // Update password in Firestore
                db.collection("users").whereEqualTo("name", userName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("users").document(document.getId())
                                        .update("password", newPass)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(EditPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(EditPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                break;
                            }
                        } else {
                            Toast.makeText(EditPasswordActivity.this, "Failed to fetch user for password update", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(EditPasswordActivity.this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(EditPasswordActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}
