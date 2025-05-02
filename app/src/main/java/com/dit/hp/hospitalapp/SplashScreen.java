package com.dit.hp.hospitalapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.dit.hp.hospitalapp.utilities.Preferences;


public class SplashScreen extends AppCompatActivity {

    ProgressBar progressBar;
    Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        progressBar = findViewById(R.id.loadingProgress);
        progressBar.setVisibility(View.VISIBLE);

        Preferences.getInstance().loadPreferences(SplashScreen.this);
        if(Preferences.getInstance().isLoggedIn){
            System.out.println("Logged In "+Preferences.getInstance().isLoggedIn);

        }else{
            loadPrefrence();
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Preferences.getInstance().isLoggedIn){
                     Intent mainIntent = new Intent(SplashScreen.this, Homescreen.class);
                        SplashScreen.this.startActivity(mainIntent);
                        SplashScreen.this.finish();
                 }else{

                    Intent mainIntent = new Intent(SplashScreen.this, LoginScreen.class);
                    startActivity(mainIntent);
                    finish();
                 }
            }
        }, 2500);
    }



    private void loadPrefrence() {
        Preferences.getInstance().loadPreferences(SplashScreen.this);

        Preferences.getInstance().isLoggedIn = false;
        Preferences.getInstance().savePreferences(SplashScreen.this);
    }
}