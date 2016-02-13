package com.pure.gothic.hackathon.idhackandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
// jae, lemonhall2, CYhan, gon
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=new Intent(MainActivity.this,ChatBubbleActivityForDoctor.class);
        startActivity(intent);
    }
}
