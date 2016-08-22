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

/**
 * contribute
 * jae, lemonhall2, CYhan, gon, quki
 */
public class MainActivityPatient extends AppCompatActivity {

    private String userId;
    private SessionManager session;
    private EditText otherPhone;
    private Button requestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_patient);

        final Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        otherPhone = (EditText) findViewById(R.id.otherPhone);
        requestBtn = (Button) findViewById(R.id.requestBtn);
        requestBtn.setEnabled(false);
        setSupportActionBar(mToolbar);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        if (userId != null) {
            setTitle(userId);
        } else {
            Toast.makeText(this, "LOGIN ERROR", Toast.LENGTH_SHORT).show();
            signOut();
        }

        // Session Manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            signOut();
        }
        otherPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    requestBtn.setEnabled(true);
                } else {
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
                Intent i = new Intent(MainActivityPatient.this, ChatActivityPatient.class);
                i.putExtra("sender", userId);
                i.putExtra("receiver", otherPhone.getText().toString());
                startActivity(i);
            }
        });
    }

    private void signOut() {
        session.setLogin(false, userId, 0);
        // Launching the login activity
        Intent intent = new Intent(MainActivityPatient.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logOut) {

            // AlertDialog
            AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(
                    MainActivityPatient.this);
            mAlertBuilder.setTitle("SIGN OUT")
                    .setMessage("REALLY?")
                    .setCancelable(false)
                    .setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            signOut();

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
