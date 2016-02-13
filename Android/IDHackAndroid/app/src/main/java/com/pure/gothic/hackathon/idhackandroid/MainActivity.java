package com.pure.gothic.hackathon.idhackandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.pure.gothic.hackathon.idhackandroid.dialog.DialogHelper;
import com.pure.gothic.hackathon.idhackandroid.login.LoginActivity;
import com.pure.gothic.hackathon.idhackandroid.login.SessionManager;
import com.pure.gothic.hackathon.idhackandroid.volley.AppController;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

// jae, lemonhall2, CYhan, gon, quki
public class MainActivity extends AppCompatActivity {

    DialogHelper mDialogHelper;
    String yourId;
    private SessionManager session;
    private Toolbar mToolbar;
    private TextView mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mPhone = (TextView) findViewById(R.id.mPhone);
        setSupportActionBar(mToolbar);

        mDialogHelper = new DialogHelper(this);
        requestByVolley();

        Intent intent = getIntent();
        yourId = intent.getStringExtra("yourId");

        if (yourId != null) {
            setTitle(yourId);
        } else {
            Toast.makeText(this, "LOGIN ERROR", Toast.LENGTH_SHORT).show();
            logoutUser();
        }

        // Session Manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }
    }

    private void logoutUser() {
        session.setLogin(false, yourId, 0);
        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    // DB로 부터 response받고,JSON파싱 이후 adapter에 저장 (데이터 변화 감지)
    private void requestByVolley() {
        mDialogHelper.showPdialog("PLEASE WAIT...", true);

        StringRequest strReq = new StringRequest(Request.Method.POST, "REQUEST URL",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        mDialogHelper.hidePdialog();
                        try {
                            // String response -> JSON Array -> JSON Object 추출 -> 개별 항목 parsing
                            JSONArray jArray = new JSONArray(response);

                        } catch (JSONException e) {
                            Log.e("==MTAG==", "JSONException : " + e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("==MTAG==", "Error: " + error.getMessage());

                mDialogHelper.hidePdialog();


                Toast.makeText(getApplicationContext(), "CHECK YOUR NETWORK STATUS", Toast.LENGTH_SHORT).show();

            }
        }) {
            // POST방식으로 Parmaeter를 URL에 전달, 계정정보만 전달
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("number", "01076779064");
                params.put("message", "HELLO WORLD!");
                return params;
            }

            // UTF-8로 Encoding하는 작업
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                try {
                    String utf8String = new String(response.data, "UTF-8");
                    return Response.success(new String(utf8String), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                }
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.logOut) {

            // AlertDialog
            AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(
                    MainActivity.this);
            mAlertBuilder.setTitle("SIGN OUT")
                    .setMessage("REALLY?")
                    .setCancelable(false)
                    .setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //로그아웃
                            logoutUser();

                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                // 취소 버튼 클릭시 설정
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();

                }
            });

            AlertDialog dialog = mAlertBuilder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
