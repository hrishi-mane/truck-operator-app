package com.example.tracktmtruckoperator.SERVICES;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import static com.example.tracktmtruckoperator.Constants.FASTEST_INTERVAL;
import static com.example.tracktmtruckoperator.Constants.UPDATE_INTERVAL;

public class LocationService extends Service {

    FusedLocationProviderClient locationObject;
    String documentId;

    LocationCallback startLocationCallback;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationObject = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 26) {

            String CHANNEL_ID = "my_channel_01";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "my_channel", importance);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Location Service").setContentText("Location Service is Running in background." +
                            "Clear the app to stop.").build();

            startForeground(1, notification);

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocation();
        documentId = intent.getStringExtra("user_doc_id");
        return START_NOT_STICKY;
    }

    private void startLocation() {
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();

        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location curr_location = locationResult.getLastLocation();

                if(curr_location!=null){
                    updateFireStore(curr_location);

                }
            }
        };

        locationObject.requestLocationUpdates(mLocationRequestHighAccuracy,startLocationCallback,Looper.myLooper());
    }

    private void updateFireStore(Location location) {
        GeoPoint curr_geo_point = new GeoPoint(location.getLatitude(), location.getLongitude());

        DocumentReference curr_user_doc = FirebaseFirestore.getInstance().
                collection("Session")
                .document("session123").collection("Users").document(documentId);

        curr_user_doc.update("geoPoint", curr_geo_point).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("DOCUMENT UPDATE STATUS", "Document updated successfully" );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DOCUMENT UPDATE STATUS", "Document update failed" );
            }
        });

    }

    @Override
    public void onDestroy() {

        locationObject.removeLocationUpdates(startLocationCallback);
        super.onDestroy();

    }
}
