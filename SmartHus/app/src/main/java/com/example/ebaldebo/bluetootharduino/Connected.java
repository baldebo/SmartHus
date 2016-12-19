/*  Created by Emil Baldebo
    9/12/2016 */
package com.example.ebaldebo.bluetootharduino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.app.Activity;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class Connected extends Activity {

    //All variables.
    private TextView lamp1, lamp2, lamp3, temp, window, alarm;
    private String address = null;
    private static ProgressDialog progress;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean disconnected = false;
    private final int REQ_CODE_SPEECH_INPUT = 20;
    private Handler bluetoothIn;
    private ConnectedThread mConnectedThread;
    private final StringBuilder recDataString = new StringBuilder();
    private Vibrator v;
    private String message = "00000";
    private final int handlerState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newInt = getIntent();
        //Gets address
        address = newInt.getStringExtra(Devices.EXTRA_ADDRESS);

        setContentView(R.layout.activity_connected);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);

        //Widgets
        Button stt = (Button) findViewById(R.id.stt);
        Button disconnect = (Button)findViewById(R.id.disconnect);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        lamp1 = (TextView)findViewById(R.id.lampa1);
        lamp2 = (TextView)findViewById(R.id.lampa2);
        lamp3 = (TextView)findViewById(R.id.lampa3);
        temp = (TextView)findViewById(R.id.temp);
        window = (TextView)findViewById(R.id.window);
        alarm = (TextView)findViewById(R.id.larm);


        //Handler that parses and takes care of messages from the Arduino.
        bluetoothIn = new Handler(new Handler.Callback() {
            public boolean handleMessage(android.os.Message msg) {
                if(msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("Q");
                    if (endOfLineIndex > 0) {
                        if(recDataString.charAt(0) == 'P') {

                            String lamp1Value = recDataString.substring(1,2);
                            String lamp2Value = recDataString.substring(2,3);
                            String lamp3Value = recDataString.substring(3,4);
                            String tempValue = recDataString.substring(4,6);
                            String windowValue = recDataString.substring(6,7);
                            String alarmValue = recDataString.substring(7,8);




                            if (lamp1Value.equals("1")) {
                                if(!lamp1Value.equals(Character.toString(message.charAt(0)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Lamp 1 ON", Toast.LENGTH_SHORT).show();
                                }
                                lamp1.setText(R.string.lamp1On);
                            }
                                else if(lamp1Value.equals("0")) {
                                if(!lamp1Value.equals(Character.toString(message.charAt(0)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Lamp 1 OFF", Toast.LENGTH_SHORT).show();
                                }
                                lamp1.setText(R.string.lamp1Off);
                            }
                            if (lamp2Value.equals("1")){
                                if(!lamp2Value.equals(Character.toString(message.charAt(1)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Lamp 2 ON", Toast.LENGTH_SHORT).show();
                                }
                                lamp2.setText(R.string.lamp2On);
                            }
                                else if (lamp2Value.equals("0")){
                                if(!lamp2Value.equals(Character.toString(message.charAt(1)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Lamp 2 OFF", Toast.LENGTH_SHORT).show();
                                }
                                lamp2.setText(R.string.lamp2Off);
                            }
                            if (lamp3Value.equals("1")){
                                if(!lamp3Value.equals(Character.toString(message.charAt(2)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Lamp 3 ON", Toast.LENGTH_SHORT).show();
                                }
                                lamp3.setText(R.string.lamp3On);
                            }
                                else if(lamp3Value.equals("0")){
                                if(!lamp3Value.equals(Character.toString(message.charAt(2)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Lamp 3 OFF", Toast.LENGTH_SHORT).show();
                                }
                                lamp3.setText(R.string.lamp3Off);
                            }
                            temp.setText(tempValue + (char)0x00B0 + "C");

                            if (windowValue.equals("1")){
                                if(!windowValue.equals(Character.toString(message.charAt(3)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Window Open!", Toast.LENGTH_SHORT).show();
                                }
                                window.setText(R.string.windowOpen);
                            }
                                else if (windowValue.equals("0")){
                                if(!windowValue.equals(Character.toString(message.charAt(3)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Window Closed!", Toast.LENGTH_SHORT).show();
                                }
                                window.setText(R.string.windowClosed);
                            }
                            if (alarmValue.equals("1")) {
                                if(!alarmValue.equals(Character.toString(message.charAt(4)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Alarm On!", Toast.LENGTH_SHORT).show();
                                }
                                alarm.setText(R.string.alarmOn);
                            }
                                else if (alarmValue.equals("0")) {
                                if(!alarmValue.equals(Character.toString(message.charAt(4)))) {
                                    v.vibrate(100);
                                    Toast.makeText(getApplicationContext(), "Alarm Off!", Toast.LENGTH_SHORT).show();
                                }
                                alarm.setText(R.string.alarmOff);
                            }

                        }
                        //message variable to track changes.
                        message = "";
                        message += recDataString.substring(1,2);
                        message += recDataString.substring(2,3);
                        message += recDataString.substring(3,4);
                        message += recDataString.substring(6,7);
                        message += recDataString.substring(7,8);

                        recDataString.delete(0, recDataString.length());
                    }


                }
                return true;
            }
        });

        //Use background class to connect bluetooth.
        new Bluetooth().execute();

        //Listeners for the speech recognition.
        stt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                speech();
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                disconnect();      //method to turn on
            }
        });
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();

        unregisterReceiver(receiver);
    }

    //Methods to communicated with "Connected"-class.
    private void disconnect() {
        if (btSocket != null) {
            try{
                btSocket.close();
                disconnected = true;
                Intent disconnect = new Intent(Connected.this, Devices.class);
                startActivity(disconnect);

            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
            }
        }
    }



    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) && !disconnected) {
                Intent failConnect = new Intent(Connected.this, Devices.class);
                Toast.makeText(getApplicationContext(), "Bluetooth connection lost!", Toast.LENGTH_LONG).show();
                startActivity(failConnect);
            }
        }
    };

    private void speech() {
        Intent speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speech.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.stt));

        try {
            startActivityForResult(speech, REQ_CODE_SPEECH_INPUT);
        } catch(ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Speech not supported!", Toast.LENGTH_LONG).show();
        }
    }

    //Array with voice commands in swedish.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String[] commands = {
                "lampa 1",
                "lampa 2",
                "lampa 3",
                "alarm",
        };

        switch(requestCode) {
            case REQ_CODE_SPEECH_INPUT : {
                if(resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(result.get(0).equals(commands[0])) {
                        mConnectedThread.write("1");
                    }

                    if(result.get(0).equals(commands[1])) {
                        mConnectedThread.write("2");
                    }

                    if(result.get(0).equals(commands[2])) {
                        mConnectedThread.write("3");
                    }

                    if(result.get(0).equals(commands[3])) {
                        mConnectedThread.write("4");
                    }
                }

            }
        }
    }

    //Thread to handle stuff outside of the main thread.
    private class ConnectedThread extends Thread {
        private final InputStream inPut;
        private final OutputStream outPut;

        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch(IOException e){
                Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
            }
            inPut = tmpIn;
            outPut = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = inPut.read(buffer);
                    String readMessage = new String(buffer, 0 , bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {break;}
            }
        }
        void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                outPut.write(msgBuffer);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    //Background class to connect to HC-06
    private class Bluetooth extends AsyncTask<Void, Void, Void> {

        private boolean connected = true;

        //Shows a dialog when class is started.
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Connected.this, "Connecting...", "Please wait.");
        }

        //Connecting to HC-06
        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if(btSocket == null || !isBtConnected) {
                    BluetoothAdapter myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice hc06 = myBluetooth.getRemoteDevice(address);
                    btSocket = hc06.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();

                    mConnectedThread = new ConnectedThread(btSocket);
                    mConnectedThread.start();
                }
            } catch(IOException e) {
                connected = false;
            }
            return null;
        }

        //Checks if connected, else tells the user.
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(!connected) {
                Toast.makeText(getApplicationContext(), "Connection failed.", Toast.LENGTH_LONG).show();
                Intent failConnect = new Intent(Connected.this, Devices.class);
                startActivity(failConnect);
            } else {
                Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
