package com.example.bluetoothchat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ConnectionActivity extends AppCompatActivity {
    //init BT service vars
    int REQUEST_ENABLE_BT=1;
    //public BluetoothAdapter bluetoothAdapter;
    protected Set<BluetoothDevice> pairedDevices;

    //added comment for pull request
    //init list and adapter for paired devices context menu
    @SuppressWarnings("FieldMayBeFinal")
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
            pairedDevices = bluetoothAdapter.getBondedDevices();

            //update list + adapter with found devices
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    devices_list.add(device.getName() + "\n" + device.getAddress());
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

        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.paired_devices_cm, menu);
    }

    //Connect to selected device from menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //hail mary
        if (item.getItemId() == R.id.cmenu_connect) {

            //get the MenuItem text
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String key = ((TextView) info.targetView).getText().toString();
            Log.d("Connect","CONNECTION TO: "+key);

            if (!key.isEmpty() && bluetoothAdapter != null) {
                // Get the device by MAC address, which is the last 17 chars in the View
                String MACadd = key.substring(key.length() - 17);
                BluetoothDevice bt_device = bluetoothAdapter.getRemoteDevice(MACadd);

                //start new thread to try and connect to the BT device
                Log.d("Connect", "trying to connect to " + bt_device.getName());

                //ConnectThread connectThread = new ConnectThread(bt_device);

                //old code to switch tasks
                Intent intent = new Intent(ConnectionActivity.this, ChatActivity.class);
                intent.putExtra("bt_device", bt_device);
                startActivity(intent);


            } else {
                if (bluetoothAdapter == null) {
                    Log.e("Connect","BT adapter == null");
                }
                Log.e("Connect","CONNECTION failed");
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

}