package com.pure.gothic.hackathon.idhackandroid.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


public class SMSHelper {

    private Context context;
    private String TAG = SMSHelper.class.getSimpleName();
    public SMSHelper(Context context) {
        this.context = context;
    }

    /**
     * Pop up AlertDialog
     * when network status OFF !
     */
    public void sendMessageSMS(final String phoneNum, final String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("NETWORK STATUS OFF");
        dialog.setMessage("Would you like to send a SMS text message?\n" + message);
        dialog.setPositiveButton("SEND SMS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                registerSMS(phoneNum, message);
                dialog.cancel();
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    /**
     * Register and send SMS to phoneNum
     */
    public void registerSMS(String phoneNum, String message) {
        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);

        // Register SMS BroadcastReceiver
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        // SUCCESS
                        Toast.makeText(context.getApplicationContext(), "SEND COMPLETE", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "SEND COMPLETE");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        // SEND FAILURE
                        Toast.makeText(context.getApplicationContext(), "SEND FAILURE", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "RESULT_ERROR_GENERIC_FAILURE");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        // NO SERVICE AREA
                        Toast.makeText(context.getApplicationContext(), "SEND FAILURE", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "RESULT_ERROR_NO_SERVICE");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        // RADIO OFF
                        Toast.makeText(context.getApplicationContext(), "SEND FAILURE", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "RESULT_ERROR_RADIO_OFF");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        // PDU FAILURE
                        Toast.makeText(context.getApplicationContext(), "SEND FAILURE", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "RESULT_ERROR_NULL_PDU");
                        break;
                }
            }
        }, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage(phoneNum, null, message, sentIntent, deliveredIntent);
    }
}
