package com.capstone.wearable;

import android.app.LoaderManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.UUID;

public class CommunicationService extends Service implements LoaderManager.LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "CommunicationService";
    public String STOP_ACTION = "android.wearable.STOP";
    //private static Boolean stopWorker = false;
    private int SERVICE_ID = 101;
   // public static BluetoothDevice mmDevice = null;
    private static Boolean stopWorker = false;
    BluetoothSocket mmSocket;
    InputStream mmInputStream;
    public GoogleApiClient mGoogleApiClient;
    String data, temp;
    int readBufferPosition = 0;
    int count = 0;
    private Location mLastLocation;
    private int REQUEST_CHECK_SETTINGS;

    String phonenumber, password;
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
        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API)
                    .build();
        }
        //Log.d(TAG, "reciever registered");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "In onStart.. ");
        mGoogleApiClient.connect();

        BluetoothDevice mmDevice = intent.getExtras().getParcelable("device");
        phonenumber = intent.getExtras().getString("phonenumber");
        password = intent.getExtras().getString("password");
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
        createLocationRequest();

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
                                                if(data.contains("5")) {
                                                    createLocationRequest();
                                                }
                                                Log.d(TAG, "data: " + data);
                                            }
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        ++count;
                                        readBufferPosition = 0;
                                        readBuffer = new byte[255];
                                   /*     if (data != null && !data.equals("") && !data.equals("\n")) {
                                            Log.d(TAG, "displaying..: " + data);
                                            Log.d(TAG, String.valueOf(data.length()));

                                        }*/
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
        mGoogleApiClient.disconnect();

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {

        final LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100000);
        mLocationRequest.setFastestInterval(50000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());


        Log.d(TAG, "creating request");

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                Log.d(TAG, "in on result");

                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        //...
                        Log.d(TAG, "in Sucess");

                        try {
                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            if (mLastLocation == null)
                                Log.d(TAG, "mLastLocation is null");
                            if (mGoogleApiClient == null)
                                Log.d(TAG, "mGoogleAPiCLient is null");
                            else
                                Log.d(TAG, "nothing is null");


                        } catch (SecurityException e) {
                            e.printStackTrace();
                            //   Snackbar.make(mLoginFormView, "Location Request Denied", Snackbar.LENGTH_SHORT).show();
                        }
                        if (mLastLocation != null) {
                            // double latitude = mLastLocation.getAltitude();
                            //Log.d(TAG,"LATITUDE: "+String.valueOf(latitude));
                            //   Snackbar.make(mLoginFormView, "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + ", " + "Longitude: " + String.valueOf(mLastLocation.getLongitude()), Snackbar.LENGTH_LONG).show();
                            Toast.makeText(getApplicationContext(), "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + ", " + "Longitude: " + String.valueOf(mLastLocation.getLongitude()), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + ", " + "Longitude: " + String.valueOf(mLastLocation.getLongitude()));
                            double latitude = mLastLocation.getLatitude();
                            double longitude = mLastLocation.getLongitude();
                         //   phonenumber = sharedPreferences.getString("phonenumber", "9143234566");
                          //  password = sharedPreferences.getString("password", "password");
                            UploadTask task = new UploadTask(phonenumber, password, String.valueOf(latitude), String.valueOf(longitude), getApplicationContext());
                            task.execute();
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        Log.d(TAG, "in on resolution required");
                        /*try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break*/
                        ;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        //...
                        Log.d(TAG, "in on no option");

                        break;
                }
            }
        });
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
