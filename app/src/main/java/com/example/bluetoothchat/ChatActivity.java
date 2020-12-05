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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
//import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;



@SuppressWarnings("FieldCanBeLocal")
public class ChatActivity extends AppCompatActivity {
    //init list and adapter for chat messages
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<String> chat_list = new ArrayList<>();
    private ArrayAdapter<String> chat_listAdapter;

    //member fields
    private BluetoothAdapter bluetoothAdapter;
    private ConnectThread mConnectThread;
    SendReceive sendReceive;

    static final int STATE_CONNECTION_FAILED=1;
    static final int STATE_MESSAGE_RECEIVED=2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        chat_listAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chat_list);
        
        //get UI elements
        ListView lv =  findViewById(R.id.chat_lv);
        Button send_btn = findViewById(R.id.send_btn);
        EditText Chat_msg_txt = findViewById(R.id.Chat_msg_txt);

        //get BT adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //get device to connect to from MainActivity intent
        Intent intent = this.getIntent();
        BluetoothDevice bt_device = intent.getExtras().getParcelable("bt_device");

        //Connect to BT server
        Toast.makeText(getApplicationContext(), "Connecting to " + bt_device.getName(), Toast.LENGTH_SHORT).show();
        if (bluetoothAdapter != null) {
            mConnectThread = new ConnectThread(bt_device);
            mConnectThread.start();
            Log.d("Chat", "Connection Success!");
        }


        //init received messages list
        lv.setAdapter(chat_listAdapter);
        registerForContextMenu(lv);
        chat_list.clear();
        chat_listAdapter.notifyDataSetChanged();

        //send button logic
        send_btn.setOnClickListener(v -> {
            String msg_str= String.valueOf(Chat_msg_txt.getText());
            Log.d("Chat", "Sending message: "+ msg_str);
            sendReceive.write(msg_str.getBytes()); //send data using new thread
            Chat_msg_txt.setText(""); //clear message box
                });

    }


    //close connection when chat activity closes
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnectThread.cancel();
    }

    Handler handler=new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_CONNECTION_FAILED:
                    Log.e("handler","Connection Failed");
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
        //private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            //mmDevice = device;
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
        @SuppressWarnings("unused")
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("Connect", "Could not close the client socket", e);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket)
        {
            Looper.prepare();
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("BT_IO", "failted to get BT IOstream.");
            }

            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            //noinspection InfiniteLoopStatement
            while (true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    if (bytes > 0) {
                        Log.d("!!!", "got a message:");
                    }
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