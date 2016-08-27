package com.pure.gothic.hackathon.idhackandroid.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pure.gothic.hackathon.idhackandroid.MainActivityPatient;
import com.pure.gothic.hackathon.idhackandroid.R;
import com.pure.gothic.hackathon.idhackandroid.dialog.ProgressDialogHelper;
import com.pure.gothic.hackathon.idhackandroid.network.AppController;
import com.pure.gothic.hackathon.idhackandroid.network.NetworkConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private ProgressDialogHelper mProgressDialogHelper;
    private int role;
    private final static String POST_TAG_REGISTER = "register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText inputName = (EditText) findViewById(R.id.name);
        final EditText inputPassword = (EditText) findViewById(R.id.password);
        final EditText inputPasswordCheck = (EditText) findViewById(R.id.password_check);
        final Button btnRegister = (Button) findViewById(R.id.btnRegister);
        final TextView btnLinkToLogin = (TextView) findViewById(R.id.btnLinkToLoginScreen);
        final LinearLayout rootView = (LinearLayout) findViewById(R.id.registerActivityView);
        final CheckBox askDoctor = (CheckBox) findViewById(R.id.askDoctor);
        final TextView helloDoctor = (TextView) findViewById(R.id.helloDoctor);
        askDoctor.setChecked(false);

        // Hide keyboard when user touch empty view
        rootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // Check box toggle if doctor or not
        askDoctor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    role = 1;
                    rootView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.doctor_bg_color));
                    btnRegister.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.doctor_bg_color));
                    helloDoctor.setVisibility(View.VISIBLE);
                } else {
                    role = 0;
                    rootView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.patient_bg_color));
                    btnRegister.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.patient_bg_color));
                    helloDoctor.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Progress dialog
        mProgressDialogHelper = new ProgressDialogHelper(this);

        // Session manager
        SessionManager mSessionManager = new SessionManager(getApplicationContext());

        // Session check user already login
        if (mSessionManager.isLoggedIn()) {

            Intent intent = new Intent(RegisterActivity.this, MainActivityPatient.class);
            startActivity(intent);
            finish();
        }

        // Register user account
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String password = inputPassword.getText().toString();
                String passwordCheck = inputPasswordCheck.getText().toString();

                if (name.trim().length() > 0 && password.trim().length() > 0 && passwordCheck.trim().length() > 0) {
                    // check password equal to password check filed
                    if (password.equals(passwordCheck)) {
                        registerUser(name, password);
                    } else {
                        Toast.makeText(RegisterActivity.this, "PASSWORD NO MATCH", Toast.LENGTH_LONG).show();
                        inputPassword.requestFocus();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "NO SPACE ALLOWED", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Launch LoginActivity
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


    /**
     * Insert user account to table name 'Account'
     * POST
     * @param userId user phone number
     * @param password user password
     */
    private void registerUser(final String userId, final String password) {

        mProgressDialogHelper.showPdialog("PLEASE WAIT...", false);

        StringRequest strReq = new StringRequest(Request.Method.POST, NetworkConfig.URL_ACCOUNT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        mProgressDialogHelper.hidePdialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            // Insert success
                            if (!error) {
                                // Launch login activity
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("userId", userId);
                                intent.putExtra("password", password);
                                intent.putExtra("role", role);
                                startActivity(intent);
                                finish();

                            // Insert fail
                            } else {
                                String errorMsg = jObj.getString("error_msg");
                                if (errorMsg.equals("User already existed")) {
                                    Toast.makeText(RegisterActivity.this,
                                            "ALREADY REGISTERED PHONE NUMBER", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this,
                                            errorMsg, Toast.LENGTH_LONG).show();
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                mProgressDialogHelper.hidePdialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("tag", POST_TAG_REGISTER);
                params.put("name", userId);
                params.put("password", password);
                params.put("role", role + "");
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, TAG);

    }
}
