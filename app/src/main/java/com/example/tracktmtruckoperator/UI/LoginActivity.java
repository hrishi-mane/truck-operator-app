package com.example.tracktmtruckoperator.UI;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.tracktmtruckoperator.R;
import com.example.tracktmtruckoperator.UserInfoDb;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.example.tracktmtruckoperator.Constants.ERROR_DIALOG_REQUEST;
import static com.example.tracktmtruckoperator.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.tracktmtruckoperator.Constants.PERMISSION_REQUEST_ENABLE_GPS;

public class LoginActivity extends AppCompatActivity {

    MaterialButton enable_location;

    TextInputEditText user_id;
    TextInputLayout layout_user_id;

    ScrollView scrollView;

    final Handler handler = new Handler();

    UserInfoDb userInfoDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userInfoDb = new UserInfoDb(this);



        scrollView = findViewById(R.id.scrollview_layout);

        enable_location = findViewById(R.id.button_location);

        layout_user_id = findViewById(R.id.textInput_user_id);
        user_id = findViewById(R.id.editText_user_id);


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
                } else {
                    loginUser();
                }

            }
        });
    }

    private void loginUser() {
        FirebaseFirestore.getInstance().collection("Users").document(user_id.getText().toString())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    userInfoDb.insert(user_id.getText().toString());
                    startActivity(new Intent(LoginActivity.this, LocationActivity.class).
                            putExtra("user_doc_id", user_id.getText().toString()));
                } else {
                    layout_user_id.setError("User Does not Exists.Enter correct ID or Contact Plant Operator");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            layout_user_id.setError(null);
                        }
                    }, 2000);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkServices()) {
            getLocationPermission();
        }
    }

    private boolean checkServices() {
        if (isServiceOK()) {
            return isGpsEnabled();
        }
        return false;
    }


    private boolean isServiceOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available,
                    ERROR_DIALOG_REQUEST);
            dialog.show();

        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;

    }


    private boolean isGpsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
    protected void onStart() {
        super.onStart();
        userInfoDb.getWritableDatabase();

        Cursor res = userInfoDb.retrieve();
        Log.d("Result Count: ", "onStart: " + res.getCount());
        if (res.getCount() != 0) {
            res.moveToFirst();
            startActivity(new Intent(LoginActivity.this, LocationActivity.class).
                    putExtra("user_doc_id", res.getString(0)));
            finish();
        }
    }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (!(grantResults.length > 0)) {
                Toast.makeText(this, "This application will not work without Location Permission",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


}