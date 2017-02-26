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

/**
 * Created by emman on 15/10/2016.
 */

public class ImageCorrection  {

    private int hashvalue;
    private Context context;
    private String TAG = "MAIN ACTIVITY";




    ImageCorrection() throws Exception{


        Log.d(TAG, "HEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEERE");

          String hash1;



           Mat readyImg = prepareImg();




           hash1 =  calcHash(getAvg(readyImg), readyImg);

            //hashvalue = hammingDistance(hash1, hash2);

            Log.d(TAG, " The hash value at: " + hash1);



    }





    public int getVal(){
        return hashvalue;
    }

    public String calcHash(int avg, Mat greyImg){


        String bits="";
        for(int i = 0; i < greyImg.rows(); i++){
            for(int j = 0; j < greyImg.cols(); j++){

                if(avg <= (int) greyImg.get(i, j)[0]){
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

    public Mat prepareImg() {
        Mat newImg = new Mat();
        Mat greyImg = new Mat();


        try {
            byte[] imageBytes = LoadImage("/home/emmanuelsq/AndroidStudioProjects/DisCloud/app/src/main/res/drawable/thrust.jpg");

            Mat matImg = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);

            Imgproc.resize(matImg, newImg, new Size(8, 8));


            Imgproc.cvtColor(newImg, greyImg, COLOR_BGR2GRAY);

        }catch(Exception e){
            Log.d(TAG, "ERRRRROR: " + e);
        }





        return greyImg;



    }

    public static byte[] LoadImage(String filePath) throws Exception {
        File file = new File(filePath);
        int size = (int)file.length();
        byte[] buffer = new byte[size];
        FileInputStream in = new FileInputStream(file);
        in.read(buffer);
        in.close();
        return buffer;
    }

    private int hammingDistance(String hash1, String hash2){

        char[] h1 = hash1.toCharArray();
        char[] h2 = hash2.toCharArray();



        int hamDist = 0;
        for(int i =0; i < 238; i++){

            if(h1[i] != h2[i]){
                hamDist += 1;
            }

        }


        return hamDist;

    }



}
