package com.hoarder.emmanuel.disccloud;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;

import android.widget.LinearLayout;
import android.widget.TextView;


import java.io.File;
import java.lang.Exception;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.StrictMath.abs;


public class Scanner extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView javaCameraView;
    Mat mRgba;
    Mat imgGray;
    Mat imgCanny;
    ImageReconService mService;
    final String[] hashBuffer = new String[30]; // 1 element per frame per second
    String text = "";
    String[] fields;
    private TextView textViewA = null;
    private TextView textViewT = null;
    private TextView textViewV = null;
    private LinearLayout lView;
    int hashIndex = 0;
    boolean mBound = false;
    public static final String TAG = "MAIN ACTIVITY";
    private String lastTitle = "Title";
    private String lastArtist = "Artist";
    private String lastValue = "";
    private int counter =0; // counter is to make sure record not recognised does not appear too often

    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        javaCameraView = (JavaCameraView) findViewById(R.id.java_camera);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        Intent intent = new Intent(Scanner.this, ImageReconService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);




        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(mBound && hashBuffer[0] != null){ // ensure atleast one hash has been found before sending
                            text = mService.sendRequest(hashBuffer);
                            Arrays.fill(hashBuffer, null);

                            if(lView != null){
                                lView.removeAllViews();
                            }
                            DisplayMetrics displayMetrics = new DisplayMetrics(); // we need to get the height and width of screen to keep text view relative
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int width = displayMetrics.widthPixels;
                            float posX = (float) (width*0.6);
                            lView = (LinearLayout) findViewById(R.id.mylinear); // create horizontal layout to add text views stacked ontop of each other vertically
                            textViewT = new TextView(Scanner.this);
                            textViewT.setX(posX);
                            textViewT.setTextSize((float) 20.0);
                            textViewT.setTextColor(Color.GREEN);

                            textViewA = new TextView(Scanner.this);
                            textViewA.setX(posX);
                            textViewA.setY(50);
                            textViewA.setTextSize((float) 20.0);
                            textViewA.setTextColor(Color.GREEN);

                            textViewV = new TextView(Scanner.this);
                            textViewV.setX(posX);
                            textViewV.setY(100);
                            textViewV.setTextSize((float) 20.0);
                            textViewV.setTextColor(Color.YELLOW);

                            if(text != null){
                                fields = text.split(",");
                                if(!(fields[0].equals("na"))){
                                    if(fields.length == 3){
                                        textViewT.setText(fields[0]);
                                        textViewA.setText(fields[1]);
                                        textViewV.setText(textRating(fields[2]));
                                        //also set last elements seen as to not keep switching
                                        lastTitle = fields[0];
                                        lastArtist = fields[1];
                                        lastValue = textRating(fields[2]);
                                        counter = 0;
                                    }
                                }else{
                                    counter += 1;
                                    if(counter == 5){
                                        lastTitle = "Record not recogonsied";
                                        lastArtist = "try different background";
                                        lastValue = "";
                                        counter = 0;
                                    }
                                    textViewT.setText(lastTitle);
                                    textViewA.setText(lastArtist);
                                    textViewV.setText(lastValue);

                                }
                            }
                            lView.addView(textViewT);
                            lView.addView(textViewA);
                            lView.addView(textViewV);
                            Log.d(TAG,"----"+text+ "------");
                            hashIndex = 0;
                        }
                    };
                });
            }
        }, 0, 1500);
    }
    private ServiceConnection  mConnection = new ServiceConnection(){

        public void onServiceConnected(ComponentName className, IBinder service){
            ImageReconService.LocalBinder binder = (ImageReconService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;


        }

        public void onServiceDisconnected(ComponentName className){

            mBound = false;

        }
    };
    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }

    }
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            mLoaderCallBack.onManagerConnected((LoaderCallbackInterface.SUCCESS));
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallBack);
        }
    }
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgGray = new Mat(height, width, CvType.CV_8UC1);
        imgCanny = new Mat(height, width, CvType.CV_8UC1);
    }
    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        double largest = -1;
        int largestId = -1;
        Mat test = new Mat();
        int ratio1 = mRgba.cols() / 100;
        int ratio2 = mRgba.rows() / 100;
        mRgba = inputFrame.rgba();
        Mat temp_mRgba = mRgba.clone();
        Imgproc.resize( temp_mRgba, temp_mRgba, new Size(100,100)); //shrink image as it makes contour detection easier
        Imgproc.cvtColor(temp_mRgba, imgGray, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.bilateralFilter(imgGray, test, 0, 175, 0);
        Imgproc.Canny(test, imgCanny, 30, 200);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();


        try {
            Imgproc.findContours(imgCanny, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            MatOfPoint temp_contour;
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f approxCurve_temp = new MatOfPoint2f();

            for (int idx = 0; idx < contours.size(); idx++) {
                temp_contour = contours.get(idx);
                double contourarea = Imgproc.contourArea(temp_contour);

                if(contourarea > largest && contourarea > 155){

                    MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                    //double peri = temp_contour.total();
                    double peri =  Imgproc.arcLength(new_mat, true);
                    Imgproc.approxPolyDP(new_mat, approxCurve_temp, peri*0.02, true);

                    if(approxCurve_temp.total() == 4){
                        largest = contourarea;
                        approxCurve = approxCurve_temp;
                        largestId = idx;
                    }
                }
            }

            Point[] points = approxCurve.toArray();

            if(points.length == 4){
                Imgproc.drawContours(mRgba, contours, largestId, new Scalar(255, 0, 255), 3);

                double[] temp_double;
                temp_double = approxCurve.get(0, 0); // because we have been using a shrunken image we need to re expand the points to their relatie locations in the full size image
                Point p1 = new Point(temp_double[0]*ratio1, temp_double[1]*ratio2);

                temp_double = approxCurve.get(1, 0);
                Point p2 = new Point(temp_double[0]*ratio1, temp_double[1]*ratio2);

                temp_double = approxCurve.get(2, 0);
                Point p3 = new Point(temp_double[0]*ratio1, temp_double[1]*ratio2);

                temp_double = approxCurve.get(3, 0);
                Point p4 = new Point(temp_double[0]*ratio1, temp_double[1]*ratio2);

                List<Point> source = new ArrayList<>();
                source.add(p1);
                source.add(p2);
                source.add(p3);
                source.add(p4);

                source = sortPoints(source);
                Mat startM = Converters.vector_Point2f_to_Mat(source); // new image with the image inside the contour boundary
                warp(mRgba, startM, ratio1, ratio2);
            }
            return mRgba;


        } catch ( Exception e) {
           Log.d(TAG, "EXCEPTION" + e);
            return mRgba;

        }
    }

    public void warp(Mat inputMat, Mat startM, int ratio1, int ratio2) throws Exception{
        int resultHeight = 100 * ratio2;
        int resultWidth = 100 * ratio1;

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);
        Point outPoint1 = new Point(0,0);
        Point outPoint2 = new Point(resultWidth, 0);
        Point outPoint3 = new Point(resultWidth, resultHeight);
        Point outPoint4 = new Point(0, resultHeight);

        List<Point> dst = new ArrayList<>();
        dst.add(outPoint1);
        dst.add(outPoint2);
        dst.add(outPoint3);
        dst.add(outPoint4);

        Mat endM = Converters.vector_Point2f_to_Mat(dst);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);

        //Imgproc.resize(outputMat,outputMat, new Size(300, 300));
        Core.flip(outputMat,outputMat, 1);
        Core.transpose(outputMat, outputMat);

        ImageCorrection hash = new ImageCorrection();
        bufferController(hash.getHash(outputMat));
        //storeImage(outputMat);
    }
    public List<Point> sortPoints(List<Point> points){

        Point lgstPoint = new Point(0,0);
        Point smlstPoint = new Point(999, 999);
        Point smlstDiffPoint = new Point(999, 0);
        Point lgstDiffPoint = new Point(0, 999);
        List<Point> sorted = new ArrayList<>();
        for(Point point : points){

            if(abs(point.x + point.y) < (abs(smlstPoint.x + smlstPoint.y))){
                smlstPoint = point;
            }

            if(abs(point.x + point.y) > (abs(lgstPoint.x + lgstPoint.y))){
                lgstPoint = point;
            }
            if((point.x - point.y) < (smlstDiffPoint.x - smlstDiffPoint.y)){
                smlstDiffPoint = point;
            }
            if((point.x - point.y) > (lgstDiffPoint.x - lgstDiffPoint.y)){
                lgstDiffPoint = point;
            }

        }
        sorted.add(smlstDiffPoint);
        sorted.add(smlstPoint);
        sorted.add(lgstDiffPoint);
        sorted.add(lgstPoint);
        return sorted;
    }

    public void bufferController(String hash){
       hashBuffer[hashIndex%30] = hash;
        hashIndex+=1;
    }

    public String textRating(String val){
        int rating = Integer.parseInt(val);
        String dispRating = "";
        for(int i =0; i < rating; i++){
            dispRating += "☆";
        }

        return dispRating;
    }
}

