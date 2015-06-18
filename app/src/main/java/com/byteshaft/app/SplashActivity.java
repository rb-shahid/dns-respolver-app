package com.byteshaft.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SplashActivity extends Activity {

    TextView youAre;
    TextView atHome;
    TextView away;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        youAre = (TextView) findViewById(R.id.you_are);
        atHome = (TextView) findViewById(R.id.at_home);
        away = (TextView) findViewById(R.id.away_text);

    }
}
