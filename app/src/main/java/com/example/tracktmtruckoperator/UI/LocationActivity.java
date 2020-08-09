package com.example.tracktmtruckoperator.UI;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tracktmtruckoperator.R;
import com.example.tracktmtruckoperator.SERVICES.LocationService;

public class LocationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        startLocationService();
    }

    private void startLocationService() {
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this , LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                startForegroundService(serviceIntent);
            }else{

                startService(serviceIntent);
            }
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