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
import android.os.Bundle;
import android.app.Activity;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class Arduino extends Activity {

    //Alla variabler
    TextView lampa1, lampa2, lampa3, temp, window, larm;
    private String address = null;
    private static ProgressDialog progress;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean disconnected = false;
    private final int REQ_CODE_SPEECH_INPUT = 20;

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
        temp = (TextView)findViewById(R.id.temp);
        window = (TextView)findViewById(R.id.window);
        larm = (TextView)findViewById(R.id.larm);

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
                    if(result.get(0).equals(commands[0])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("1".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }

                    if(result.get(0).equals(commands[1])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("2".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }

                    if(result.get(0).equals(commands[2])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("3".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }

                    if(result.get(0).equals(commands[3])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("4".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }

                    if(result.get(0).equals(commands[4])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("5".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }

                    if(result.get(0).equals(commands[5])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("6".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }

                    if(result.get(0).equals(commands[6])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("7".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }

                    if(result.get(0).equals(commands[7])) {
                        if(btSocket != null) {
                            try {
                                btSocket.getOutputStream().write("8".getBytes());
                            } catch(IOException e) {
                                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
                            } break;
                        }
                    }
                }

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
