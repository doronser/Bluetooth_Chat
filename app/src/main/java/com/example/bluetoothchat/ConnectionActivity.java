package com.example.bluetoothchat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ConnectionActivity extends AppCompatActivity {
    //init BT service vars
    int REQUEST_ENABLE_BT=1;



    //added comment for pull request
    //init list and adapter for paired devices context menu
    private ArrayList<String> devices_list = new ArrayList<>();
    private ArrayAdapter<String> devices_listAdapter;

    //init list and adapter for chat messages
    //private ArrayList<String> chat_list = new ArrayList<>();
    //private ArrayAdapter<String> chat_listAdapter;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_layout);



        //get BT adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Log.w("no BT", "Failed to get device BlueTooth adapter.");
            //System.exit(0);
        }

        //Paired Devices Context Menu
        devices_listAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices_list);
        ListView lv =  findViewById(R.id.paried_devices_lv);
        lv.setAdapter(devices_listAdapter);
        registerForContextMenu(lv);

        //add dummy item to devices list
        devices_list.add("dummy list item");
        devices_listAdapter.notifyDataSetChanged();

        //button to turn on BT
        Button BT_on_btn = this.findViewById(R.id.BT_on_btn);
        BT_on_btn.setOnClickListener(v -> {
            assert bluetoothAdapter != null;
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent;
                enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                BT_on_btn.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.button_green));
                Toast.makeText(getApplicationContext(),"Bluetooth Turned ON",Toast.LENGTH_SHORT).show();
            } else {
                bluetoothAdapter.disable();
                BT_on_btn.setBackgroundTintList(ContextCompat.getColorStateList(this,R.color.button_red));
                Toast.makeText(getApplicationContext(),"Bluetooth Turned OFF", Toast.LENGTH_SHORT).show();
            }
        });

        //button to get paired devices list
        Button show_paired_btn = this.findViewById(R.id.show_paired_btn);
        show_paired_btn.setOnClickListener(v -> {
            //clear any previous devices
            devices_list.clear();

            //get paired devices object (name + MAC per device)
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            //update list + adapter with found devices
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    devices_list.add(device.getName());
                }
                devices_listAdapter.notifyDataSetChanged();
            }
        });




        //button to make device discoverable
        Button make_visible_btn = this.findViewById(R.id.make_visible_btn);
        make_visible_btn.setOnClickListener(v -> {

            Intent makeVisibleIntent;
            makeVisibleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivity(makeVisibleIntent);
            Toast.makeText(getApplicationContext(),"Device is now visible", Toast.LENGTH_SHORT).show();
            //old code to switch tasks
//            Intent intent = new Intent(ConnectionActivity.this, ChatActivity.class);
//            intent.putExtra("msg", "switched activities :)");
//            startActivity(intent);

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
        if (item.getItemId() == R.id.cmenu_connect) {
            Toast.makeText(getApplicationContext(), "connecting...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }
}