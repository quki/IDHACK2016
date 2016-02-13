package com.pure.gothic.hackathon.idhackandroid;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.telephony.SmsManager;
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
import com.pure.gothic.hackathon.idhackandroid.volley.AppController;
import com.pure.gothic.hackathon.idhackandroid.volley.NetworkConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ChatBubbleActivityForDoctor extends Activity {
    private static final String TAG = "ChatActivity";
    Context mContext;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String sendNum;
    private String num;
    Intent intent;
    private boolean side = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bubble_for_doctor);


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

        String msg = chatText.getText().toString();
        insertByVolley(num, sendNum, msg, "1");

        /*chatArrayAdapter.add(new ChatMessage("true", sendNum, msg));*/
        chatText.setText("");

        if (sendNum.length()>0 && msg.length()>0){
            sendSMS(sendNum, msg);
            Log.d("test1", "xml정보보냄 sendSMS()로");
        }else{
        }
        //side = !side;
        return true;
    }
    public void sendSMS(String smsNumber, String smsText){
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("android.provider.Telephony.SMS_RECEIVED"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);
        Log.d("test1", "sendSMS()함수 내부작동");
        /**
         * SMS가 발송될때 실행
         * When the SMS massage has been sent
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        // 전송 성공
                       requestByVolley();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // 전송 실패
                        Log.d("test1", "전송실패");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // 서비스 지역 아님
                        Log.d("test1", "전송지역아님");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // 무선 꺼짐
                        Log.d("test1", "전송무선꺼짐");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU 실패
                        Log.d("test1", "pdu실패");
                        break;
                }
            }
        }, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        /**
         * SMS가 도착했을때 실행
         * When the SMS massage has been delivered
         */
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

               Log.e("PPPADSAF","asdfsadf");

            }
        }, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }

    // DB로 부터 response받고,JSON파싱 이후 adapter에 저장 (데이터 변화 감지)
    private void requestByVolley() {
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_message);
        listView.setAdapter(chatArrayAdapter);
        StringRequest strReq = new StringRequest(Request.Method.POST, NetworkConfig.URL_SELECT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            // String response -> JSON Array -> JSON Object 추출 -> 개별 항목 parsing
                            JSONArray jArray = new JSONArray(response);
                            Log.e("^^^^^^^",jArray.toString());
                            for(int i = 0 ; i<jArray.length(); i++){
                                JSONObject jObj = jArray.getJSONObject(i);
                                ChatMessage chatMessage;
                                if(jObj.getString("role").equals("0")){
                                    chatMessage = new ChatMessage("1",jObj.getString("send_num"),jObj.getString("msg"));
                                }else{
                                    chatMessage = new ChatMessage("0",jObj.getString("send_num"),jObj.getString("msg"));
                                }
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


                Toast.makeText(getApplicationContext(), "CHECK YOUR NETWORK STATUS", Toast.LENGTH_SHORT).show();

            }
        }) {
            // POST방식으로 Parmaeter를 URL에 전달, 계정정보만 전달
            @Override
            protected Map<String, String> getParams() {
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
    private void insertByVolley(final String num,final String sendNum,final String msg,final String role) {

        StringRequest strReq = new StringRequest(Request.Method.POST, NetworkConfig.URL_INSERT,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.e("^^insert^^", response);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("==MTAG==", "Error: " + error.getMessage());

                Toast.makeText(getApplicationContext(), "CHECK YOUR NETWORK STATUS", Toast.LENGTH_SHORT).show();

            }
        }) {
            // POST방식으로 Parmaeter를 URL에 전달, 계정정보만 전달
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("num", num);
                params.put("send_num", sendNum);
                params.put("msg", msg);
                params.put("role", role);
                chatArrayAdapter.add(new ChatMessage("0",sendNum,msg));
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


}
