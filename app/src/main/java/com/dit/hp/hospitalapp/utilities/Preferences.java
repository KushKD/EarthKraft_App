package com.dit.hp.hospitalapp.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    // Singleton Instance
    private static Preferences instance;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    // Preference file name
    private final String preferenceName = "com.dit.hp.ekraft";

    // Keys
    private final String KEY_USER_ID = "userId";
    private final String KEY_USER_NAME = "userName";
    private final String KEY_MOBILE_NUMBER = "mobileNumber";
    private final String KEY_FIRST_NAME = "firstName";
    private final String KEY_LAST_NAME = "lastName";
    private final String KEY_ROLE_ID = "roleId";
    private final String KEY_ROLE_NAME = "roleName";

    private String KEY_IS_LOGED_IN = "isLoggedIn";

    // Instance Variables
    public int userId;
    public String userName = null;
    public Long mobileNumber = null;
    public String firstName = null;
    public String lastName = null;
    public int roleId;
    public String roleName = null;
    public boolean isLoggedIn;

    // Singleton Constructor
    private Preferences() {
    }

    // Singleton Instance
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            instance = new Preferences();
        }
        return instance;
    }

    // Load Preferences
    public void loadPreferences(Context context) {
        if (context == null) return;

        preferences = context.getSharedPreferences(preferenceName, Activity.MODE_PRIVATE);

        userId = preferences.getInt(KEY_USER_ID, -1);
        userName = preferences.getString(KEY_USER_NAME, null);

        long mobileNumValue = preferences.getLong(KEY_MOBILE_NUMBER, -1);
        mobileNumber = (mobileNumValue == -1) ? null : mobileNumValue;

        firstName = preferences.getString(KEY_FIRST_NAME, null);
        lastName = preferences.getString(KEY_LAST_NAME, null);

        roleId = preferences.getInt(KEY_ROLE_ID, -1);
        roleName = preferences.getString(KEY_ROLE_NAME, null);

        isLoggedIn = preferences.getBoolean(KEY_IS_LOGED_IN, isLoggedIn);

    }

    // Save Preferences
    public void savePreferences(Context context) {
        if (context == null) return;

        preferences = context.getSharedPreferences(preferenceName, Activity.MODE_PRIVATE);
        editor = preferences.edit();

        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);

        editor.putLong(KEY_MOBILE_NUMBER, mobileNumber == null ? -1L : mobileNumber);

        editor.putString(KEY_FIRST_NAME, firstName);
        editor.putString(KEY_LAST_NAME, lastName);

        editor.putInt(KEY_ROLE_ID, roleId);
        editor.putString(KEY_ROLE_NAME, roleName);

        editor.putBoolean(KEY_IS_LOGED_IN, isLoggedIn);

        editor.apply();
    }
}
