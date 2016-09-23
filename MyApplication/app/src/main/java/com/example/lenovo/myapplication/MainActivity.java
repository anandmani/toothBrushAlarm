package com.example.lenovo.myapplication;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //Variables
    AlarmManager alarmManager;
    TimePicker timePicker;
    TextView alarm_status,ringtoneText;
    ImageView imageAlarmOff;
    Context context;
    PendingIntent pendingIntent;
    Uri returnUri = null;
    Boolean turnAlarmOff= false;
    Intent intent;
    public int retry =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.context = this; //wtf?

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        final Calendar calendar = Calendar.getInstance();
        alarm_status = (TextView) findViewById(R.id.alarm_status);
        imageAlarmOff = (ImageView)findViewById(R.id.imageAlarmOff);

        intent = new Intent(this.context,Alarm_Receiver.class);

        Button alarm_on = (Button) findViewById(R.id.alarm_on);
       // alarm_on.setBackgroundColor(Color.BLACK);
        alarm_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageAlarmOff.setImageDrawable(null);
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE,timePicker.getCurrentMinute());

                int hour = timePicker.getCurrentHour();
                int minute = timePicker.getCurrentMinute();

                int remaining_hour = 0, remaining_minute = 0, current_hour = 0; //handles the case where alarm set time = current time
                String min;

                //calculating time remaining
                Calendar current = Calendar.getInstance();
//calendar.
                current_hour = current.get(Calendar.HOUR_OF_DAY);
                if(current_hour < hour){
                    int temp =(hour*60 + minute) - (current_hour*60 + current.get(Calendar.MINUTE));
                    remaining_hour = temp / 60;
                    remaining_minute = temp % 60;
                }
                if(current_hour > hour){
                    int temp = 1440  -  (current_hour*60 + current.get(Calendar.MINUTE)) + (hour*60 + minute);
                    remaining_hour = temp / 60;
                    remaining_minute = temp % 60;
                }
                if(current_hour == hour){
                    if(current.get(Calendar.MINUTE)<minute){
                        int temp =(hour*60 + minute) - (current_hour*60 + current.get(Calendar.MINUTE));
                        remaining_hour = temp / 60;
                        remaining_minute = temp % 60;
                    }
                    if(current.get(Calendar.MINUTE)>minute){
                        int temp = 1440  -  (current_hour*60 + current.get(Calendar.MINUTE)) + (hour*60 + minute);
                        remaining_hour = temp / 60;
                        remaining_minute = temp % 60;
                    }
                }

                if(minute<10) {
                   min = "0" + Integer.toString(minute); //setting 0 in 11:03
                }
                else{
                    min = Integer.toString(minute);
                }

                alarm_status.setText("Alarm Set for "+hour+":"+min);
                Toast.makeText(getApplicationContext(),"Time remaining: "+remaining_hour+" hours and "+remaining_minute+" minutes",Toast.LENGTH_SHORT).show();
                //Adding service id as an extra to the intent
                intent.putExtra("serviceId",1);
                if(returnUri!=null) {
                    intent.putExtra("ringtonePresent",1);
                    intent.putExtra("ringtoneUri", returnUri.toString());
                }
                else{
                    intent.putExtra("ringtonePresent",0);
                }
                long  timeOfWait = remaining_hour * 60 *60 * 1000 + remaining_minute*60*1000;
                Log.e("Time to Wait in Millis ",""+timeOfWait);
                pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
               // alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
                alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,  SystemClock.elapsedRealtime()+timeOfWait,pendingIntent);
                retry = 1;//Setting retry to initial Value
                Log.e("Alarm turned on","retry made "+retry+". Initial value of retry should be 1");

            }
        });



        Button alarm_off = (Button) findViewById(R.id.alarm_off);
        alarm_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("AlarmOffImage",""+imageAlarmOff.getDrawable());
                alarm_status.setText("No Alarm");
                //cancel the pending alarm
                alarmManager.cancel(pendingIntent);

                Intent custom_intent = new Intent(getApplicationContext(),Custom.class);
                startActivityForResult(custom_intent,1);
//                //Calling camera
//                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                    startActivityForResult(takePictureIntent, 1);//requestCode =1
//
//                    //Send intent only after camera image is satisfactory
//                    if(turnAlarmOff) {
////                        intent.putExtra("serviceId", 0);
////                        //stop ringing alarm
////                        sendBroadcast(intent);
////                        turnAlarmOff = false;
//                    }
//                }



            }
        });

        ImageButton ringtoneButton = (ImageButton)findViewById(R.id.ringtoneButton);
        ringtoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fetchToneIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(fetchToneIntent, 0);//requestCode = 0
            }
        });

        ringtoneText = (TextView)findViewById(R.id.ringtoneText);
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


    //----
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == 0) {//ringtone
                if (resultCode == RESULT_OK){
                    //Retrieve mp3 track, put it in textView and set it as alarm track
                    returnUri = data.getData();
                    Cursor returnCursor =
                        getContentResolver().query(returnUri, null, null, null, null);
                     int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                     returnCursor.moveToFirst();
                     ringtoneText.setText(returnCursor.getString(nameIndex));
                }
            }//end of ringtone

            if (requestCode == 1) {//camera image
                if(resultCode == 1 || retry == 3){
//                    turnAlarmOff = true;
                    intent.putExtra("serviceId", 0);
                    //stop ringing alarm
                    sendBroadcast(intent);
                    turnAlarmOff = false;
                    if(resultCode ==1) {
                        Toast.makeText(getApplicationContext(), "ToothBrush detected. Alarm off!", Toast.LENGTH_SHORT).show();
                    }
                    else {//retry ==3
                        Toast.makeText(getApplicationContext(), "Three failed attempts. Switching alarm off!", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(resultCode == 0){
                    Toast.makeText(getApplicationContext(),"ToothBrush Not detected. Please try again.",Toast.LENGTH_SHORT).show();
                    Log.e("Value of retry = ",""+retry);  //Initial Value of retry = 1 and we are incrementing retry after Log, because at the third retry attempt, alarm should stop.
                    retry++;
                }
                else{
                    Toast.makeText(getApplicationContext(),"Face not detected / Unable to process face. \nPlease try again",Toast.LENGTH_SHORT).show();
                    Log.e("Value of retry = ",""+retry);
                    retry++;
                }

            }//end of camera

    }
    //----
}
