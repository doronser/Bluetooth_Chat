package com.example.bluetoothchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ConnectionActivity extends AppCompatActivity {
    //init list and adapter for paired devices context menu
    private ArrayList<String> devices_list = new ArrayList<>();
    private ArrayAdapter<String> devices_listAdapter;

    //init list and adapter for chat messages
    //private ArrayList<String> chat_list = new ArrayList<>();
    //private ArrayAdapter<String> chat_listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);

        //Paired Devices Context Menu
        devices_listAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices_list);
        ListView lv =  findViewById(R.id.paried_devices_lv);
        lv.setAdapter(devices_listAdapter);
        registerForContextMenu(lv);

        //add dummy item
        devices_list.add("dummy list item");
        devices_listAdapter.notifyDataSetChanged();

        //button to switch activity
        Button btnObj = this.findViewById(R.id.make_visible_btn);
        btnObj.setOnClickListener(v -> {

            Intent intent = new Intent(ConnectionActivity.this, ChatActivity.class);
            intent.putExtra("msg", "switched activities :)");
            startActivity(intent);

        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.paired_devices_cm, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //AdapterView.AdapterContextMenuInfo info =
        //        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.cmenu_connect) {
            Toast.makeText(getApplicationContext(), "connecting...", Toast.LENGTH_SHORT).show();
            devices_list.set(0, "changed!");
            devices_listAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}