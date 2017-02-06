package com.hoarder.emmanuel.disccloud;


import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.SurfaceView;
;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static java.lang.StrictMath.abs;


public class Scanner extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView javaCameraView;
    Mat mRgba;
    Mat imgGray;
    Mat imgCanny;
    Mat approx;

    public static final String TAG = "MAIN ACTIVITY";


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



    }


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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        double largest = -1;
        int largestId = -1;


        Mat result = new Mat();
        mRgba = inputFrame.rgba();
        Mat temp_mRgba = mRgba.clone();
        Imgproc.resize( temp_mRgba, temp_mRgba, new Size(100,100));
        int ratio1 = mRgba.cols() / 100;
        int ratio2 = mRgba.rows() / 100;


        //Imgproc.bilateralFilter(mRgba, mRgba, 11, 17,17);

        Imgproc.cvtColor(temp_mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
        //Imgproc.resize(imgGray, imgGray, new Size(100,100));

        Imgproc.GaussianBlur(imgGray, imgGray, new Size(5, 5), 5);

        //Imgproc.medianBlur(imgGray,imgGray,5);

        Imgproc.Canny(imgGray, imgCanny, 30, 200);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();


        try {
            Imgproc.findContours(imgCanny, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            MatOfPoint temp_contour;
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f approxCurve_temp = new MatOfPoint2f();



            for (int idx = 0; idx < contours.size(); idx++) {

                temp_contour = contours.get(idx);
                double contourarea = Imgproc.contourArea(temp_contour);




                if(contourarea > largest){

                    MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());

                    //double peri = temp_contour.total();

                    double peri =  Imgproc.arcLength(new_mat, true);
                    Imgproc.approxPolyDP(new_mat, approxCurve_temp, peri*0.08, true);


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

                Mat startM = Converters.vector_Point2f_to_Mat(source);
                result = warp(mRgba, startM, ratio1, ratio2);

            }





            return mRgba;


        } catch (IndexOutOfBoundsException ex) {
            Log.d(TAG, "CANT FIND IT MATE");
            return mRgba;

        }




    }

    public Mat warp(Mat inputMat, Mat startM, int ratio1, int ratio2){

        int resultHeight = 90 * ratio2;
        int resultWidth = 90 * ratio1;



        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);



        Point outPoint1 = new Point(0,0);
        Point outPoint2 = new Point(0, resultHeight);
        Point outPoint3 = new Point(resultWidth, resultHeight);
        Point outPoint4 = new Point(resultWidth, 0);

        List<Point> dst = new ArrayList<>();


        dst.add(outPoint1);
        dst.add(outPoint2);
        dst.add(outPoint3);
        dst.add(outPoint4);

        Mat endM = Converters.vector_Point2f_to_Mat(dst);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform, new Size(resultWidth, resultHeight));



        storeImage(outputMat);


        return outputMat;
    }


    public void storeImage(Mat mat){
        Mat temp_mat = new Mat();
        Random generator = new Random();



        Imgproc.cvtColor(mat, temp_mat, Imgproc.COLOR_RGBA2BGR, 3);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String filename = ""+generator.nextInt(20)+".png";
        File file = new File(path, filename);

        Boolean bool = null;

        filename = file.toString();
        bool = Imgcodecs.imwrite(filename, temp_mat);


        if (bool == true)
            Log.d(TAG, "SUCCESS writing image to external storage");
        else
            Log.d(TAG, "Fail writing image to external storage");
    }



    public List<Point> sortPoints(ArrayList<Point> points){
        int lgst;
        int smlst = -1;
        int smlstId = 0;
        List<Point> sorted = new ArrayList<>();





        for(Point point : points){

            if(abs(point.x - point.y) < smlst){

            }

        }


        sorted.add(points.get(smlstId));




        return points;
    }









}

