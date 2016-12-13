/*  Skapad av Emil Baldebo
    9/12/2016 */
package com.example.ebaldebo.bluetootharduino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class Arduino extends Activity {

    //Alla variabler
    private Switch lysa, blinka, asynk, ljud;
    private String address = null;
    private static ProgressDialog progress;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newInt = getIntent();
        //Hämtar addressen från klassen Enheter
        address = newInt.getStringExtra(Enheter.EXTRA_ADDRESS);

        setContentView(R.layout.activity_arduino);

        //Widgets
        lysa = (Switch)findViewById(R.id.lysa);
        blinka = (Switch)findViewById(R.id.blinka);
        asynk = (Switch)findViewById(R.id.asynk);
        ljud = (Switch)findViewById(R.id.ljud);
        Button disconnect = (Button) findViewById(R.id.disconnect);

        //Använd bakgrundsklassen Bluetooth för att ansluta.
        new Bluetooth().execute();

        //Lyssnarna för alla switchar. Gör något och skickar ett meddelande beroende på vad.
        lysa.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(lysa.isChecked()) {
                    lysaOn();
                    Toast.makeText(getApplicationContext(), "Lampa på!", Toast.LENGTH_SHORT).show();
                } else {
                    lysaAv();
                    Toast.makeText(getApplicationContext(), "Lampa av!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        blinka.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(blinka.isChecked()) {
                    blinkaOn();
                    Toast.makeText(getApplicationContext(), "Blinkar", Toast.LENGTH_SHORT).show();
                } else {
                    blinkaAv();
                    Toast.makeText(getApplicationContext(), "Blinkar inte", Toast.LENGTH_SHORT).show();
                }
            }
        });

        asynk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(asynk.isChecked()) {
                    asynkOn();
                    Toast.makeText(getApplicationContext(), "Blinkar asynkront", Toast.LENGTH_SHORT).show();
                } else {
                    asynkAv();
                    Toast.makeText(getApplicationContext(), "Blinkar inte", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ljud.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(ljud.isChecked()) {
                    ljudOn();
                    Toast.makeText(getApplicationContext(), "Ljud på", Toast.LENGTH_SHORT).show();
                } else {
                    ljudAv();
                    Toast.makeText(getApplicationContext(), "Ljud av", Toast.LENGTH_SHORT).show();
                }
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

    //Metoder för att kommunicera med Arduino. Skickar 1,2,3,4,5,6,7,8 som hanteras av Arduinon.
    private void disconnect() {
        if (btSocket != null) {
            try{
                btSocket.close();
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void lysaOn() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("1".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void lysaAv() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("2".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void blinkaOn() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("3".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void blinkaAv() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("4".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void asynkOn() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("5".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void asynkAv() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("6".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void ljudOn() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("7".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void ljudAv() {
        if(btSocket != null) {
            try {
                btSocket.getOutputStream().write("8".getBytes());
            } catch(IOException e) {
                Toast.makeText(getApplicationContext(), "FEL!", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Ansluten.", Toast.LENGTH_LONG).show();
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
