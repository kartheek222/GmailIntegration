package com.example.krishna.gmailintegration;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.example.krishna.gmailintegration.gmail.GmailAccount;
import com.example.krishna.gmailintegration.integration.GmailIntergrationActivity;
import com.example.krishna.gmailintegration.sample.SplashActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showSample(View view){
        Intent intent=new Intent(this, SplashActivity.class);
        startActivity(intent);
    }


    public void showGmail(View view){
        Intent intent=new Intent(this, GmailAccount.class);
        startActivity(intent);

//        Intent intent = new Intent();
//        intent.setAction("com.CUSTOM_INTENT");
//        intent.putExtra("val", "Hai");
//        sendBroadcast(intent);
    }

    public void showGmailIntegration(View view){
        Intent intent=new Intent(this, GmailIntergrationActivity.class);
        startActivity(intent);
    }
}
