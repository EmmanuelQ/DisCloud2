package com.hoarder.emmanuel.disccloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import static com.hoarder.emmanuel.disccloud.MainActivity.TAG;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGRA2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;

/**
 * Created by emman on 15/10/2016.
 */

public class ImageCorrection  {



    public String getHash(Mat image){

        Mat readyImg = prepareImg(image);
        String hashvalue = calcHash(getAvg(readyImg), readyImg);
        return hashvalue;
    }

    public String calcHash(int avg, Mat greyImg){

        String bits="";
        for(int i = 0; i < greyImg.rows(); i++){
            for(int j = 0; j < greyImg.cols(); j++){
                if((int) greyImg.get(i, j)[0] < avg){
                    bits+="1";

                }else{
                    bits+="0";
                };
            }
        }

        //convert to hex
         BigInteger big = new BigInteger(bits, 2);
         String hextr = big.toString(16);

         return hextr;
    }

    public int getAvg(Mat greyImg){
        //Calculate average value for grey img
        int avg;
        double sum = 0.0;
        for(int i = 0; i < greyImg.rows(); i++){
            for(int j = 0; j < greyImg.cols(); j++){
                double[] pixel = greyImg.get(i, j);
                sum += pixel[0];
            }
        }

        avg = (int) sum / (int) greyImg.total();
        return avg;

    }

    public Mat prepareImg(Mat matImg){
        Mat greyImg = new Mat();
        Imgproc.resize(matImg, matImg, new Size(8,8));
        Imgproc.cvtColor(matImg, greyImg, COLOR_RGBA2GRAY, 3); // frame is rgba so convert to gray

        return greyImg;
    }




}
