package com.byteshaft.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class SplashActivity extends Activity implements TextView.OnClickListener {

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
        atHome.setOnClickListener(this);
        away.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.at_home:
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.away_text:
                Uri uri = Uri.parse("http://www.google.com");
                Intent away = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(away);
                System.out.println("away clicked");
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
