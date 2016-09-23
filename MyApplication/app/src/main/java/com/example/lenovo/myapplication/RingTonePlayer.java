package com.example.lenovo.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Lenovo on 08-09-2016.
 */
public class RingTonePlayer extends Service {
    MediaPlayer media_song;
    boolean isRunning = false;
    NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        //what is this startId that is passed in the function ? Is it the number of times the onStartCommand is invoked?
        //So, we actually dont need to assign startId a val below, just directly use intent.getIntExtra("serviceId",0);
        Log.e("Inside1","onStart Ringtone startId : "+startId+" intent : "+intent);
        startId = intent.getIntExtra("serviceId",0);

        Log.e("Inside2","onStart Ringtone startId : "+startId+" intent : "+intent);

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent mainActivity_intent = new Intent(getApplicationContext(),MainActivity.class);
        PendingIntent pending_mainActivity_intent = PendingIntent.getActivity(this,0,mainActivity_intent,0);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.alert_icon)
                    .setContentTitle("AlarmApp")
                    .setContentText("Alarm is ringing. Click to open App")
                    .setContentIntent(pending_mainActivity_intent)
                    .setColor(0xff0000)//After loliipop, we can only set background color of icon in notifiation. Icon itself will be greyscale
                    .setAutoCancel(true)
                    .build();
        }

        Log.e("ringtonePresent",""+intent.getIntExtra("ringtonePresent",0));
        if(startId == 1 && !isRunning) {//And !isRunning because otherwise, it will trigger one more instance of music player!

            //==
            if(intent.getIntExtra("ringtonePresent",0)==1) {
                media_song = new MediaPlayer();
                Log.e("ringtoneUri Player", intent.getStringExtra("ringtoneUri"));
                Uri myUri = Uri.parse(intent.getStringExtra("ringtoneUri"));
                media_song.setAudioStreamType(AudioManager.STREAM_MUSIC);

                try {
                    media_song.setDataSource(getApplicationContext(), myUri);
                    media_song.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                media_song = MediaPlayer.create(this, R.raw.song);
            }
            //==
            media_song.setLooping(true);
            media_song.start();
            isRunning = true;
            Log.e("Sng strt, isRun = ",String.valueOf(media_song.isPlaying()));

            //Show notification
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                notificationManager.notify(1, notification);//1 is the notification id
            }
        }

        if(startId == 0 && isRunning ){ //media_song.isPlaying is initially null, if music is not paying. Hence isRunning is added. If music is not running, isRunning = flase, thus, && media_song.isPlaying() is not even evaluated in the logical expression, thus no null pointer error
            media_song.stop(); //removed isPlaying from if to handle short tones
            isRunning = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                notificationManager.cancel(1);// cancelling notification with notification id = 1
            }
        }

        //handle when music is short. therefore isPlaying is false. thus have to manually make isRunning false
        //ignore for now. Unfit alarm tone

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this,"On destroy Called",Toast.LENGTH_SHORT).show();
    }
}
