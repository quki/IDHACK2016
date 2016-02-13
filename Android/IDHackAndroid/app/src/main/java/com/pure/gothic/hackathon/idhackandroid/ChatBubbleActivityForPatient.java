package com.pure.gothic.hackathon.idhackandroid;


import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.pure.gothic.hackathon.idhackandroid.dialog.DialogHelper;
import com.pure.gothic.hackathon.idhackandroid.volley.AppController;
import com.pure.gothic.hackathon.idhackandroid.volley.NetworkConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ChatBubbleActivityForPatient extends Activity {
    private static final String TAG = "ChatActivity";

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String sendNum,num;
    Intent intent;
    private boolean side = false;

    DialogHelper mDialogHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bubble_for_patient);
        mDialogHelper = new DialogHelper(this);

        intent = getIntent();
        sendNum = intent.getStringExtra("send_num");
        num = intent.getStringExtra("num");


        buttonSend = (Button) findViewById(R.id.buttonSend);

        listView = (ListView) findViewById(R.id.listView1);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_message);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.chatText);
        chatText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        requestByVolley();
    }

    private boolean sendChatMessage(){
        chatArrayAdapter.add(new ChatMessage("0", sendNum, chatText.getText().toString()));
        chatText.setText("");
        //side = !side;
        return true;
    }

    // DB로 부터 response받고,JSON파싱 이후 adapter에 저장 (데이터 변화 감지)
    private void requestByVolley() {
        mDialogHelper.showPdialog("PLEASE WAIT...", true);

        StringRequest strReq = new StringRequest(Request.Method.POST, NetworkConfig.URL_SELECT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        mDialogHelper.hidePdialog();
                        try {
                            // String response -> JSON Array -> JSON Object 추출 -> 개별 항목 parsing
                            JSONArray jArray = new JSONArray(response);
                            Log.e("^^^^^^^",jArray.toString());
                            for(int i = 0 ; i<jArray.length(); i++){
                                JSONObject jObj = jArray.getJSONObject(i);
                                ChatMessage chatMessage = new ChatMessage(jObj.getString("role"),jObj.getString("send_num"),jObj.getString("msg"));
                                chatArrayAdapter.add(chatMessage);
                            }

                        } catch (JSONException e) {
                            Log.e("==MTAG==", "JSONException : " + e.getMessage());
                        }

                        chatArrayAdapter.notifyDataSetChanged();

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
                Log.e("TESTESTES",num+sendNum);
                Map<String, String> params = new HashMap<String, String>();
                params.put("num", num);
                params.put("send_num", sendNum);
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
    //볼리 데이터값 받아오기
}
