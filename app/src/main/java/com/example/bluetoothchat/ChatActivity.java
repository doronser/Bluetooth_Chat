package com.example.bluetoothchat;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
//import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;



public class ChatActivity extends AppCompatActivity {
    //init list and adapter for chat messages
    private ArrayList<String> chat_list = new ArrayList<>();
    private ArrayAdapter<String> chat_listAdapter;

    //member fields
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread mConnectThread;
    SendReceive sendReceive;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        chat_listAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chat_list);
        ListView lv =  findViewById(R.id.paried_devices_lv);
        lv.setAdapter(chat_listAdapter);
        registerForContextMenu(lv);

        Intent intent = this.getIntent();
        BluetoothDevice bt_device = intent.getExtras().getParcelable("bt_device");
        Toast.makeText(getApplicationContext(), "Connecting to " + bt_device.getName(), Toast.LENGTH_SHORT).show();

        mConnectThread = new ConnectThread(bt_device);
        mConnectThread.start();
        Log.d("Chat","Connection Success!");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("Chat","Closing after 10 seconds");
        mConnectThread.cancel();
    }

    Handler handler=new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    Log.d("handler","Listening");
                    break;
                case STATE_CONNECTING:
                    Log.d("handler","Connecting");
                    break;
                case STATE_CONNECTED:
                    Log.d("handler","Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    Log.d("handler","Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    chat_list.add(tempMsg);
                    chat_listAdapter.notifyDataSetChanged();
                    break;
            }
            return true;
        }
    });


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
            sendReceive = new SendReceive(mmSocket);
            sendReceive.start();
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

    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}