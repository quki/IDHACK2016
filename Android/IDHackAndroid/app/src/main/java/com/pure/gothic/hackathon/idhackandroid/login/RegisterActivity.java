package com.pure.gothic.hackathon.idhackandroid.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.pure.gothic.hackathon.idhackandroid.MainActivity;
import com.pure.gothic.hackathon.idhackandroid.R;
import com.pure.gothic.hackathon.idhackandroid.dialog.DialogHelper;
import com.pure.gothic.hackathon.idhackandroid.volley.AppController;
import com.pure.gothic.hackathon.idhackandroid.volley.NetworkConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private Button btnRegister;
    private TextView btnLinkToLogin;

    private EditText inputEmail,inputPassword,inputPasswordCheck;
    private LinearLayout rootView;
    private DialogHelper mDialogHelper;

    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail = (EditText)findViewById(R.id.email);
        inputPassword = (EditText)findViewById(R.id.password);
        inputPasswordCheck = (EditText)findViewById(R.id.password_check);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnLinkToLogin = (TextView)findViewById(R.id.btnLinkToLoginScreen);
        rootView = (LinearLayout)findViewById(R.id.registerActivityView);

        // 공백을 클릭시 EditText의 focus와 자판이 사라지게 하기
        rootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // Progress dialog
        mDialogHelper = new DialogHelper(this);

        // Session manager
        mSessionManager = new SessionManager(getApplicationContext());

        // 유저가 한번 로그인 했었는지 체크
        if (mSessionManager.isLoggedIn()) {
            // 유저가 이미 로그인 했었을 때...
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                String passwordCheck = inputPasswordCheck.getText().toString();

                if (email.trim().length() > 0 && password.trim().length() > 0 && passwordCheck.trim().length()>0) {

                    if(password.equals(passwordCheck)){
                        registerUser(email, password);
                    }else{
                        Toast.makeText(RegisterActivity.this, "PASSWORD NO MATCH", Toast.LENGTH_LONG).show();
                        inputPassword.requestFocus();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "NO SPACE ALLOWED", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    // 서버에 회원 정보 insert
    private void registerUser(final String email, final String password) {

        String tag_string_req = "req_register";

        mDialogHelper.showPdialog("PLEASE WAIT...", false);

        StringRequest strReq = new StringRequest(Request.Method.POST, NetworkConfig.URL_REGISTER,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        mDialogHelper.hidePdialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            // User가 성공적으로 계정정보를 MySQL로 TABLE에 저장한 경우
                            if (!error) {
                                // Launch login activity
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("email",email );
                                intent.putExtra("password",password );
                                startActivity(intent);
                                finish();

                                Toast.makeText(RegisterActivity.this, "COMPLETE",
                                        Toast.LENGTH_LONG).show();

                                // 계정정보 TABLE에 저장 실패
                            } else{

                                String errorMsg = jObj.getString("error_msg");
                                if(errorMsg.equals("User already existed")){
                                    Toast.makeText(RegisterActivity.this,
                                            "ALREADY REGISTERED PHONE NUMBER", Toast.LENGTH_LONG).show();
                                }else{
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
                mDialogHelper.hidePdialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("name", "user");
                params.put("email", email);
                params.put("password", password);
                params.put("tag", "register");
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }
}
