/*  Skapad av Emil Baldebo
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

public class Arduino extends Activity {

    //Alla variabler
    TextView lampa1, lampa2, lampa3, temp, window, larm, testa;
    private String address = null;
    private static ProgressDialog progress;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean disconnected = false;
    private final int REQ_CODE_SPEECH_INPUT = 20;
    Handler bluetoothIn;
    private ConnectedThread mConnectedThread;
    private StringBuilder recDataString = new StringBuilder();

    final int handlerState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newInt = getIntent();
        //Hämtar addressen från klassen Enheter
        address = newInt.getStringExtra(Enheter.EXTRA_ADDRESS);

        setContentView(R.layout.activity_arduino);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);

        //Widgets
        Button stt = (Button) findViewById(R.id.stt);
        Button disconnect = (Button)findViewById(R.id.disconnect);
        lampa1 = (TextView)findViewById(R.id.lampa1);
        lampa2 = (TextView)findViewById(R.id.lampa2);
        lampa3 = (TextView)findViewById(R.id.lampa3);
        testa = (TextView)findViewById(R.id.header);
        temp = (TextView)findViewById(R.id.temp);
        window = (TextView)findViewById(R.id.window);
        larm = (TextView)findViewById(R.id.larm);

        bluetoothIn = new Handler(new Handler.Callback() {
            public boolean handleMessage(android.os.Message msg) {
                if(msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("Q");
                    if (endOfLineIndex > 0) {
                        if(recDataString.charAt(0) == 'P') {
                            String lampa1Value = recDataString.substring(1,2);
                            String lampa2Value = recDataString.substring(2,3);
                            String lampa3Value = recDataString.substring(3,4);
                            String tempValue = recDataString.substring(4,6);
                            String windowValue = recDataString.substring(6,7);
                            String larmValue = recDataString.substring(7,8);

                            if (lampa1Value.equals("1")) lampa1.setText("Lampa 1 PÅ");
                                else lampa1.setText("Lampa 1 AV");
                            if (lampa2Value.equals("1")) lampa2.setText("Lampa 2 PÅ");
                                else lampa2.setText("Lampa 2 AV");
                            if (lampa3Value.equals("1")) lampa3.setText("Lampa 3 PÅ");
                                else lampa3.setText("Lampa 3 AV");
                            temp.setText(tempValue + (char)0x00B0 + "C");
                            if (windowValue.equals("1")) window.setText("Fönster öppet!");
                                else window.setText("Fönster stängt.");
                            if (larmValue.equals("1")) larm.setText("Larm på.");
                                else larm.setText("Larm av.");

                        }
                        recDataString.delete(0, recDataString.length());


                    }


                }
                return true;
            }
        });

        //Använd bakgrundsklassen Bluetooth för att ansluta.
        new Bluetooth().execute();

        //Lyssnarna för alla switchar. Gör något och skickar ett meddelande beroende på vad.
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

    //Metoder för att kommunicera med Arduino. Skickar 1,2,3,4,5,6,7,8 som hanteras av Arduinon.
    private void disconnect() {
        if (btSocket != null) {
            try{
                btSocket.close();
                disconnected = true;
                Intent disconnect = new Intent(Arduino.this, Enheter.class);
                startActivity(disconnect);

            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }



    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) && !disconnected) {
                Intent failConnect = new Intent(Arduino.this, Enheter.class);
                Toast.makeText(getApplicationContext(), "Bluetoothanslutningen bröts!", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), "Tal funkar ej", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String[] commands = {
                "tänd lampa 1",
                "släck lampa 1",
                "tänd lampa 2",
                "släck lampa 2",
                "tänd lampa 3",
                "släck lampa 3",
                "larm på",
                "stäng av larm"
        };

        switch(requestCode) {
            case REQ_CODE_SPEECH_INPUT : {
                if(resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(result.get(0).equals(commands[0]) || result.get(0).equals(commands[1])) {
                        mConnectedThread.write("1");
                    }

                    if(result.get(0).equals(commands[2]) || result.get(0).equals(commands[3])) {
                        mConnectedThread.write("2");
                    }

                    if(result.get(0).equals(commands[4]) || result.get(0).equals(commands[5])) {
                        mConnectedThread.write("3");
                    }

                    if(result.get(0).equals(commands[6]) || result.get(0).equals(commands[7])) {
                        mConnectedThread.write("4");
                    }
                }

            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream inPut;
        private final OutputStream outPut;

        ConnectedThread(BluetoothSocket sockerino) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = sockerino.getInputStream();
                tmpOut = sockerino.getOutputStream();
            } catch(IOException e){
                Toast.makeText(getApplicationContext(), "Något gick väldigt fel!", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getBaseContext(), "Detta fungerar inte...", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    //Bakgrundsklass för att ansluta till HC-06 via bluetooth.
    private class Bluetooth extends AsyncTask<Void, Void, Void> {

        private boolean ansluten = true;

        //Visar en dialog när klassen startar.
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Arduino.this, "Ansluter...", "Vänta!");
        }

        //Ansluter till HC-06
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
                ansluten = false;
            }
            return null;
        }

        //Kollar om anslutningen lyckats och meddelar användaren.
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(!ansluten) {
                Toast.makeText(getApplicationContext(), "Anslutning misslyckat", Toast.LENGTH_LONG).show();
                Intent failConnect = new Intent(Arduino.this, Enheter.class);
                startActivity(failConnect);
            } else {
                Toast.makeText(getApplicationContext(), "Ansluten.", Toast.LENGTH_SHORT).show();
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
