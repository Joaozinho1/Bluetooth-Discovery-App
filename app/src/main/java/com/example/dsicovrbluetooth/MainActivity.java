package com.example.dsicovrbluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static  final int REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private ListView devicesList;
    private Button scanningBtn;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> listAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        devicesList = findViewById(R.id.devicesList);
        scanningBtn = findViewById(R.id.scanningBtn);

        //Create an array adapter to display devices detected
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        devicesList.setAdapter(listAdapter);

        //Check bluetooth state

        checkBluetoothState();

        //register a dedicated receiver for Blutooth actions

        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        registerReceiver(devicesFoundReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        scanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
                    if(checkCoarseLocationPermission()){
                        listAdapter.clear();
                        bluetoothAdapter.startDiscovery();
                    }
                }else {
                    checkBluetoothState();
                }
            }
        });


        //checking the permissing at the beginning of an app
        checkCoarseLocationPermission();



    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(devicesFoundReceiver);
    }

    private boolean checkCoarseLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_ACCESS_COARSE_LOCATION);
            return false;
        }else{
            return true;
        }
    }

    private  void checkBluetoothState(){
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth is not supported on your devices!", Toast.LENGTH_SHORT).show();
        }else {

            if(bluetoothAdapter.isEnabled()) {
                if (bluetoothAdapter.isDiscovering()) {

                    Toast.makeText(this, "Device discovering process!", Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(this, "Bluetooth is diables!", Toast.LENGTH_SHORT).show();
                    scanningBtn.setEnabled(true);
                }
            }else{
                    Toast.makeText(this, "You need to enable Bluetooth!", Toast.LENGTH_SHORT).show();
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
                }
            }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == REQUEST_ENABLE_BLUETOOTH){
            checkBluetoothState();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"access coarse location allowed. You can san devices", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"access coarse location forbidden. You can't scan Bluetooth devices", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

    //method for detecting dvices

    private final BroadcastReceiver devicesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listAdapter.add(device.getName() + "\n" + device.getAddress());
                listAdapter.notifyDataSetChanged();

            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                scanningBtn.setText("Scanning Bluetooth Devices");

            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                scanningBtn.setText("Scanning in progress...");
            }

        }
    };
}