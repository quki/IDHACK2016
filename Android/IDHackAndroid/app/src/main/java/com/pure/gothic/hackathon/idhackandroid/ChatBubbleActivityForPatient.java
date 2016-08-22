package com.pure.gothic.hackathon.idhackandroid;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Message;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.pure.gothic.hackathon.idhackandroid.adapter.ChatArrayAdapter;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatConfig;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatData;
import com.pure.gothic.hackathon.idhackandroid.dialog.DialogHelper;

import java.util.Arrays;

public class ChatBubbleActivityForPatient extends Activity {
    private static final String TAG = ChatBubbleActivityForPatient.class.getSimpleName();

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String receiver, sender, key;
    private boolean side = false;

    DialogHelper mDialogHelper;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    /*BroadcastReceiver myReceiver = new SMSBroadCast();*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bubble_for_patient);
        Firebase.setAndroidContext(this);

        mDialogHelper = new DialogHelper(this);

        Intent i = getIntent();
        receiver = i.getStringExtra("receiver");
        sender = i.getStringExtra("sender");
        key = makeKey(sender,receiver);

        buttonSend = (Button) findViewById(R.id.buttonSend);

        listView = (ListView) findViewById(R.id.listView1);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.item_message);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.chatText);
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
                ChatData chatData = new ChatData(sender, receiver, chatText.getText().toString(), key);
                databaseReference.child("chatData").push().setValue(chatData);
                chatText.setText("");
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

        final Firebase ref = new Firebase("https://medichat-d5712.firebaseio.com/chatData");
        com.firebase.client.Query queryRef = ref.orderByChild("key").equalTo(key);
        queryRef.addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                Log.e("==onChildAdded==", "key: " + dataSnapshot.getKey() + " value " + dataSnapshot.getValue());
                ChatData chatData = dataSnapshot.getValue(ChatData.class);
                /*if (chatData.getReceiver().equals(receiver)) {
                    chatArrayAdapter.add(chatData);
                }*/
                if(chatData.getSender().equals(sender)){
                    chatData.setStatus(ChatConfig.RIGHT_BUBBLE);
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

        /*databaseReference.child("chatData").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatData chatData = dataSnapshot.getValue(ChatData.class);
                chatArrayAdapter.add(chatData);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
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
