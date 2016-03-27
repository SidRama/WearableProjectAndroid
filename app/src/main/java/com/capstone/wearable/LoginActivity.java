package com.capstone.wearable;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GetJSON getJSON;
    String response;
    Master master;
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    ProgressDialog progressDialog;
    private Location mLastLocation;
    private int REQUEST_CHECK_SETTINGS;
    public static AlarmManager alarm;
    public static PendingIntent pintent;
    private int LOCATION_PERMISSION = 111;
    private int SET_PERMISSION = 2;
    private int BLUETOOTH_PERMISSION = 3;
    private int INTERNET_PERMISSION = 4;
    private int NETWORK_STATE_PERMISSION = 5, locationFlag=0;
    String BLUETOOTH_ACTION = "com.capstone.wearable.BlutoothActivity";

    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mPhonenumberView;
    private Button registerLink;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String phonenumber, password;
    public GoogleApiClient mGoogleApiClient;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    protected ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        master = new Master();
        locationFlag =0;
        mPhonenumberView = (AutoCompleteTextView) findViewById(R.id.phonenumber);
        populateAutoComplete();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        mPasswordView = (EditText) findViewById(R.id.password);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Intent intent = new Intent();
                intent.setAction(BLUETOOTH_ACTION);
                startActivity(intent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        registerLink = (Button) findViewById((R.id.link_register_button));
        registerLink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API)
                    .build();
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Bluetooth Permission request required");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH},
                    BLUETOOTH_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Internt Permission request required");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    INTERNET_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Network Permission request required");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    NETWORK_STATE_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location Permission request required");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }
        else {
            locationFlag =1;
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.capstone.wearable/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Login Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.capstone.wearable/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
      /*  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mPhonenumberView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }*/
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        } else if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createLocationRequest();
            }
        }
    }

    private void attemptLogin() {
        Log.d(TAG, "Inside attempt login");
        if (mAuthTask != null) {
            return;
        }
        Log.d(TAG, "Still in");
        mPhonenumberView.setError(null);
        mPasswordView.setError(null);

        String phonenumber = mPhonenumberView.getText().toString();
        String password = mPasswordView.getText().toString();
        editor.putString("phonenumber", phonenumber);
        editor.putString("password", password);
        editor.commit();
        Log.d(TAG, "ph number and pword: " + phonenumber + " " + password);
        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(phonenumber)) {
            mPhonenumberView.setError(getString(R.string.error_field_required));
            focusView = mPhonenumberView;
            cancel = true;
        } else if (!isPhonenumberValid(phonenumber)) {
            mPhonenumberView.setError(getString(R.string.error_invalid_phonenumber));
            focusView = mPhonenumberView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {

            if (Master.isNetworkAvailable(LoginActivity.this)) {

                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();
                mAuthTask = new UserLoginTask(phonenumber, password, LoginActivity.this);
                mAuthTask.execute((Void) null);
            }
        }
    }

    private boolean isPhonenumberValid(String phonenumber) {
        //TODO: Replace this with your own logic
        return phonenumber.length() == 10;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only phone numbers.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Phone
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> phone = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            phone.add(cursor.getString(ProfileQuery.NUMBER));
            cursor.moveToNext();
        }

        addPhoneToAutoComplete(phone);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addPhoneToAutoComplete(List<String> phonenumbersCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, phonenumbersCollection);

        mPhonenumberView.setAdapter(adapter);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
      /* try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
           if(mLastLocation != null) {
               Log.d(TAG,"mLastLocation not null");
           }
        }
        catch(SecurityException e) {
            e.printStackTrace();
            Snackbar.make(mLoginFormView,"Location Request Denied",Snackbar.LENGTH_SHORT).show();
        }
        if (mLastLocation != null) {
            Snackbar.make(mLoginFormView,"Latitude: "+String.valueOf(mLastLocation.getLatitude())+", "+"Longitude: "+String.valueOf(mLastLocation.getLongitude()),Snackbar.LENGTH_LONG).show();
        }*/

      /*  if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission request required");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
          //  requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);


        } else {

            createLocationRequest();
            Log.d(TAG,"Has permission");

        }*/
       if(locationFlag == 1){
           createLocationRequest();
       }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
        };

        int NUMBER = 0;
        int IS_PRIMARY = 1;
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
                            Snackbar.make(mLoginFormView, "Location Request Denied", Snackbar.LENGTH_SHORT).show();
                        }
                        if (mLastLocation != null) {
                            // double latitude = mLastLocation.getAltitude();
                            //Log.d(TAG,"LATITUDE: "+String.valueOf(latitude));
                            Snackbar.make(mLoginFormView, "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + ", " + "Longitude: " + String.valueOf(mLastLocation.getLongitude()), Snackbar.LENGTH_LONG).show();
                            Log.d(TAG, "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + ", " + "Longitude: " + String.valueOf(mLastLocation.getLongitude()));
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        Log.d(TAG, "in on resolution required");
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    LoginActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mPhonenumber;
        private final String mPassword;
        private final Context context;

        UserLoginTask(String phonenumber, String password, Context context) {
            mPhonenumber = phonenumber;
            mPassword = password;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {

            getJSON = new GetJSON();
            Log.d(TAG, "inside Async");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phonenumber", mPhonenumber);
                jsonObject.put("password", mPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
            response = getJSON.getJSONFromUrl(master.getLoginUrl(), jsonObject, "POST", false, null, null);
            Log.d(TAG, response);
            return response;


        }

        @Override
        protected void onPostExecute(String response) {
            mAuthTask = null;
            //showProgress(false);
            progressDialog.dismiss();
            JSONObject resp = null;
            try {
                resp = new JSONObject(response);

                if (resp.getString("response").equals("Invalid Credentials") || resp.getString("response").equals("Login failed")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show();

                } else {
            /*if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }*/


                   /* Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.SECOND, 10);

                    Intent alarmIntent = new Intent(context, UpdateService.class);
                    alarmIntent.putExtra("phonenumber",mPhonenumber);
                    alarmIntent.putExtra("password", mPassword);
                    pintent = PendingIntent.getService(context, 0, alarmIntent, 0);
                    alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 5000, pintent);*/

                    Intent intentComm = new Intent(context, CommunicationService.class);
                    intentComm.putExtra("phonenumber", mPhonenumber);
                    intentComm.putExtra("password", mPassword);
                    startService(intentComm);
                    Intent intent = new Intent();
                    intent.setAction(BLUETOOTH_ACTION);
                    // Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show();


                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            //  showProgress(false);
            progressDialog.dismiss();
        }
    }
}

