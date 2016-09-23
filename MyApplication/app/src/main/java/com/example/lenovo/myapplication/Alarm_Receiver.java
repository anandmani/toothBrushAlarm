package com.example.lenovo.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Lenovo on 07-09-2016.
 */
public class Alarm_Receiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Enter the receiver","done");

      //  if(intent.getIntExtra("serviceId",-1)==1)

            //Create an intent to rintoneplayer
            Intent service_intent = new Intent(context, RingTonePlayer.class);
            Log.e("serviceId in AlarmRece",Integer.toString(intent.getIntExtra("serviceId",0)));

            service_intent.putExtra("serviceId",intent.getIntExtra("serviceId",0));//Do nothing if no default value
        //handle is ringtoneUri does not exist
        Log.e("ringtonePresent",""+intent.getIntExtra("ringtonePresent",0));
        Log.e("ringtoneUri",""+intent.getStringExtra("ringtoneUri"));

            service_intent.putExtra("ringtoneUri",intent.getStringExtra("ringtoneUri"));
            service_intent.putExtra("ringtonePresent",intent.getIntExtra("ringtonePresent",0));

            context.startService(service_intent);



    }
}
