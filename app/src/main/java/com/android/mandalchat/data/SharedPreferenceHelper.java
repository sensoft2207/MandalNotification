package com.android.mandalchat.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.mandalchat.model.User;



public class SharedPreferenceHelper {
    private static SharedPreferenceHelper instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static String SHARE_USER_INFO = "userinfo";
    private static String SHARE_KEY_NAME = "name";
    private static String SHARE_KEY_EMAIL = "email";
    private static String SHARE_KEY_TOKEN = "not_token";
    private static String SHARE_KEY_JOINING = "joining";
    private static String SHARE_KEY_PROFESSION = "profession";
    private static String SHARE_KEY_MOBILE = "mobile";
    private static String SHARE_KEY_AVATA = "avata";
    private static String SHARE_KEY_UID = "uid";


    private SharedPreferenceHelper() {}

    public static SharedPreferenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceHelper();
            preferences = context.getSharedPreferences(SHARE_USER_INFO, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }
        return instance;
    }

    public void saveUserInfo(User user) {
        editor.putString(SHARE_KEY_NAME, user.name);
        editor.putString(SHARE_KEY_EMAIL, user.email);
        editor.putString(SHARE_KEY_TOKEN, user.tokenn);
        editor.putString(SHARE_KEY_JOINING, user.joining);
        editor.putString(SHARE_KEY_PROFESSION, user.profession);
        editor.putString(SHARE_KEY_MOBILE, user.mobile);
        editor.putString(SHARE_KEY_AVATA, user.avata);
        editor.putString(SHARE_KEY_UID, StaticConfig.UID);
        editor.apply();
    }

    public User getUserInfo(){
        String userName = preferences.getString(SHARE_KEY_NAME, "");
        String email = preferences.getString(SHARE_KEY_EMAIL, "");
        String tokken = preferences.getString(SHARE_KEY_TOKEN, "");
        String joining = preferences.getString(SHARE_KEY_JOINING, "");
        String profession = preferences.getString(SHARE_KEY_PROFESSION, "");
        String mobile = preferences.getString(SHARE_KEY_MOBILE, "");
        String avatar = preferences.getString(SHARE_KEY_AVATA, "default");

        User user = new User();
        user.name = userName;
        user.email = email;
        user.tokenn = tokken;
        user.joining = joining;
        user.profession = profession;
        user.mobile = mobile;
        user.avata = avatar;

        return user;
    }

    public String getUID(){
        return preferences.getString(SHARE_KEY_UID, "");
    }

}
