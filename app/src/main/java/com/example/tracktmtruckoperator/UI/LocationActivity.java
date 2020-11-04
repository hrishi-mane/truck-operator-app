package com.example.tracktmtruckoperator.UI;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.tracktmtruckoperator.R;
import com.example.tracktmtruckoperator.SERVICES.LocationService;
import com.example.tracktmtruckoperator.UserInfoDb;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

public class LocationActivity extends AppCompatActivity {
    Button startLocationButton;
    TextView locationProgressTextView;
    ProgressBar locationProgressBar;

    Intent serviceIntent;

    Toolbar location_activity_toolbar;

    FusedLocationProviderClient locationObject;

    UserInfoDb userInfoDb;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //this is only needed if you have specific things
        //that you want to do when the user presses the back button.
        /* your specific things...*/
        moveTaskToBack(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_location_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_option) {
            userInfoDb.delete( getIntent().getStringExtra("user_doc_id") );
            startActivity(new Intent(LocationActivity.this, LoginActivity.class));

        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        userInfoDb = new UserInfoDb(this);

        startLocationButton = findViewById(R.id.start_location_button);
        locationProgressTextView = findViewById(R.id.location_progress_textview);
        locationProgressBar = findViewById(R.id.location_progressBar);
        location_activity_toolbar = findViewById(R.id.location_activity_toolbar);

        setSupportActionBar(location_activity_toolbar);
        getSupportActionBar().setTitle("");

        FirebaseFirestore.getInstance().collection("Users").
                document(getIntent().getStringExtra("user_doc_id")).get().
                addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult() != null) {
                            location_activity_toolbar.setVisibility(View.VISIBLE);
                            String current_user = task.getResult().getString("name");
                            getSupportActionBar().setTitle(current_user);
                        }
                    }
                });

        serviceIntent = new Intent(this, LocationService.class);

        locationObject = LocationServices.getFusedLocationProviderClient(this);

        startLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                FirebaseFirestore.getInstance().collection("Session").document("session123")
                        .collection("Users").document(getIntent().getStringExtra("user_doc_id")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists() && startLocationButton.getText().toString().equalsIgnoreCase("start location")) {
                            startLocation();
                        } else if (startLocationButton.getText().toString().equalsIgnoreCase("stop location")) {
                            stopLocation();
                        } else {
                            Snackbar.make(view, "You are not in the Session!!!", Snackbar.LENGTH_SHORT).
                                    setBackgroundTint(Color.LTGRAY).setTextColor(Color.RED).setDuration(2500).show();
                        }
                    }
                });
            }
        });

    }

    private void startLocation() {
        locationProgressTextView.setVisibility(View.VISIBLE);
        locationProgressBar.setVisibility(View.VISIBLE);

        startLocationButton.setText("Stop location");
        startLocationButton.setBackgroundColor(Color.RED);

        getLastKnownLocation();

        startLocationService();
    }

    private void stopLocation() {
        locationProgressTextView.setVisibility(View.INVISIBLE);
        locationProgressBar.setVisibility(View.INVISIBLE);

        startLocationButton.setText("Start location");
        startLocationButton.setBackgroundColor(Color.parseColor("#3700B3"));


        stopService(serviceIntent);
        FirebaseFirestore.getInstance().collection("Session").document("session123")
                .collection("Users").document("9WNOoW0xJlOVuBSzQKgR").update("geoPoint", null);
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            return;
        }
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.
                getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                addToFireStore(task.getResult());
            }
        });
    }

    private void addToFireStore(Location location) {
        GeoPoint var_geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        FirebaseFirestore.getInstance().collection("Session").
                document("session123").collection("Users").document("9WNOoW0xJlOVuBSzQKgR").
                update("geoPoint", var_geoPoint);
    }

    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            serviceIntent.putExtra("user_doc_id", getIntent().getStringExtra("user_doc_id"));

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.tracktmtruckoperator.SERVICES.LocationService".equals(service.service.getClassName())) {
                return true;
            }
        }

        return false;
    }

}