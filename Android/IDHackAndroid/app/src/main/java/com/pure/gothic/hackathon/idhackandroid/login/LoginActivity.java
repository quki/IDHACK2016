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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.pure.gothic.hackathon.idhackandroid.MainActivityDoctor;
import com.pure.gothic.hackathon.idhackandroid.MainActivityPatient;
import com.pure.gothic.hackathon.idhackandroid.R;
import com.pure.gothic.hackathon.idhackandroid.dialog.MyProgressDialog;
import com.pure.gothic.hackathon.idhackandroid.network.AppController;
import com.pure.gothic.hackathon.idhackandroid.network.NetworkConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private MyProgressDialog mMyProgressDialog;
    private SessionManager mSessionManager;
    private int role = 999; //default
    private final static String POST_TAG_LOGIN = "login";
    private LinearLayout rootView;
    private EditText inputName;
    private EditText inputPassword;
    private Button btnLogin;
    private TextView btnLinkToRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputName = (EditText) findViewById(R.id.name);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (TextView) findViewById(R.id.btnLinkToRegisterScreen);
        rootView = (LinearLayout)findViewById(R.id.loginActivityView);

        // Hide keyboard when user touch empty view
        rootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // Progress dialog
        mMyProgressDialog = new MyProgressDialog(this);

        // Session manager
        mSessionManager = new SessionManager(getApplicationContext());

        // Session check user already login
        if (mSessionManager.isLoggedIn()) {

            // get user data from session
            HashMap<String, String> user = mSessionManager.getUserDetails();
            HashMap<String, Integer> userRole = mSessionManager.getUserRole();
            String userId  = user.get(SessionManager.KEY_USER_ID);

            int roleFromSession = userRole.get(SessionManager.KEY_YOUR_ROLE);
            // If already login, launch HomeActivity
            if(roleFromSession == 0){
                Intent intent = new Intent(LoginActivity.this, MainActivityPatient.class);
                intent.putExtra("userId",userId);
                startActivity(intent);
                finish();
            }else if(roleFromSession == 1){
                Intent intent = new Intent(LoginActivity.this, MainActivityDoctor.class);
                intent.putExtra("userId",userId);
                startActivity(intent);
                finish();
            }

        }

        // Login
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String name = inputName.getText().toString();
                String password = inputPassword.getText().toString();

                if (name.trim().length() > 0 && password.trim().length() > 0) {
                    // login user
                    checkLogin(name, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(LoginActivity.this,
                            "NO SPACE ALLOWED", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If user account register success fill account to blank by auto from RegisterActivity
        Intent fromRegisterIntent = getIntent();
        String fromRegisterID = fromRegisterIntent.getStringExtra("userId");
        String fromRegisterPwd = fromRegisterIntent.getStringExtra("password");
        role = fromRegisterIntent.getIntExtra("role",999);

        if(fromRegisterID !=null && fromRegisterPwd != null){
            inputName.setText(fromRegisterID);
            inputPassword.setText(fromRegisterPwd);
        }
        if(role == 0){
            rootView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.patient_bg_color));
            btnLogin.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.patient_bg_color));
        }else if(role == 1){
            rootView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.doctor_bg_color));
            btnLogin.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.doctor_bg_color));
        }
    }

    /**
     * Check account with table 'Account'
     * POST
     * @param userId
     * @param password
     */
    private void checkLogin(final String userId, final String password) {

        mMyProgressDialog.showPdialog("PLEASE WAIT...", false);

        StringRequest strReq = new StringRequest(Request.Method.POST,
                NetworkConfig.URL_ACCOUNT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                mMyProgressDialog.hidePdialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    if (!error) {
                        // User successfully logged in
                        // Create login session
                        String r = jObj.getString("role");
                        role = Integer.parseInt(r);
                        mSessionManager.setLogin(true, userId, role);

                        if(role == 0){
                            // Launch MainActivityPatient
                            Intent intent = new Intent(LoginActivity.this,
                                    MainActivityPatient.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        }else if(role == 1){
                            // Launch MainActivityPatient(Doctor)
                            Intent intent = new Intent(LoginActivity.this,
                                    MainActivityDoctor.class);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(LoginActivity.this,"INCORRECT ACCOUNT"
                                , Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(LoginActivity.this,"NETWORK ERROR", Toast.LENGTH_SHORT).show();
                mMyProgressDialog.hidePdialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", POST_TAG_LOGIN);
                params.put("name", userId);
                params.put("password", password);
                params.put("role", role+"");

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, TAG);
    }
}
