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
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;

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

            //TODO: implement BT connection
//            //get the MenuItem text
//            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//            String key = ((TextView) info.targetView).getText().toString();
//
//            // Get the device MAC address, which is the last 17 chars in the View
//            String MACadd = key.substring(key.length() - 17);
//
//            BluetoothDevice bt_device = null;
//            Toast.makeText(getApplicationContext(), "connecting...", Toast.LENGTH_SHORT).show();
//
//            if (pairedDevices.size() > 0) {
//                for (BluetoothDevice device : pairedDevices) {
//                    if (device.getName().equals("DORONZ")) {
//                        bt_device = device;
//                        ConnectThread testThread =  new ConnectThread(bt_device);
//                        testThread.run();
//                        testThread.cancel();
//                    }
//                }
//            } else {
//                Log.w("Connect", "failed to find devices");
//            }
            Toast.makeText(getApplicationContext(), "connecting...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onContextItemSelected(item);
    }


    //Connection class
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                UUID MY_UUID= UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e("Connect", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("Connect", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(mmSocket);
            Toast.makeText(getApplicationContext(), "Connection Success!", Toast.LENGTH_SHORT).show();
            Log.d("connect", "Connection Success!");
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("Connect", "Could not close the client socket", e);
            }
        }
    }
}