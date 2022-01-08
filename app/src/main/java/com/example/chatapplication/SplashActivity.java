package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    final private static int splashTimeOut = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                Log.e("ssss", "firebaseUser   "+ firebaseUser);

                Intent i;
                if (firebaseUser != null) {
                    i = new Intent(SplashActivity.this, HomeActivity.class);
                } else {
                    i = new Intent(SplashActivity.this, SignupActivity.class);
                }
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();

            }
        }, splashTimeOut);
    }
}