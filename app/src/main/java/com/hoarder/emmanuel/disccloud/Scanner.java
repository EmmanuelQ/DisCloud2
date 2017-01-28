package com.hoarder.emmanuel.disccloud;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Scanner extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    JavaCameraView javaCameraView;
    Mat mRgba;
    Mat imgGray;
    Mat imgCanny;

    public static final String TAG = "MAIN ACTIVITY";



    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;
                }
                default:{
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


    }





    

    @Override
    protected void onPause(){
        super.onPause();

        if(javaCameraView != null){
            javaCameraView.disableView();
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }

    }

    protected void onResume(){
        super.onResume();

        if(OpenCVLoader.initDebug()){
            mLoaderCallBack.onManagerConnected((LoaderCallbackInterface.SUCCESS));
        }

        else{
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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat newImg = new Mat();
        //Get img drawable and convert to bitmap
       // Bitmap bMap = BitmapFactory.decodeResource(context.getResources(), imgId);

        //Now convert to Mat then reduce size to 32px and greyscale img
        //Utils.bitmapToMat(bMap, newImg);

        Mat img = Imgcodecs.imread("res/drawable/barrywhite.jpgn");

        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);

        Imgproc.Canny(imgGray, imgCanny, 40, 120);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(imgCanny, contours, imgCanny.clone(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        ArrayList<MatOfPoint> sorted =  new MergeSort(contours).getList();

        Log.d(TAG, "-------" + sorted.get(0));





        return imgCanny;
    }
}
