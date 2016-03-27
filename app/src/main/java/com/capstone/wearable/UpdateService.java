/*
package com.capstone.wearable;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  //  SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    private Location mLastLocation;
    private String TAG = "UpdateService";
    public GoogleApiClient mGoogleApiClient;

    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder() ;
    }
    @Override
    public void onCreate() {
        Toast.makeText(this, "First Service was Created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("Stuff working.... ");
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(AppIndex.API)
                    .build();
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if(mLastLocation != null) {
                Log.d(TAG,"mLastLocation not null");
            }
            else {
                Log.d(TAG,"mLastLOcation is null");
            }
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    //    String phonenumber = prefs.getString("phonenumber","9999999999");
      //  String password = prefs.getString("password","password");
        String phonenumber=intent.getStringExtra("phonenumber");
        String password = intent.getStringExtra("password");
        String latitude= String.valueOf(mLastLocation.getLatitude());
        String longitude = String.valueOf(mLastLocation.getLongitude());
        UploadTask task = new UploadTask(phonenumber, password, latitude, longitude);
        task.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
    }

    public void onTaskRemoved (Intent rootIntent){

        LoginActivity.alarm.cancel(LoginActivity.pintent);
        this.stopSelf();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class UploadTask extends AsyncTask<Void, Void, Void> {
        String phonenumber, password, latitude, longitude;
        Context context;

        UploadTask(String phonenumber, String password, String latitude, String longitude) {
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
            String response = getJson.getJSONFromUrl(Master.getUploadUrl(), jsonObject, "POST", true, phonenumber, password);
            Log.d(TAG, response);
            return null;
        }
    }

}
*/
