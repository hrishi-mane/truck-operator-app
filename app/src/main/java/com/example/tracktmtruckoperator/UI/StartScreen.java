package com.example.tracktmtruckoperator.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tracktmtruckoperator.R;
import com.google.firebase.auth.FirebaseAuth;

public class StartScreen extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView account_creation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(StartScreen.this, LoginActivity.class);
                startActivity(intent);
            }
        }, 2000);

    }
}