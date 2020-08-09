package com.example.tracktmtruckoperator.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tracktmtruckoperator.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView account_creation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        mAuth = FirebaseAuth.getInstance();
        account_creation = (TextView) findViewById(R.id.textview_account_creation);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            account_creation.setText("Creating Guest Account");
            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Intent intent = new Intent(StartScreen.this, MainActivity.class);
                        startActivity(intent);
                        Log.d("Login Status", "Login Success");
                    }
                    else{
                        Log.d("Login Status", "Login failed");
                    }
                }
            });
        }

        else{

            Intent intent = new Intent(StartScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}