package com.pure.gothic.hackathon.idhackandroid.chat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pure.gothic.hackathon.idhackandroid.R;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatConfig;
import com.pure.gothic.hackathon.idhackandroid.chat.ChatData;
import com.pure.gothic.hackathon.idhackandroid.chat.RoleConfig;

import java.util.ArrayList;
import java.util.List;

public class ChatArrayAdapter extends ArrayAdapter<ChatData> {

    private TextView singleMessage;
    private List<ChatData> chatChatDataList = new ArrayList<>();
    private LinearLayout singleMessageContainer;

    @Override
    public void add(ChatData object) {

        chatChatDataList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.chatChatDataList.size();
    }

    public ChatData getItem(int index) {
        return this.chatChatDataList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatData chatData = getItem(position);
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (chatData.getStatus() == ChatConfig.LEFT_BUBBLE) {
            convertView = inflater.inflate(R.layout.item_message_left, parent, false);
        } else if (chatData.getStatus() == ChatConfig.RIGHT_BUBBLE) {
            convertView = inflater.inflate(R.layout.item_message_right, parent, false);
        }
        TextView label = (TextView) convertView.findViewById(R.id.label);
        TextView message = (TextView) convertView.findViewById(R.id.message);
        label.setText(chatData.getRole() == RoleConfig.ROLE_DOCTOR ? "Doctor" : "Patient");
        message.setText(chatData.getText());

        if(chatData.getStatus() == ChatConfig.RIGHT_BUBBLE){
            switch (chatData.getRole()) {
                case RoleConfig.ROLE_PATIENT: {
                    message.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.bg_message_patient, null));
                    break;
                }
                case RoleConfig.ROLE_DOCTOR: {
                    message.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.bg_message_doctor, null));
                    break;
                }
            }
        }


        return convertView;
    }

}