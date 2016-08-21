package com.pure.gothic.hackathon.idhackandroid.adapter;

import android.content.Context;
import android.graphics.Color;
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
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_message, parent, false);
        }
        singleMessageContainer = (LinearLayout) row.findViewById(R.id.singleMessageContainer);
        ChatData chatData = getItem(position);
        singleMessage = (TextView) row.findViewById(R.id.singleMessage);
        singleMessage.setText(chatData.getText());

        singleMessage.setBackground(chatData.getStatus() == ChatConfig.LEFT_BUBBLE ? ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.textview_left, null) : ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.textview_right, null));
        singleMessage.setTextColor(chatData.getStatus() == ChatConfig.LEFT_BUBBLE  ? Color.parseColor("#4c4c4c") : Color.parseColor("#4c4c4c"));

        singleMessageContainer.setGravity(chatData.getStatus() == ChatConfig.LEFT_BUBBLE ? Gravity.LEFT : Gravity.RIGHT);
        return row;
    }

}