package com.pure.gothic.hackathon.idhackandroid.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by quki on 2016-02-13.
 */
public class CheckNetworkStatus {
    private Context context;
    private BroadcastReceiver broadcastReceiver;
    private String TAG = CheckNetworkStatus.class.getSimpleName();
    public CheckNetworkStatus(Context context) {
        this.context = context;
        register();
    }

    public void register() {

        if (broadcastReceiver == null) {

            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    Bundle extras = intent.getExtras();
                    NetworkInfo info = extras.getParcelable("networkInfo");
                    NetworkInfo.State state = info.getState();

                    if(state == NetworkInfo.State.CONNECTED) {
                        NetworkConfig.IS_NETWORK_ON = true;
                    } else {
                        NetworkConfig.IS_NETWORK_ON = false;
                    }

                }
            };

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    public void unRegister() {
        if (broadcastReceiver != null){
            context.unregisterReceiver(broadcastReceiver);
            Log.d(TAG, "Unregistered Network BroadCastReceiver");
        }

    }
}
