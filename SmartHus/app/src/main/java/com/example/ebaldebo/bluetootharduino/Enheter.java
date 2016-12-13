/*  Skapad av Emil Baldebo
    9/12/2016 */

package com.example.ebaldebo.bluetootharduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class Enheter extends Activity {

    private ListView deviceList;

    //Ny bluetooth och en string för addressen.
    private BluetoothAdapter myBluetooth = null;
    public static final String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enheter);

        //Knapp för att visa parkopplade enheter.
        Button btnPaired = (Button) findViewById(R.id.parkopplade);
        //Lista som populeras med parkopplade enheter om det finns några.
        deviceList = (ListView)findViewById(R.id.lista);

        //sätter myBluetooth till enhetens standardbluetooth-enhet
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        //Om den saknar bluetooth avsluta appen.
        if(myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Hittar ej bluetooth!", Toast.LENGTH_LONG).show();
            finish();
        } else if(!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        //Lyssnaren som gör att det händer något när man trycker på knappen som visar
        //parkopplade enheter.
        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });
    }

    //Metod som skapar ett Array av strings och fyller med resultatet av parkopplade enheter.
    private void pairedDevicesList(){
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Namn + radbrytning + address
            }
        } else {
            //Om det inte finns några parkopplade enheter visa meddelande.
            Toast.makeText(getApplicationContext(), "Inga parkopplade enheter funna", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
                deviceList.setOnItemClickListener(myListClickListener);
    }

    //Lyssnare för att försöka ansluta till enheten. Tar dig också till Arduinoklassen.
    private final AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(Enheter.this, Arduino.class);

            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };

}
