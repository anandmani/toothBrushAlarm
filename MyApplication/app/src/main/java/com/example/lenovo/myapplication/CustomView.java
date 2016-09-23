package com.example.lenovo.myapplication;


/**
 * Created by Lenovo on 11-09-2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

/**
 * Created by echessa on 8/31/15.
 */
public class CustomView extends View {
    public static double delta;

    private Bitmap mBitmap,mBitmap1;
    private SparseArray<Face> mFaces;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Sets the bitmap background and the associated face detections.
     */
    void setContent(Bitmap bitmap, SparseArray<Face> faces) {
        mBitmap = bitmap;
        mBitmap1 = mBitmap.copy(mBitmap.getConfig(), true);
        mBitmap = mBitmap1.copy(mBitmap1.getConfig(), true);
        mFaces = faces;

        invalidate();
    }

    /**
     * Draws the bitmap background and the associated face landmarks.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ((mBitmap != null) && (mFaces != null)) {
            double scale = drawBitmap(canvas);
//           drawFaceRectangle(canvas, scale);
            drawFaceAnnotations(canvas, scale);
        }
    }

    /**
     * Draws the bitmap background, scaled to the device size.  Returns the scale for future use in
     * positioning the facial landmark graphics.
     */
    private double drawBitmap(Canvas canvas) {
        Log.e("Called","true");
        double viewWidth = canvas.getWidth();
        double viewHeight = canvas.getHeight();
        double imageWidth = mBitmap.getWidth();
        double imageHeight = mBitmap.getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        Rect destBounds = new Rect(0, 0, (int)(imageWidth * scale), (int)(imageHeight * scale));
        canvas.drawBitmap(mBitmap, null, destBounds, null);
        return scale;
    }

    /**
     * Draws a rectangle around each detected face
     */
    private void drawFaceRectangle(Canvas canvas, double scale) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (int i = 0; i < mFaces.size(); ++i) {
            Face face = mFaces.valueAt(i);
//            Log.e("face width",""+face.getWidth());
//            Log.e("face heigth",""+face.getHeight());
            canvas.drawRect((float)(face.getPosition().x * scale),
                    (float)(face.getPosition().y * scale),
                    (float)((face.getPosition().x + face.getWidth()) * scale),
                    (float)((face.getPosition().y + face.getHeight()) * scale),
                    paint);
        }
    }

    /**
     * Draws a small circle for each detected landmark, centered at the detected landmark position.
     *
     * Note that eye landmarks are defined to be the midpoint between the detected eye corner
     * positions, which tends to place the eye landmarks at the lower eyelid rather than at the
     * pupil position.
     */
    private void drawFaceAnnotations(Canvas canvas, double scale) {
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
        for (int i = 0; i < mFaces.size(); ++i) {
            // y = 3 is  right cheek int rc x,y  [my right. not image's face's right]
            //y = 4 is  left cheek  int lc x,y
            //y = 5 is  right mouth-end  int rm x,y
            // y =6 is  left mouth-end  int lm x,y
            Face face = mFaces.valueAt(i);
            rect_width = (int)face.getWidth()/16;
            rectangle_height = (int)face.getHeight()/16;
//            Log.e("face width",""+face.getWidth());
//            Log.e("face heigth",""+face.getHeight());
            for (Landmark landmark : face.getLandmarks()) {

                int cx = (int) (landmark.getPosition().x * scale);

                int cy = (int) (landmark.getPosition().y * scale);


                if(y==3){
                    paint.setColor(Color.BLUE);
                    canvas.drawCircle(cx, cy, 10, paint);
                    canvas.drawLine(cx,0,cx,canvas.getHeight(),paint);
                    rcx = (int) (landmark.getPosition().x);
                    rcy =(int) (landmark.getPosition().y);
                }
                if(y==4){
                    paint.setColor(Color.BLUE);
                    canvas.drawCircle(cx, cy, 10, paint);
                    canvas.drawLine(cx,0,cx,canvas.getHeight(),paint);
                    lcx =(int) (landmark.getPosition().x);
                    lcy =(int) (landmark.getPosition().y);
                }
                if(y==5) {
                    paint.setColor(Color.GREEN);
                    canvas.drawCircle(cx, cy, 10, paint);
                    canvas.drawLine(0,cy,canvas.getWidth(),cy,paint);
//                    dummyx = (int) (landmark.getPosition().x);
//                    dummyy=(int) (landmark.getPosition().y );
                    rmx = (int) (landmark.getPosition().x);
                    rmy =(int) (landmark.getPosition().y);
                }
                if(y==6) {
                    paint.setColor(Color.GREEN);
                    canvas.drawCircle(cx, cy, 10, paint);
                    canvas.drawLine(0,cy,canvas.getWidth(),cy,paint);
                    lmx=(int) (landmark.getPosition().x);
                    lmy=(int) (landmark.getPosition().y);
                }

                y++;
            }//end of each landmark
//            for(int j =0;j<50;j++){
//                for(int k =0;k<50;k++){
//                    mBitmap.setPixel((int)((dummyx+j)),(int)((dummyy+k)),Color.WHITE);
//                }
//            }
            //scale = drawBitmap(canvas);

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

        int size=0;
        for(int j =-rect_width/2;j<rect_width/2;j++){
            for(int k =-rectangle_height/2;k<rectangle_height/2;k++){
                rcp[size]= mBitmap.getPixel((int)((rcx+j)),(int)((rcy+k)));
                rc_avg_red += Color.red(rcp[size]);
                rc_avg_green += Color.green(rcp[size]);
                rc_avg_blue += Color.blue(rcp[size]);
                mBitmap.setPixel((int)((rcx+j)),(int)((rcy+k)),Color.WHITE);
                //  Log.e("rcp_"+size,":"+rcp[size]+" Red:"+Color.red(rcp[size])+" Green:"+Color.green(rcp[size])+" Blue:"+Color.blue(rcp[size]));
                size++;
            }
        }
        rc_avg_red/=size;
        rc_avg_green/=size;
        rc_avg_blue/=size;
//        Log.e("RC Average Color","Red:"+rc_avg_red+" Green:"+rc_avg_green+" Blue:"+rc_avg_blue);

        //----------------------------------------------------------------------------------------------
        //left cheek
        size =0;
        for(int j =-rect_width/2;j<rect_width/2;j++){
            for(int k =-rectangle_height/2;k<rectangle_height/2;k++){
                lcp[size]= mBitmap.getPixel((int)((lcx+j)),(int)((lcy+k)));
                lc_avg_red += Color.red(lcp[size]);
                lc_avg_green += Color.green(lcp[size]);
                lc_avg_blue += Color.blue(lcp[size]);
                mBitmap.setPixel((int)((lcx+j)),(int)((lcy+k)),Color.WHITE);
                // Log.e("lcp_"+size,":"+lcp[size]+" Red:"+Color.red(lcp[size])+" Green:"+Color.green(lcp[size])+" Blue:"+Color.blue(lcp[size]));
                size++;
            }
        }
        lc_avg_red/=size;
        lc_avg_green/=size;
        lc_avg_blue/=size;
//        Log.e("LC Average Color","Red:"+lc_avg_red+" Green:"+lc_avg_green+" Blue:"+lc_avg_blue);
        //----------------------------------------------------------------------------------------------
        //(rectangle near) right mouth end
        size =0;
        for(int j =0;j<rect_width;j++){
            for(int k =0;k<rectangle_height;k++){
                rmp[size]= mBitmap.getPixel((int)((rmx+j)),(int)((rmy+k)));
                rm_avg_red += Color.red(rmp[size]);
                rm_avg_green += Color.green(rmp[size]);
                rm_avg_blue += Color.blue(rmp[size]);
                    mBitmap.setPixel((int)((rmx+j)),(int)((rmy+k)),Color.WHITE);
                // Log.e("rmp_"+size,":"+rmp[size]+" Red:"+Color.red(rmp[size])+" Green:"+Color.green(rmp[size])+" Blue:"+Color.blue(rmp[size]));
                size++;
            }
        }
        rm_avg_red/=size;
        rm_avg_green/=size;
        rm_avg_blue/=size;
//        Log.e("RM Average Color","Red:"+rm_avg_red+" Green:"+rm_avg_green+" Blue:"+rm_avg_blue);
        //----------------------------------------------------------------------------------------------
        //(rectangle near) left mouth end
        size = 0;
        for(int j =0;j<rect_width;j++){
            for(int k =0;k<rectangle_height;k++){
                lmp[size]= mBitmap.getPixel((int)((lmx-j)),(int)((lmy+k)));
                lm_avg_red += Color.red(lmp[size]);
                lm_avg_green += Color.green(lmp[size]);
                lm_avg_blue += Color.blue(lmp[size]);
                mBitmap.setPixel((int)((lmx-j)),(int)((lmy+k)),Color.WHITE);
                // Log.e("lmp_"+size,":"+lmp[size]+" Red:"+Color.red(lmp[size])+" Green:"+Color.green(lmp[size])+" Blue:"+Color.blue(lmp[size]));
                size++;
            }
        }
        lm_avg_red/=size;
        lm_avg_green/=size;
        lm_avg_blue/=size;
//        Log.e("LM Average Color","Red:"+lm_avg_red+" Green:"+lm_avg_green+" Blue:"+lm_avg_blue);

//        ColorUtil util = new ColorUtil();
//        double diff_1 = util.getColorDifference((int)rc_avg_red,(int)rc_avg_green,(int)rc_avg_blue,(int)lc_avg_red,(int)lc_avg_green,(int)lc_avg_blue);
//        double diff_2 =  util.getColorDifference((int)rm_avg_red,(int)rm_avg_green,(int)rm_avg_blue,(int)lm_avg_red,(int)lm_avg_green,(int)lm_avg_blue);
//        Log.e("Cheek diff:",""+diff_1);
//        Log.e("Mouth diff:",""+diff_2);
//
//        Double delta = diff_2-diff_1; // Delta-E of mouth / Delta-E of cheek;
//
//
//        if(delta>=10){
//            Log.e("Delta = ",""+delta+" Toothbrush detected!");
//        }
//        else{
//            Log.e("Delta =",""+delta+" Toothbrush not detected");
//        }


    }//end of method
}//end of class