package com.capstone.wearable;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {
    private AutoCompleteTextView mPhonenumberView;
    private AutoCompleteTextView mEmergencyNumberView;
    private EditText mNameView;
    private Master master;
    private final String TAG = "RegisterActivity";
    private GetJSON getJson;
    private String BLUETOOTH_ACTION = "com.capstone.wearable.BlutoothActivity";
    private UserRegisterTask mRegisterTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        master = new Master();
        mPhonenumberView = (AutoCompleteTextView) findViewById(R.id.new_phonenumber);
        mNameView = (EditText) findViewById(R.id.new_name);
        mEmergencyNumberView = (AutoCompleteTextView) findViewById(R.id.new_emergencynumber);
        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                registerUser();
            }
        });

    }

    private void registerUser() {
        Log.d(TAG, "Inside attempt register");
        if (mRegisterTask != null)
            return;
        mPhonenumberView.setError(null);
        mEmergencyNumberView.setError(null);
        mNameView.setError(null);
        String phonenumber = mPhonenumberView.getText().toString();
        String emergencyNumber = mEmergencyNumberView.getText().toString();
        String name = mNameView.getText().toString();
        View focusView = null;
        boolean cancel = false;

        if (name == null || name.length() == 0) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
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
        if (TextUtils.isEmpty(emergencyNumber)) {
            mEmergencyNumberView.setError(getString(R.string.error_field_required));
            focusView = mEmergencyNumberView;
            cancel = true;
        } else if (!isPhonenumberValid(phonenumber)) {
            mEmergencyNumberView.setError(getString(R.string.error_invalid_phonenumber));
            focusView = mEmergencyNumberView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            if (Master.isNetworkAvailable(Register.this)) {
                mRegisterTask = new UserRegisterTask(phonenumber, name, emergencyNumber, Register.this);
                mRegisterTask.execute((Void) null);
            }

        }
    }

    private boolean isPhonenumberValid(String phonenumber) {
        return phonenumber.length() == 10;
    }


    public class UserRegisterTask extends AsyncTask<Void, Void, String> {

        private String userPhoneNumber, name, emergencyNumber;
        Context context;

        UserRegisterTask(String userPhoneNumber, String name, String emergencyNumber, Context context) {
            this.userPhoneNumber = userPhoneNumber;
            this.name = name;
            this.emergencyNumber = emergencyNumber;
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            String response;
            Log.d(TAG, "Inside doInBack");
            getJson = new GetJSON();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("userPhonenumber", userPhoneNumber);
                jsonObject.put("userName", name);
                jsonObject.put("emergencyNumber", emergencyNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
            response = getJson.getJSONFromUrl(master.getRegisterUrl(), jsonObject, "POST", false, null, null);
            Log.d(TAG, response);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            JSONObject resp = null;
            try {
                resp = new JSONObject(response);
                if (resp.getString("response").equals("failed")) {
                    View view = findViewById(R.id.new_name);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    Snackbar.make(view, "Registeration Failed", Snackbar.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent();
                    intent.setAction(BLUETOOTH_ACTION);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
