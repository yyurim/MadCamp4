package com.madcamp.petclub.Friends;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.madcamp.petclub.R;
import com.madcamp.petclub.login.LoginActivity;
import com.madcamp.petclub.login.SharedPreference;

public class SplashActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGTH = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //FirebaseAuth.getInstance().signOut();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent mainIntent = null;
                if ( SharedPreference.getAttribute(getApplicationContext(),"userID")==null) {
                    mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                } else {
                    mainIntent = new Intent(SplashActivity.this, FriendActivity.class);
                }
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}