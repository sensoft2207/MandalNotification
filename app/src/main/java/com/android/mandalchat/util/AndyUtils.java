package com.android.mandalchat.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sachin on 3/2/2017.
 */

public class AndyUtils {

    private static ProgressDialog pDialog;

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void displayDialog(Activity activity, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void showCustomProgressDialog(Context context) {

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait..");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public static void removeCustomProcessDialog(){
        pDialog.dismiss();
    }

    public static boolean eMailValidation(String emailstring) {
        if (null == emailstring || emailstring.length() == 0) {
            return false;
        }
        Pattern emailPattern = Pattern
                .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher emailMatcher = emailPattern.matcher(emailstring);
        return emailMatcher.matches();
    }

    public static boolean phoneValidation(String phone){
        if(null == phone || phone.length() == 0 ) {
            return false;
        }
        //return android.util.Patterns..PHONE.matcher(phone).matches();
        Pattern phonePattern = Patterns.PHONE;//compile("(\\+\\d+|0)(\\d{9})$");
        Matcher phoneMatcher = phonePattern.matcher(phone);
        return phoneMatcher.matches();
    }
    public static boolean isInputEditTextMatches(EditText textInputEditText1, EditText textInputEditText2, TextInputLayout textInputLayout, String message) {
        String value1 = textInputEditText1.getText().toString().trim();
        String value2 = textInputEditText2.getText().toString().trim();
        if (!value1.contentEquals(value2)) {
            textInputLayout.setError(message);

            return false;
        } else {
            textInputLayout.setErrorEnabled(false);
        }
        return true;
    }
    public static boolean nameValidation(String name){
        if(null == name || name.length() == 0 ) {
            return false;
        }
        Pattern namePattern = Pattern.compile("^[\\p{L} .'-]+$");
        Matcher nameMatcher = namePattern.matcher(name);
        return nameMatcher.matches();
    }

    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

}
