package com.pure.gothic.hackathon.idhackandroid.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.HashMap;

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();
    private SharedPreferences pref;
    private Editor editor;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "Idhack_Login";
    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_YOUR_ROLE = "role";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    // Set login status
    public void setLogin(boolean isLoggedIn, String userId, int role) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putString(KEY_USER_ID, userId);
        editor.putInt(KEY_YOUR_ROLE, role);
        // commit changes
        editor.apply();
        Log.d(TAG, "User login session modified!");
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }


    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();

        // user ID
        user.put(KEY_USER_ID, pref.getString(KEY_USER_ID, null));

        // return user
        return user;
    }

    public HashMap<String, Integer> getUserRole() {
        HashMap<String, Integer> user = new HashMap<>();

        // user ID
        user.put(KEY_YOUR_ROLE, pref.getInt(KEY_YOUR_ROLE, 0));

        // return user
        return user;
    }


}
