package com.example.final_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView tvResult;

    private String userName;

    private double latMin;
    private double latMax;
    private double lonMin;
    private double lonMax;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_des);

        tvResult = findViewById(R.id.tv_result);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        userName = getIntent().getStringExtra("displayName");

        db = FirebaseFirestore.getInstance();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                runOnUiThread(() -> checkIfInsideRoom(location.getLatitude(), location.getLongitude()));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        fetchLocationBounds();
    }

    private void fetchLocationBounds() {
        db.collection("location").document("classRoomBounds").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                latMin = document.getDouble("latMin");
                                latMax = document.getDouble("latMax");
                                lonMin = document.getDouble("lonMin");
                                lonMax = document.getDouble("lonMax");
                            } else {
                                Toast.makeText(LocationActivity.this, "Location bounds not found in database", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LocationActivity.this, "Error fetching location bounds: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void getLocation(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            } catch (SecurityException e) {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation(null);
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkIfInsideRoom(double latitude, double longitude) {
        if (latitude >= latMin && latitude <= latMax && longitude >= lonMin && longitude <= lonMax) {
            tvResult.setText("Inside ClassRoom");
            Toast.makeText(LocationActivity.this, "Verification Successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LocationActivity.this, StudentActivity.class);
            intent.putExtra("displayName", userName);
            startActivity(intent);
            finish();
        } else {
            tvResult.setText("Outside ClassRoom");
            Toast.makeText(LocationActivity.this, "Verification Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
