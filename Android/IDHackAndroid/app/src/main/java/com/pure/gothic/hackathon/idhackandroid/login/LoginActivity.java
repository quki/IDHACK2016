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
import com.pure.gothic.hackathon.idhackandroid.ChatBubbleActivityForDoctor;
import com.pure.gothic.hackathon.idhackandroid.MainActivity;
import com.pure.gothic.hackathon.idhackandroid.MainActivityDoctor;
import com.pure.gothic.hackathon.idhackandroid.R;
import com.pure.gothic.hackathon.idhackandroid.dialog.DialogHelper;
import com.pure.gothic.hackathon.idhackandroid.volley.AppController;
import com.pure.gothic.hackathon.idhackandroid.volley.NetworkConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // LogCat tag
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private TextView btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private DialogHelper mDialogHelper;
    private SessionManager mSessionManager;
    private LinearLayout rootView;
    private int role = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent i = new Intent(LoginActivity.this, ChatBubbleActivityForDoctor.class);
        startActivity(i);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (TextView) findViewById(R.id.btnLinkToRegisterScreen);
        rootView = (LinearLayout)findViewById(R.id.loginActivityView);

        // 공백을 클릭시 EditText의 focus와 자판이 사라지게 하기
        rootView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        });

        // RegisterActivity이후에 빈칸에 세팅
        Intent fromRegisterIntent = getIntent();
        String fromRegisterEmail = fromRegisterIntent.getStringExtra("email");
        String fromRegisterPwd = fromRegisterIntent.getStringExtra("password");
        role = fromRegisterIntent.getIntExtra("role",0);

        if(fromRegisterEmail !=null && fromRegisterPwd != null){
            inputEmail.setText(fromRegisterEmail);
            inputPassword.setText(fromRegisterPwd);
        }


        // Progress dialog
        mDialogHelper = new DialogHelper(this);

        // Session manager
        mSessionManager = new SessionManager(getApplicationContext());

        // 로그인 한지 안한지 체크
        if (mSessionManager.isLoggedIn()) {

            // get user data from session
            HashMap<String, String> user = mSessionManager.getUserDetails();
            HashMap<String, Integer> userRole = mSessionManager.getUserRole();
            String yourEmail  = user.get(SessionManager.KEY_YOUR_EMAIL);



            int roleFromSession = userRole.get(SessionManager.KEY_YOUR_ROLE);
            // 이미 로그인이 되어있으면 HomeActivity로
            if(roleFromSession == 0){
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("yourId",yourEmail);
                startActivity(intent);
                finish();
            }else if(roleFromSession == 1){
                Intent intent = new Intent(LoginActivity.this, MainActivityDoctor.class);
                intent.putExtra("yourId",yourEmail);
                startActivity(intent);
                finish();
            }

        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                // Check for empty data in the form
                if (email.trim().length() > 0 && password.trim().length() > 0) {
                    // login user
                    checkLogin(email, password);
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
    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {

        String tag_string_req = "req_login";

        mDialogHelper.showPdialog("PLEASE WAIT...", false);


        StringRequest strReq = new StringRequest(Request.Method.POST,
                NetworkConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                mDialogHelper.hidePdialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");


                    // 로그인 에러 체크
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        JSONObject jObjUser = jObj.getJSONObject("user");
                        String r = jObjUser.getString("role");
                        role = Integer.parseInt(r);
                        mSessionManager.setLogin(true, email, role);

                        if(role == 0){
                            // Launch main activity
                            Intent intent = new Intent(LoginActivity.this,
                                    MainActivity.class);
                            intent.putExtra("yourId", email);
                            startActivity(intent);
                            finish();
                        }else if(role == 1){
                            // Launch main activity
                            Intent intent = new Intent(LoginActivity.this,
                                    MainActivityDoctor.class);
                            intent.putExtra("yourId", email);
                            startActivity(intent);
                            finish();
                        }


                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(LoginActivity.this,"INCORRECT ACCOUNT"
                                , Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(LoginActivity.this,"NETWORK ERROR", Toast.LENGTH_SHORT).show();
                mDialogHelper.hidePdialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "login");
                params.put("email", email);
                params.put("password", password);
                params.put("role", role+"");

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
