package com.capstone.wearable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private String TAG = "BluetoothActivity";
    public static BluetoothDevice mmDevice = null;
    private static Boolean stopWorker = false;
    BluetoothSocket mmSocket;
    BluetoothAdapter mBluetoothAdapter;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    String data, temp, deviceName;
    int readBufferPosition = 0;
    int count = 0;
    byte[] readBuffer = new byte[255];
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Set<BluetoothDevice> pairedDevices;
    List<String> devices;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
       sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        view = findViewById(R.id.device);
        final TextView textBox = (TextView) findViewById(R.id.device);
        if (mmDevice == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Snackbar.make(view, "Bluetooth Unavailable", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            devices = new ArrayList<String>();

            //devices.addAll(pairedDevices);
            if (pairedDevices.size() > 0) {
                Snackbar.make(view, "Bluetooth Enabled", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                for (BluetoothDevice device : pairedDevices) {
                    devices.add(device.getName());
                }
                Log.d(TAG, "Size of list: " + devices.size());
                selectDevice();
            } else {
                Snackbar.make(view, "No paired devices", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        else {
            Snackbar.make(view, "Paired with "+deviceName, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public void selectDevice() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle(R.string.select_paired)
                .setSingleChoiceItems(devices.toArray(new CharSequence[devices.size()]), 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        Log.d(TAG, "Selected postition: " + selectedPosition);
                        int i = 0;
                        Iterator<BluetoothDevice> iterator = pairedDevices.iterator();
                        Log.d(TAG,"Entering loop");
                        while (iterator.hasNext()) {
                            if (selectedPosition == i) {
                                mmDevice = iterator.next();
                                break;
                            }
                            ++i;
                        }
                        Log.d(TAG,"Outide loop");
                        if (mmDevice != null) {
                            Toast.makeText(getApplicationContext(), devices.get(selectedPosition), Toast.LENGTH_SHORT).show();
                            TextView textBox = (TextView) findViewById(R.id.deviceName);
                            textBox.setText(devices.get(selectedPosition));
                            Log.d(TAG,"Selected position "+selectedPosition);
                            Log.d(TAG, "Name: "+devices.get(selectedPosition));
                            deviceName = devices.get(selectedPosition);
                            Intent intentComm = new Intent(getApplicationContext(), CommunicationService.class);
                            intentComm.putExtra("device", mmDevice);
                            intentComm.putExtra("phonenumber", sharedPreferences.getString("phonenumber", "1234567891"));
                            intentComm.putExtra("password", sharedPreferences.getString("password", "password"));
                            startService(intentComm);
                            connectionFunction();
                            Log.d(TAG,"done");
                        } else {
                            Snackbar.make(view, "No device selected", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }

                    }
                }).show();
    }

    public void onClickBtn(View v) {
        // Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
        if (mmDevice != null) {
            switch (v.getId()) {
             /*   case R.id.buttonSend:
                    Log.d(TAG, "button clicked");
                    EditText textBox = (EditText) findViewById(R.id.sendText);
                    String dataOut = textBox.getText().toString();
                    //  dataOut += "\n";
                    try {
                        mmOutputStream.write(dataOut.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Snackbar.make(v, "Data sent", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    break;*/


                case R.id.sendLeft:
                    Log.d(TAG, "left clicked");
                    String dataLeft = "1";
                    try {
                        mmOutputStream.write(dataLeft.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.sendRight:
                    Log.d(TAG, "right clicked");
                    String dataRight = "2";
                    try {
                        mmOutputStream.write(dataRight.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.sendDown:
                    Log.d(TAG, "down clicked");
                    String dataDown = "3";
                    try {
                        mmOutputStream.write(dataDown.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case R.id.deviceName:
                    selectDevice();
                    break;

            }
        }
        else if (v.getId() == R.id.deviceName) {
            Log.d(TAG,"Choose device called");
            selectDevice();
        }
        else {
            Snackbar.make(v, "Not connected with any device", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
       /* if (mmDevice != null)
            connectionFunction();*/
    }

    public void connectionFunction() {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        if (mmDevice != null) {
            try {
                Log.d(TAG, "in comm part");
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d(TAG, "socket created");
                mmSocket.connect();
                Log.d(TAG, "conected");
                mmOutputStream = mmSocket.getOutputStream();
                mmInputStream = mmSocket.getInputStream();
                Log.d(TAG, "Stream instantitated");

          /*  final Handler handler = new Handler();
            Thread workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                        try {
                            int bytesAvailable = mmInputStream.available();
                            byte delimiter = '\n';
                            if (bytesAvailable > 0) {
                                Log.d(TAG, "Bytes availabel: " + bytesAvailable);
                                byte[] packetBytes = new byte[bytesAvailable];
                                Log.d(TAG, "creating packet bytes");
                                try {
                                    mmInputStream.read(packetBytes);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                //     byte[] readBuffer = new byte[255];
                                //  String data = null;
                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        Log.d(TAG, "delimtter found " + count);
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        try {
                                            temp = new String(encodedBytes, "US-ASCII");
                                            if (temp != null && !temp.equals("") && !temp.equals("\n")) {
                                                data = temp;
                                                Log.d(TAG, "data: " + data);
                                            }
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        ++count;
                                        readBufferPosition = 0;
                                        readBuffer = new byte[255];
                                        if (data != null && !data.equals("") && !data.equals("\n")) {
                                            Log.d(TAG, "displaying..: " + data);
                                            Log.d(TAG, String.valueOf(data.length()));
                                            //rec = data;
                                            handler.post(new Runnable() {
                                                public void run() {
                                                    TextView textBox = (TextView) findViewById(R.id.receiveText);
                                                    Log.d(TAG, "displaying in thread..: " + data);

                                                    textBox.setText(data);
                                                    Toast.makeText(getApplicationContext(), "The data is: " + data, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } else {
                                        if (b != delimiter && b != 13) {
                                            Log.d(TAG, "Adding to buffer: " + b);
                                            readBuffer[readBufferPosition++] = b;
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            stopWorker = true;
                        }
                    }
                }
            });
            workerThread.start();*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Snackbar.make(view, "No device selected", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
