package com.example.exidge.smsvoice;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by exidge on 2015-11-14.
 */

public class SmsReceiver extends BroadcastReceiver {
    public static ArrayList<String> toRead=new ArrayList<String>();
    @SuppressLint("NewApi")
    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle=intent.getExtras();
        SmsMessage sms[]=null;
        String str="";
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if(isScreenOn) {
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                sms = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    sms[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String numerNadawcy = sms[i].getOriginatingAddress();
                    String nadawca = getContactName(context, numerNadawcy);
                    Log.d("NadawcaSMS", numerNadawcy + " " + nadawca);
                    Locale curr = MainActivity.ttsLocale;
                    String toTTS = "";
                    if (nadawca != null && !nadawca.isEmpty() && !nadawca.equals("null")) {
                        //if(curr==Locale.forLanguageTag("pl-PL")) {
                        toTTS += "Nowa wiadomość od. " + nadawca + " ." + sms[i].getMessageBody();
                        //  }
                        // else {
                        toTTS += "New message from. " + nadawca + " ." + sms[i].getMessageBody();
                        // }
                    } else {
                        // if(curr==Locale.forLanguageTag("pl-PL")) {
                        toTTS += "Nowa wiadomość od. " + numerNadawcy.replace("", " ").trim() + " ." + sms[i].getMessageBody();
                        // }
                        // else {
                        toTTS += "New message from. " + numerNadawcy.replace("", " ").trim() + " ." + sms[i].getMessageBody();
                        // }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MainActivity.ttsOdsluch(toTTS);
                    Log.d("Debug", toTTS);
                }
            }
        }
        else {
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                sms = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    sms[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    String numerNadawcy = sms[i].getOriginatingAddress();
                    String nadawca = getContactName(context, numerNadawcy);
                    if(nadawca.matches("[0-9]+")){
                        nadawca.replace("", " ").trim();//zeby sam numer telefonu był poprawnie czytany
                    }
                    Log.d("NadawcaSMS", numerNadawcy + " " + nadawca);
                    Locale curr = MainActivity.ttsLocale;
                    String toTTS = "";
                    if (nadawca != null && !nadawca.isEmpty() && !nadawca.equals("null")) {
                        //if(curr==Locale.forLanguageTag("pl-PL")) {
                        toTTS += "Nowa wiadomość od. " + nadawca + " ." + sms[i].getMessageBody();
                        //  }
                        // else {
                        toTTS += "New message from. " + nadawca + " ." + sms[i].getMessageBody();
                        // }
                    } else {
                        // if(curr==Locale.forLanguageTag("pl-PL")) {
                        toTTS += "Nowa wiadomość od. " + numerNadawcy.replace("", " ").trim() + " ." + sms[i].getMessageBody();
                        // }
                        // else {
                        toTTS += "New message from. " + numerNadawcy.replace("", " ").trim() + " ." + sms[i].getMessageBody();
                        // }
                    }
                    toRead.add(toTTS);
                }
            }
        }
    }
    private static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }
}
