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
    private DialogHelper mDialogHelper;
    private int role;

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
        askDoctor.setChecked(false);

        // 공백 클릭시 EditText의 focus와 자판이 사라지게 하기
        rootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // 의사 인지 여부 check box toggle
        askDoctor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    role = 1;
                    Toast.makeText(getApplicationContext(), role + "", Toast.LENGTH_SHORT).show();
                } else {
                    role = 0;
                    Toast.makeText(getApplicationContext(), role + "", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Progress dialog
        mDialogHelper = new DialogHelper(this);

        // Session manager
        SessionManager mSessionManager = new SessionManager(getApplicationContext());

        // 유저가 한번 로그인 했었는지 체크
        if (mSessionManager.isLoggedIn()) {

            // 유저가 이미 로그인 했었을 때...
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // 회원가입
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputName.getText().toString();
                String password = inputPassword.getText().toString();
                String passwordCheck = inputPasswordCheck.getText().toString();
                // 이름에 공백유무 확인
                if (name.trim().length() > 0 && password.trim().length() > 0 && passwordCheck.trim().length() > 0) {
                    // 비밀번호 일치여부 확인
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

        // Login 화면으로 전환
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


    /**
     * DB에 회원 정보 insert, POST 방식
     * @param name 회원이름(ID)
     * @param password 비밀번호
     */
    private void registerUser(final String name, final String password) {

        mDialogHelper.showPdialog("PLEASE WAIT...", false);

        StringRequest strReq = new StringRequest(Request.Method.POST, NetworkConfig.URL_ACCOUNT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        mDialogHelper.hidePdialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            // 회원 정보 DB insert success
                            if (!error) {
                                // Launch login activity
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("password", password);
                                intent.putExtra("role", role);
                                startActivity(intent);
                                finish();

                                Toast.makeText(RegisterActivity.this, "COMPLETE",
                                        Toast.LENGTH_LONG).show();

                            // 회원 정보 DB insert fail
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
                mDialogHelper.hidePdialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("tag", "register");
                params.put("name", name);
                params.put("password", password);
                params.put("role", role + "");
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, TAG);

    }
}
