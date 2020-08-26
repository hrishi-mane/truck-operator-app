package com.example.tracktmtruckoperator.UI;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tracktmtruckoperator.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

import static com.example.tracktmtruckoperator.Constants.ERROR_DIALOG_REQUEST;
import static com.example.tracktmtruckoperator.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.tracktmtruckoperator.Constants.PERMISSION_REQUEST_ENABLE_GPS;

public class MainActivity extends AppCompatActivity {


    MaterialButton enable_location;

    TextInputEditText user_id;
    TextInputLayout layout_user_id;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;

    Location location;
    GeoPoint var_geoPoint;


    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location = new Location(LocationManager.GPS_PROVIDER);

        location.setLatitude(19.948636);
        location.setLongitude(73.790575);

        enable_location = findViewById(R.id.button_location);

        layout_user_id = findViewById(R.id.textInput_user_id);
        user_id =findViewById(R.id.editText_user_id);


        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        enable_location.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (Objects.requireNonNull(user_id.getText()).toString().isEmpty()) {
                    layout_user_id.setError(getString(R.string.error_message));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            layout_user_id.setError(null);
                        }
                    }, 2000);
                }
                else {
                    getLastKnownLocation();

                }

            }
        });
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
                addToFireStore(location);

            }
        });

    }

    private void addToFireStore(Location location) {

        var_geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        firebaseFirestore.collection("Session").
                document("session123").collection("Users").document(user_id.getText().toString()).
                update("geoPoint",var_geoPoint).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                intent.putExtra("user_doc_id", user_id.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (!(grantResults.length > 0)) {
                Toast.makeText(this, "This application will not work without Location Permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkServices() {
        if(isServiceOK()){
            return isGpsEnabled();
        }
        return false;
    }

    private boolean isGpsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE) ;

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly")
                .setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableGpsIntent, PERMISSION_REQUEST_ENABLE_GPS);
            }
        });
        builder.show();
    }

    private boolean isServiceOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available,ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;

    }
    @Override
    protected void onResume() {
        super.onResume();

        if (checkServices()) {
            getLocationPermission();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isLocationServiceRunning()){
            startActivity(new Intent(MainActivity.this, LocationActivity.class));
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.tracktmtruckoperator.SERVICES.LocationService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}