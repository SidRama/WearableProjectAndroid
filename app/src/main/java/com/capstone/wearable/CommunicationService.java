package com.capstone.wearable;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.UUID;

public class CommunicationService extends Service {
    private String TAG = "CommunicationService";
    public String STOP_ACTION = "android.wearable.STOP";
    //private static Boolean stopWorker = false;
    private int SERVICE_ID = 101;
    public static BluetoothDevice mmDevice = null;
    private static Boolean stopWorker = false;
    BluetoothSocket mmSocket;
    InputStream mmInputStream;
    String data, temp;
    int readBufferPosition = 0;
    int count = 0;
    byte[] readBuffer = new byte[255];   /* private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received broadcast...");

            String action = intent.getAction();
            if(action.equals(STOP_ACTION)){
                Log.d(TAG,"Stopping service");
                stopSelf();
            }

        }
    };*/

    public CommunicationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "reciever registered");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "In onStart.. ");
        Notification notification = null;
        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .addAction(R.drawable.ic_clear_black_24dp, "Close Connection", getPendingAction(this, 0, 1));
        notification = builder.build();
        startForeground(SERVICE_ID, notification);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        //  OutputStream mmOutputStream;
        // InputStream mmInputStream;

        //Just to test, delete later.....
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) ;
            }
        }).start();*/

        try {
            Log.d(TAG, "in comm part");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            Log.d(TAG, "socket created");
            mmSocket.connect();
            Log.d(TAG, "conected");
            mmInputStream = mmSocket.getInputStream();
            Log.d(TAG, "Stream instantitated");

            final Handler handler = new Handler();
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
            workerThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Service.START_STICKY;
    }

    public PendingIntent getPendingAction(Context context, int id, int code) {
        Intent intent = new Intent(context, MyReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("code", code);
        intent.setAction(STOP_ACTION);
        Random generator = new Random();
        Log.d(TAG, "Sending broadcast..");
        return PendingIntent.getBroadcast(context, generator.nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //     unregisterReceiver(receiver);
        Log.d(TAG, "Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        String phonenumber, password, latitude, longitude;
        Context context;

        UploadTask(String phonenumber, String password, String latitude, String longitude, Context context) {
            this.phonenumber = phonenumber;
            this.password = password;
            this.latitude = latitude;
            this.longitude = longitude;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            GetJSON getJson = new GetJSON();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phonenumber", phonenumber);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
            } catch (JSONException e) {

            }
            String response = getJson.getJSONFromUrl(Master.getEmergencyUrl(), jsonObject, "POST", true, phonenumber, password);
            Log.d(TAG, response);
            return null;
        }
    }

}
