package com.example.exidge.smsvoice;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;
public class MainActivity extends AppCompatActivity{
    private EditText tw;
    private static TextToSpeech tts;
    private TextView lbPlayInfo;
    private Button btnPlayInfo;
    private TextView lbSettingsInfo;
    private Button btnSettingsInfo;
    private TextView tbStatus;
    public static Locale ttsLocale;
    public static boolean active;
    private Button btnActivate;
    private Button btnDeActivate;
    private NotificationManager mNotificationManager;

    public static void ttsOdsluch(String tekst){
        if(active){
            tts.speak(tekst, TextToSpeech.QUEUE_ADD, null);
        }
    }
    public void ButtonOdczytaj_OnClick(View paramView)
    {
        tts.speak(tw.getText().toString(), TextToSpeech.QUEUE_ADD, null);
    }
    public void showAll_OnClick(View paramView)
    {
        lbPlayInfo.setVisibility(View.VISIBLE);
        btnPlayInfo.setVisibility(View.VISIBLE);
        lbSettingsInfo.setVisibility(View.VISIBLE);
        btnSettingsInfo.setVisibility(View.VISIBLE);
    }
    public void ButtonPlay_OnClick(View paramView)
    {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.tts")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.tts")));
        }
    }
    public void ButtonSettings_OnClick(View paramView)
    {
        if (Build.VERSION.SDK_INT >= 14){
            Intent intent = new Intent();
            intent.setAction("com.android.settings.TTS_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void btnActivate_OnClick(View paramView)
    {
        active=true;
        btnActivate.setEnabled(false);
        btnDeActivate.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), (int)System.currentTimeMillis(), intent, 0);
        Notification n  = new Notification.Builder(this)
                .setContentTitle("SmsVoice aktywowany")   
                .setContentText("Czytam twoje SMS!")
                .setSmallIcon(R.drawable.ikonka2)
                .setContentIntent(pIntent).build();
                // .addAction(R.drawable.ikonka,"Deaktywuj",pIntent).build();
                //.setAutoCancel(true).build();
                //.addAction(R.drawable.icon, "Call", pIntent)
                //.addAction(R.drawable.icon, "More", pIntent)
                //.addAction(R.drawable.icon, "And more", pIntent).build();
        n.flags|=Notification.FLAG_NO_CLEAR;
        mNotificationManager.notify(0, n);
    }
    public void btnDeActivate_OnClick(View paramView)
    {
        active=false;
        btnActivate.setEnabled(true);
        btnDeActivate.setEnabled(false);
        mNotificationManager.cancelAll();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!SmsReceiver.toRead.isEmpty()&&active){
            for(int i=0;i<SmsReceiver.toRead.size();i++){
                ttsOdsluch(SmsReceiver.toRead.get(i));
                while(tts.isSpeaking()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            SmsReceiver.toRead.clear();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        active=false;
        lbPlayInfo=(TextView)findViewById(R.id.lbPlayInfo);
        lbPlayInfo.setVisibility(View.INVISIBLE);
        btnPlayInfo=(Button)findViewById(R.id.btnPlayInfo);
        btnPlayInfo.setVisibility(View.INVISIBLE);
        lbSettingsInfo=(TextView)findViewById(R.id.lbSettingsInfo);
        lbSettingsInfo.setVisibility(View.INVISIBLE);
        btnSettingsInfo=(Button)findViewById(R.id.btnSettingsInfo);
        btnSettingsInfo.setVisibility(View.INVISIBLE);
        tbStatus=(TextView)findViewById(R.id.tbStatus);
        tw = ((EditText)findViewById(R.id.textBox));
        btnActivate=(Button)findViewById(R.id.btnAktywuj);
        btnDeActivate=(Button)findViewById(R.id.btnDeaktywuj);
        tts=new TextToSpeech(this,new TextToSpeech.OnInitListener() {
            @SuppressLint("NewApi")
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS) {
                    if(tts.isLanguageAvailable(Locale.forLanguageTag("pl-PL"))==TextToSpeech.LANG_NOT_SUPPORTED){
                    //if(tts.isLanguageAvailable(Locale.ENGLISH)==TextToSpeech.LANG_NOT_SUPPORTED){
                        tts.setLanguage(Locale.getDefault());
                        tbStatus.setText("Polski język nie obsługiwany. Ustawony język: " + Locale.getDefault().getLanguage());
                        ttsLocale=Locale.getDefault();
                        tbStatus.setTextColor(Color.RED);
                        lbSettingsInfo.setVisibility(View.VISIBLE);
                        btnSettingsInfo.setVisibility(View.VISIBLE);
                    }
                    else if(tts.isLanguageAvailable(Locale.forLanguageTag("pl-PL"))==TextToSpeech.LANG_MISSING_DATA) {
                        tts.setLanguage(Locale.getDefault());
                        tbStatus.setText("Pliki języka Polskiego nie znalezione. Próba ustawienia: " + Locale.getDefault().getLanguage());
                        ttsLocale=Locale.getDefault();
                        tbStatus.setTextColor(Color.RED);
                        lbSettingsInfo.setVisibility(View.VISIBLE);
                        btnSettingsInfo.setVisibility(View.VISIBLE);
                    }
                    else {
                        tts.setLanguage(Locale.forLanguageTag("pl-PL"));
                        ttsLocale=Locale.forLanguageTag("pl-PL");
                        tbStatus.setText("Wszystko ustawione poprawnie");
                        tbStatus.setTextColor(Color.GREEN);
                        Locale.setDefault(Locale.forLanguageTag("pl-PL"));
                    }
                }
                else if(status==TextToSpeech.ERROR_NOT_INSTALLED_YET) {
                    tbStatus.setText("Pliki silnika TextToSpeech nie są zainstalowane.");
                    tbStatus.setTextColor(Color.RED);
                    lbSettingsInfo.setVisibility(View.VISIBLE);
                    btnSettingsInfo.setVisibility(View.VISIBLE);
                    lbPlayInfo.setVisibility(View.VISIBLE);
                    btnPlayInfo.setVisibility(View.VISIBLE);
                }
                else if(status==TextToSpeech.ERROR_NETWORK) {
                    tbStatus.setText("Błąd sieci. Sprawdz czy istnieje połączenie.");
                    tbStatus.setTextColor(Color.RED);
                }
                else {
                    tbStatus.setText("Ogólny błąd. Sprawdz czy wszystko jest poprawnie skonfigurowane.");
                    tbStatus.setTextColor(Color.RED);
                    lbSettingsInfo.setVisibility(View.VISIBLE);
                    btnSettingsInfo.setVisibility(View.VISIBLE);
                    lbPlayInfo.setVisibility(View.VISIBLE);
                    btnPlayInfo.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
