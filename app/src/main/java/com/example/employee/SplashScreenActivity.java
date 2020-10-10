package com.example.employee;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               intent.putExtra("splash", "oke" );
                startActivity(intent);
                SplashScreenActivity.this.finish();
            }
        }, 3000);
    }
}