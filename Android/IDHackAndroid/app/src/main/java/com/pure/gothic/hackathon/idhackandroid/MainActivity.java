package com.pure.gothic.hackathon.idhackandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pure.gothic.hackathon.idhackandroid.login.LoginActivity;
import com.pure.gothic.hackathon.idhackandroid.login.SessionManager;

// jae, lemonhall2, CYhan, gon, quki
public class MainActivity extends AppCompatActivity {


    String yourId;
    private SessionManager session;
    private Toolbar mToolbar;
    private EditText otherPhone;
    private Button requestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        otherPhone = (EditText) findViewById(R.id.otherPhone);
        requestBtn = (Button) findViewById(R.id.requestBtn);
        requestBtn.setEnabled(false);
        setSupportActionBar(mToolbar);

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
        otherPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    requestBtn.setEnabled(true);
                }else{
                    requestBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ChatBubbleActivityForPatient.class);
                i.putExtra("send_num",otherPhone.getText().toString());
                i.putExtra("num",yourId);
                startActivity(i);
            }
        });
    }

    private void logoutUser() {
        session.setLogin(false, yourId, 0);
        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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
