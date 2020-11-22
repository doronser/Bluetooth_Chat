package com.example.bluetoothchat;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



public class ChatActivity extends AppCompatActivity {
    //member fields
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread mConnectThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        Intent intent = this.getIntent();
        BluetoothDevice bt_device = intent.getExtras().getParcelable("bt_device");
        Toast.makeText(getApplicationContext(), "Connecting to " + bt_device.getName(), Toast.LENGTH_SHORT).show();

        ConnectThread connectThread = new ConnectThread(bt_device);
        connectThread.start();
        Log.d("Chat","Connection Success!");
        connectThread.cancel();
    }

    public void manageMyConnectedSocket(BluetoothSocket socket,BluetoothDevice device) {
        //TODO: Implement chat communication
        Log.d("Socket","Called manageMyConnectedSocket with: "+device.getName() + socket.toString());
    }

    //Connection class
    private class ConnectThread extends Thread {
        private final BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
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

        public void start() {
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
            manageMyConnectedSocket(mmSocket,mmDevice);
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