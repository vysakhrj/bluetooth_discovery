package com.example.covid;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String TAG="MainActivity";

    BluetoothAdapter bluetoothAdapter;
    public DeviceListAdapter mDeviceListAdapter;
    ListView newDevices;

    public ArrayList<BluetoothDevice> BTDevices =new ArrayList<>();

    private final BroadcastReceiver mBroadcastReciever1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action =intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)){
                final int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,bluetoothAdapter.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"OnRecieve:state OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"OnRecieve:STATE_TURNING_OF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"OnRecieve:STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"OnRecieve:STATE_TURNING_ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReciever2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action =intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode=intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,bluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG,"OnRecieve:DISCOVERABILITY ENABLED");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG,"OnRecieve:DISCOVERABILITY ENABLED. ABLE TO RECIEVE CONNECTIONS ");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG,"OnRecieve:DISCOVERABILITY DISABLED");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG,"OnRecieve:CONNECTING");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG,"OnRecieve:CONNECTED");
                        break;
                }
            }
        }
    };
    private BroadcastReceiver mBroadcastReciever3= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action =intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device= intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTDevices.add(device);


                Log.d(TAG,"OnRecieve:"+ device.getName()+":"+device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, BTDevices);
                newDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy:calledd");
        super.onDestroy();
        unregisterReceiver(mBroadcastReciever1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnonoff= findViewById(R.id.btn_onoff);
        final Button disc_enabdisab=findViewById(R.id.disc_onoff);
        Button discover=findViewById(R.id.discover);
        newDevices=findViewById(R.id.devices);
        BTDevices= new ArrayList<>();

        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        btnonoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Working");
                enableDisableBT();
            }
        });

        disc_enabdisab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent discoverbleIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverbleIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
                startActivity(discoverbleIntent);

                IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mBroadcastReciever2, BTIntent);
            }
        });

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();

                    Log.d(TAG, "DISCOVERING");

                    CheckBTPermissions();

                    bluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReciever3, discoverDevicesIntent);

                }
                if (!bluetoothAdapter.isDiscovering()){

                    CheckBTPermissions();
                    bluetoothAdapter.startDiscovery();

                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);

                    registerReceiver(mBroadcastReciever3, discoverDevicesIntent);

                }
            }
        });

    }

    private void CheckBTPermissions() {


        if(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck= this.checkSelfPermission("MANIFEST.permission.ACCESS_FINE_LOCATION");
            if(permissionCheck!=0){

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1001);
            }
        }
        else {
            Log.d(TAG,"SDKVERSION<LOLLPOP");
        }
    }

    public void enableDisableBT(){
        if (bluetoothAdapter==null){
            Log.d(TAG,"enableDisabledBT:NO BT capablilities");
        }
        if(!bluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciever1, BTIntent);
        }
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciever1, BTIntent);
        }
    }
}


