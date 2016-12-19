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

public class Devices extends Activity {

    private ListView deviceList;

    //New bluetooth and string with the adress.
    private BluetoothAdapter myBluetooth = null;
    public static final String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        //Button to show paired devices.
        Button btnPaired = (Button) findViewById(R.id.parkopplade);
        //Populate the list with paired devices.
        deviceList = (ListView)findViewById(R.id.lista);

        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        //Quit app if device doesn't have bluetooth.
        if(myBluetooth == null) {
            Toast.makeText(getApplicationContext(), "Cant find bluetooth!", Toast.LENGTH_LONG).show();
            finish();
        } else if(!myBluetooth.isEnabled()) {
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon, 1);
        }

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });
    }

    //Array that gets filled with paired devices.
    private void pairedDevicesList(){
        Set<BluetoothDevice> pairedDevices = myBluetooth.getBondedDevices();
        ArrayList<String> list = new ArrayList<>();

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice bt : pairedDevices) {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            //Show message if no paired devices were found.
            Toast.makeText(getApplicationContext(), "No paired devices found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
                deviceList.setOnItemClickListener(myListClickListener);
    }

    //Try to connect and take you to Connected-class if successful.
    private final AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(Devices.this, Connected.class);

            i.putExtra(EXTRA_ADDRESS, address);
            startActivity(i);
        }
    };

}
