package com.example.final_project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewResult;
    private Spinner spinnerUserType;

    private FirebaseFirestore db;

    private static final String TAG = "MainActivity";
    private static final String SHARED_PREFS = "my_shared_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USERTYPE = "user_type";
    private static final String KEY_DISPLAYNAME = "display_name";
    private static final String USER_STUDENT = "Student";
    private static final String USER_TEACHER = "Teacher";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonSignIn);
        textViewResult = findViewById(R.id.textViewError);
        spinnerUserType = findViewById(R.id.role_spinner);

       db = FirebaseFirestore.getInstance();

        String[] userTypeOptions = {"Student", "Teacher"};
        ArrayAdapter<String> userTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userTypeOptions);
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUserType.setAdapter(userTypeAdapter);
        spinnerUserType.setSelection(userTypeAdapter.getPosition(USER_STUDENT));

        if (isLoggedIn()) {
            navigateToUserActivity(getSavedUserType(), getSavedDisplayName());
        }

        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            Log.d(TAG, "Username: " + username);
            Log.d(TAG, "Password: " + password);

            if (!username.isEmpty() && !password.isEmpty()) {
                checkUserCredentials(username, password);
            } else {
                textViewResult.setText("Please enter both username and password");
            }
        });
    }

    private void checkUserCredentials(String username, String password) {
        CollectionReference usersRef = db.collection("users");

        String selectedUserType = (String) spinnerUserType.getSelectedItem();
        if (selectedUserType.equals(USER_TEACHER)) {
            usersRef = db.collection("teachers");
        }

        Query query = usersRef.whereEqualTo("username", username).whereEqualTo("password", password);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    boolean userFound = false;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            String displayName = document.getString("name");
                            Log.d(TAG, "Display Name: " + displayName);
                            saveUserDetails(username, selectedUserType, displayName);
                            showWelcomeSnackbar(displayName);
                            navigateToUserActivity(selectedUserType, displayName);
                            userFound = true;
                            break;
                        }
                    }
                    if (!userFound) {
                        textViewResult.setText("Username or password is incorrect");
                    }
                } else {
                    Log.e(TAG, "Error checking user", task.getException());
                    textViewResult.setText("Error checking user");
                }
            }
        });
    }

    private void saveUserDetails(String username, String userType, String displayName) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USERTYPE, userType);
        editor.putString(KEY_DISPLAYNAME, displayName);
        editor.apply();
    }

    private boolean isLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.contains(KEY_USERNAME) && sharedPreferences.contains(KEY_USERTYPE);
    }

    private String getSavedUserType() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERTYPE, USER_STUDENT);
    }

    private String getSavedDisplayName() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_DISPLAYNAME, "");
    }

    private void showWelcomeSnackbar(String displayName) {
        Snackbar.make(findViewById(android.R.id.content), "Welcome, " + displayName + "!", Snackbar.LENGTH_SHORT).show();
    }

    private void navigateToUserActivity(String userType, String displayName) {
        Class<?> userActivityClass = userType.equals(USER_TEACHER) ? TeacherActivity.class : StudentActivity.class;
        Intent intent = new Intent(MainActivity.this, userActivityClass);
        intent.putExtra("displayName", displayName);
        startActivity(intent);
        finish();
    }
}