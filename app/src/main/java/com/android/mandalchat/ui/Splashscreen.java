package com.android.mandalchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.mandalchat.R;
import com.romainpiel.titanic.library.Titanic;
import com.romainpiel.titanic.library.TitanicTextView;

/**
 * Created by vishal on 19/4/18.
 */

public class Splashscreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 4000;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);





        CountDown();
    }

    private void CountDown() {

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                Intent intentMain = new Intent(Splashscreen.this,LoginActivity.class);
                startActivity(intentMain);
                finish();


            }
        }, SPLASH_TIME_OUT);

    }
}
