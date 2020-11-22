package com.example.bluetoothchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {
    //a
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        Intent intent = this.getIntent();
        String msg = intent.getExtras().getString("msg");
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

    }
}