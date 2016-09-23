package com.example.lenovo.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.io.FileNotFoundException;
import java.net.URL;

public class Custom extends AppCompatActivity {
Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        //  InputStream stream = getResources().openRawResource(R.raw.amma2);
        //  Bitmap bitmap = BitmapFactory.decodeStream(stream);
        Intent intent = getIntent();

        //Calling camera
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, 2);//requestCode =1
//
//        }
       ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
         imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(takePictureIntent, 2);

    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap imageBitmap = null;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if(requestCode == 2){

                try {
                      imageBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
//                    imgView.setImageBitmap(thumbnail);
                    String  imageurl = getRealPathFromURI(imageUri);
                    Log.e("Real path for pic ",imageurl);
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                Bundle extras = data.getExtras();
//                Bitmap imageBitmap = (Bitmap) extras.get("data");

                FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .build();

                // Create a frame from the bitmap and run face detection on the frame.
                Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
                SparseArray<Face> faces = detector.detect(frame);


                int numberOfFaces = faces.size();
                Log.e("Value of faces:",""+numberOfFaces);

                if(numberOfFaces == 0){//No faces detected
                    Log.e("inside no face","gg");
                    Intent intent1 = new Intent();
                    setResult(3,intent1);
                    finish();
                    return;
                }

                Log.e("progress continues","");
                if (!detector.isOperational()) {
                    Toast.makeText(this, "Face detector dependencies are not yet available.", Toast.LENGTH_SHORT).show();
                }

                CustomView overlay = (CustomView) findViewById(R.id.customView);
                overlay.setContent(imageBitmap, faces);
                detector.release();


                //----doing calculations here-----

                Paint paint = new Paint();
                int rmx =0, rmy=0, lmx=0, lmy=0, rcx=0, rcy=0, lcx=0, lcy=0;
                int rect_width=75, rectangle_height=75;  //calculate appropriate value
                int dummyx=0,dummyy=0;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5);
                int rcp[]=new int[50000],lcp[]=new int[50000],rmp[]=new int[50000],lmp[]=new int[50000];
                double rc_avg_red = 0, rc_avg_green=0, rc_avg_blue=0;
                double lc_avg_red = 0, lc_avg_green=0, lc_avg_blue=0;
                double rm_avg_red = 0, rm_avg_green=0, rm_avg_blue=0;
                double lm_avg_red = 0, lm_avg_green=0, lm_avg_blue=0;


                int y=0;
                for (int i = 0; i < faces.size(); ++i) {
                    // y = 3 is  right cheek int rc x,y  [my right. not image's face's right]
                    //y = 4 is  left cheek  int lc x,y
                    //y = 5 is  right mouth-end  int rm x,y
                    // y =6 is  left mouth-end  int lm x,y
                    Face face = faces.valueAt(i);
                    rect_width = (int)face.getWidth()/16;
                    rectangle_height = (int)face.getHeight()/16;
                    Log.e("face width",""+face.getWidth());
                    Log.e("face height",""+face.getHeight());
                    for (Landmark landmark : face.getLandmarks()) {



                        if(y==3){
                            paint.setColor(Color.BLUE);
                            rcx = (int) (landmark.getPosition().x);
                            rcy =(int) (landmark.getPosition().y);
                        }
                        if(y==4){
                            paint.setColor(Color.BLUE);
                            lcx =(int) (landmark.getPosition().x);
                            lcy =(int) (landmark.getPosition().y);
                        }
                        if(y==5) {
                            paint.setColor(Color.GREEN);
                            rmx = (int) (landmark.getPosition().x);
                            rmy =(int) (landmark.getPosition().y);
                        }
                        if(y==6) {
                            paint.setColor(Color.GREEN);
                            lmx=(int) (landmark.getPosition().x);
                            lmy=(int) (landmark.getPosition().y);
                        }

                        y++;
                    }//end of each landmark
                    if(y<8){ //All landmarks have not been found
                        Log.e("Total landmarks found:",""+y);
                        Intent intent1 = new Intent();
                        setResult(3,intent1);
                        finish();
                        return;
                    }
                }//end of each face

                //Choose the lower of the two mouth ends
                if(rmy < lmy) {//the left mouth end is lower than right mouth end
                    rmy = lmy;
                }
                else{
                    lmy = rmy;
                }
                //Draw rectangles
                //right cheek

                Log.e("Bitmap",""+imageBitmap);
                int size=0;
                for(int j =-rect_width/2;j<rect_width/2;j++){
                    for(int k =-rectangle_height/2;k<rectangle_height/2;k++){
                        rcp[size]= imageBitmap.getPixel((int)((rcx+j)),(int)((rcy+k)));
                        rc_avg_red += Color.red(rcp[size]);
                        rc_avg_green += Color.green(rcp[size]);
                        rc_avg_blue += Color.blue(rcp[size]);
//                mBitmap.setPixel((int)((rcx+j)),(int)((rcy+k)),Color.WHITE);
                        //  Log.e("rcp_"+size,":"+rcp[size]+" Red:"+Color.red(rcp[size])+" Green:"+Color.green(rcp[size])+" Blue:"+Color.blue(rcp[size]));
                        size++;
                    }
                }
                rc_avg_red/=size;
                rc_avg_green/=size;
                rc_avg_blue/=size;
                Log.e("RC Average Color","Red:"+rc_avg_red+" Green:"+rc_avg_green+" Blue:"+rc_avg_blue);

                //----------------------------------------------------------------------------------------------
                //left cheek
                size =0;
                for(int j =-rect_width/2;j<rect_width/2;j++){
                    for(int k =-rectangle_height/2;k<rectangle_height/2;k++){
                        lcp[size]= imageBitmap.getPixel((int)((lcx+j)),(int)((lcy+k)));
                        lc_avg_red += Color.red(lcp[size]);
                        lc_avg_green += Color.green(lcp[size]);
                        lc_avg_blue += Color.blue(lcp[size]);
//                mBitmap.setPixel((int)((lcx+j)),(int)((lcy+k)),Color.WHITE);
                        // Log.e("lcp_"+size,":"+lcp[size]+" Red:"+Color.red(lcp[size])+" Green:"+Color.green(lcp[size])+" Blue:"+Color.blue(lcp[size]));
                        size++;
                    }
                }
                lc_avg_red/=size;
                lc_avg_green/=size;
                lc_avg_blue/=size;
                Log.e("LC Average Color","Red:"+lc_avg_red+" Green:"+lc_avg_green+" Blue:"+lc_avg_blue);
                //----------------------------------------------------------------------------------------------
                //(rectangle near) right mouth end
                size =0;
                for(int j =0;j<rect_width;j++){
                    for(int k =0;k<rectangle_height;k++){
                        rmp[size]= imageBitmap.getPixel((int)((rmx+j)),(int)((rmy+k)));
                        rm_avg_red += Color.red(rmp[size]);
                        rm_avg_green += Color.green(rmp[size]);
                        rm_avg_blue += Color.blue(rmp[size]);
//                    mBitmap.setPixel((int)((rcx+j)),(int)((rmy+k)),Color.WHITE);
                        // Log.e("rmp_"+size,":"+rmp[size]+" Red:"+Color.red(rmp[size])+" Green:"+Color.green(rmp[size])+" Blue:"+Color.blue(rmp[size]));
                        size++;
                    }
                }
                rm_avg_red/=size;
                rm_avg_green/=size;
                rm_avg_blue/=size;
                Log.e("RM Average Color","Red:"+rm_avg_red+" Green:"+rm_avg_green+" Blue:"+rm_avg_blue);
                //----------------------------------------------------------------------------------------------
                //(rectangle near) left mouth end
                size = 0;
                for(int j =0;j<rect_width;j++){
                    for(int k =0;k<rectangle_height;k++){
                        lmp[size]= imageBitmap.getPixel((int)((lmx-j)),(int)((lmy+k)));
                        lm_avg_red += Color.red(lmp[size]);
                        lm_avg_green += Color.green(lmp[size]);
                        lm_avg_blue += Color.blue(lmp[size]);
//                mBitmap.setPixel((int)((lcx-j)),(int)((lmy+k)),Color.WHITE);
                        // Log.e("lmp_"+size,":"+lmp[size]+" Red:"+Color.red(lmp[size])+" Green:"+Color.green(lmp[size])+" Blue:"+Color.blue(lmp[size]));
                        size++;
                    }
                }
                lm_avg_red/=size;
                lm_avg_green/=size;
                lm_avg_blue/=size;
                Log.e("LM Average Color","Red:"+lm_avg_red+" Green:"+lm_avg_green+" Blue:"+lm_avg_blue);

                ColorUtil util = new ColorUtil();
                double diff_1 = util.getColorDifference((int)rc_avg_red,(int)rc_avg_green,(int)rc_avg_blue,(int)lc_avg_red,(int)lc_avg_green,(int)lc_avg_blue);
                double diff_2 =  util.getColorDifference((int)rm_avg_red,(int)rm_avg_green,(int)rm_avg_blue,(int)lm_avg_red,(int)lm_avg_green,(int)lm_avg_blue);
                Log.e("Cheek diff:",""+diff_1);
                Log.e("Mouth diff:",""+diff_2);

                Double delta = diff_2-diff_1; // Delta-E of mouth / Delta-E of cheek;

                Intent intent1 = new Intent();

                if(delta>=10){
                    Log.e("C-Delta = ",""+delta+" Toothbrush detected!");
                    setResult(1,intent1);
                }
                else{
                    Log.e("C-Delta =",""+delta+" Toothbrush not detected");
                    setResult(0,intent1);
                }

                //end of calculations
                finish();
                Log.e("Statement after finish "," executed. I still live, haha!");
            }
        }

    }

}
