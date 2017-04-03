package com.hoarder.emmanuel.disccloud;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
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
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.lang.StrictMath.abs;
import static java.util.concurrent.TimeUnit.SECONDS;


public class Scanner extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView javaCameraView;
    private Button testB;
    Mat mRgba;
    Mat imgGray;
    Mat imgCanny;
    String sender;
    ImageReconService mService;
    final String[] hashBuffer = {"iuiuiu", "oioin"}; // 1 element per frame per second

    int hashIndex = 0;
    boolean mBound = false;
    public static final String TAG = "MAIN ACTIVITY";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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

        testB = (Button) findViewById(R.id.testB);
        Intent intent = new Intent(Scanner.this, ImageReconService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);



        testB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBound){
                    String text = mService.sendRequest(hashBuffer);
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);

                }
            }
        });

        /*

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(mBound){
                            String text = mService.sendRequest(hashBuffer);
                            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);

                        }



                    };
                });
            }
        }, 0, 5000);*/


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
        Imgproc.resize( temp_mRgba, temp_mRgba, new Size(100,100));
        Imgproc.cvtColor(temp_mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.bilateralFilter(imgGray, test, 0, 175, 0);
        //Imgproc.GaussianBlur(imgGray, imgGray, new Size(5, 5), 0);
        //Imgproc.medianBlur(imgGray,imgGray,5);
        //mgproc.adaptiveThreshold(imgGray, imgCanny, 255, 1, 1, 11, 2);
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
                temp_double = approxCurve.get(0, 0);
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

                Mat startM = Converters.vector_Point2f_to_Mat(source);
                warp(mRgba, startM, ratio1, ratio2);

            }
            return mRgba;


        } catch ( Exception e) {
            Log.d(TAG, "CANT FIND IT MATE");
            return mRgba;

        }




    }
    public void warp(Mat inputMat, Mat startM, int ratio1, int ratio2) throws Exception{

        int resultHeight = 90 * ratio2;
        int resultWidth = 90 * ratio1;

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

        Imgproc.resize(outputMat,outputMat, new Size(8, 8));
        Core.flip(outputMat,outputMat, 1);
        Core.transpose(outputMat, outputMat);

        ImageCorrection hash = new ImageCorrection();
        bufferController(hash.getHash(outputMat));
        // send image request
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
       //hashBuffer[hashIndex%30] = hash;
       // hashIndex+=1;// move pointer one place "right"
    }




}

