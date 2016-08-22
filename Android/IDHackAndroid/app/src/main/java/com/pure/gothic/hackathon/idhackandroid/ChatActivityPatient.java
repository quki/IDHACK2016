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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pure.gothic.hackathon.idhackandroid.adapter.ChatArrayAdapter;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatConfig;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatData;
import com.pure.gothic.hackathon.idhackandroid.chat.RoleConfig;

import java.util.Arrays;

public class ChatActivityPatient extends Activity {
    private static final String TAG = ChatActivityPatient.class.getSimpleName();

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String receiver, sender, key;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    /*BroadcastReceiver myReceiver = new SMSBroadCast();*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_patient);
        Firebase.setAndroidContext(this);

        Intent i = getIntent();
        receiver = i.getStringExtra("receiver");
        sender = i.getStringExtra("sender");
        key = makeKey(sender,receiver);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        listView = (ListView) findViewById(R.id.listView);
        chatText = (EditText) findViewById(R.id.chatText);

        // ListView and adpter
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.item_message_left);
        listView.setAdapter(chatArrayAdapter);
        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });

        // EditText, enter chat message
        chatText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() != 0) {
                    buttonSend.setEnabled(true);
                } else {
                    buttonSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Send to Firebase database
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ChatData chatData = new ChatData(sender, receiver, chatText.getText().toString(), key);
                databaseReference.child("chatData").push().setValue(chatData);
                chatText.setText("");
            }
        });

        // Firebase query to retrieve chatData and event listener
        final Firebase ref = new Firebase("https://medichat-d5712.firebaseio.com/chatData");
        com.firebase.client.Query queryRef = ref.orderByChild("key").equalTo(key);
        queryRef.addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "key: " + dataSnapshot.getKey() + " value " + dataSnapshot.getValue());
                ChatData chatData = dataSnapshot.getValue(ChatData.class);

                // Set bubble position and role
                if(chatData.getSender().equals(sender)){
                    chatData.setStatus(ChatConfig.RIGHT_BUBBLE);
                    chatData.setRole(RoleConfig.ROLE_PATIENT);
                }else{
                    chatData.setStatus(ChatConfig.LEFT_BUBBLE);
                    chatData.setRole(RoleConfig.ROLE_DOCTOR);
                }
                chatArrayAdapter.add(chatData);
            }

            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestory()", "브로드캐스트리시버 해제됨");
    }

    private boolean sendChatMessage() {
        String msg = chatText.getText().toString();
        //insertByVolley(sender, receiver, msg, "0");
        chatText.setText("");
        //side = !side;
        if (receiver.length() > 0 && msg.length() > 0) {
            sendSMS(receiver, msg);
            Log.d("test1", "xml정보보냄 sendSMS()로");
        } else {
        }
        return true;
    }

    public void sendSMS(String smsNumber, String smsText) {
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
                        Log.d("test1", "전송성공");
                        //requestByVolley();
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


        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
    }

    /**
     * Generate key from sender and receiver
     * @param sender
     * @param receiver
     * @return
     */
    private String makeKey(String sender, String receiver) {

        String userArray[] = {sender, receiver};
        Arrays.sort(userArray);
        StringBuffer sb = new StringBuffer();
        for (String e : userArray) {
            sb.append(e);
        }
        String key = sb.toString();
        return key;
    }
}
