package com.pure.gothic.hackathon.idhackandroid;


import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
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
import com.pure.gothic.hackathon.idhackandroid.chat.ChatArrayAdapter;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatConfig;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatData;
import com.pure.gothic.hackathon.idhackandroid.chat.RoleConfig;
import com.pure.gothic.hackathon.idhackandroid.network.CheckNetworkStatus;
import com.pure.gothic.hackathon.idhackandroid.network.NetworkConfig;
import com.pure.gothic.hackathon.idhackandroid.sms.SMSHelper;

import java.util.Arrays;

public class ChatActivityPatient extends Activity {
    private static final String TAG = ChatActivityPatient.class.getSimpleName();

    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend;
    private String receiver, sender, key;

    private CheckNetworkStatus checkNetworkStatus;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_patient);

        // FireBase
        if (!Firebase.getDefaultConfig().isPersistenceEnabled())
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
        Firebase.setAndroidContext(this);
        final Firebase ref = new Firebase("https://medichat-d5712.firebaseio.com/chatData");

        // Check networks status
        checkNetworkStatus = new CheckNetworkStatus(this);

        Intent i = getIntent();
        receiver = i.getStringExtra("receiver");
        sender = i.getStringExtra("sender");
        key = makeKey(sender,receiver);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        listView = (ListView) findViewById(R.id.listView);
        chatText = (EditText) findViewById(R.id.chatText);

        // ListView and adapter
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


        // Send to FireBase database
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String message = chatText.getText().toString();
                ChatData chatData = new ChatData(sender, receiver, message, key);
                ref.push().setValue(chatData);
                if(!NetworkConfig.IS_NETWORK_ON){
                    SMSHelper smsHelper = new SMSHelper(ChatActivityPatient.this);
                    smsHelper.sendMessageSMS("01076779064", message);
                }
                chatText.setText("");
            }
        });

        // FireBase query to retrieve chatData and event listener
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
        checkNetworkStatus.unRegister();
    }


    /**
     * Generate key from sender and receiver
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
