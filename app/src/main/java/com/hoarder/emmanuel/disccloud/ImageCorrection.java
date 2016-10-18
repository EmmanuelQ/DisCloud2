package com.hoarder.emmanuel.disccloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.math.BigInteger;

import static com.hoarder.emmanuel.disccloud.MainActivity.TAG;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

/**
 * Created by emman on 15/10/2016.
 */

public class ImageCorrection {

    private int hashvalue;
    private Context context;
    private String TAG = "MAIN ACTIVITY";
    Bitmap img = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);



    ImageCorrection(Context context, int image1, int image2){
        this.context = context;


        Mat readyImg = prepareImg(image1);
        Mat readyImg2 = prepareImg(image2);
        Mat rotatedImg = new Mat();

        String hash1 = calcHash(getAvg(readyImg), readyImg);
        String hash2;


        
        for(int i = 0; i < 91; i++){

            rotatedImg = rotateImg((double) i, readyImg2);
            hash2 =  calcHash(getAvg(rotatedImg), rotatedImg);

            hashvalue = hammingDistance(hash1, hash2);

            Log.d(TAG, " The hash value at: " + (double) i + " degrees is " + hashvalue);


        }





       // Utils.matToBitmap(rotatedImg, img);



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

    public Mat prepareImg(int imgId){

        Mat matImg = new Mat();
        Mat newImg = new Mat();
        Mat greyImg = new Mat();



        Size sz = new Size(32, 32);

        //Get img drawable and convert to bitmap
        Bitmap bMap = BitmapFactory.decodeResource(context.getResources(), imgId);

        //Now convert to Mat then reduce size to 32px and greyscale img
        Utils.bitmapToMat(bMap, matImg);
        Imgproc.resize(matImg, newImg, sz);
        Imgproc.cvtColor(newImg, greyImg, COLOR_BGR2GRAY);



        return greyImg;

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

    public Mat rotateImg(double degrees, Mat image){

        Mat dst = new Mat(new Size(32, 32), 1);

        Mat rotationMatrix = Imgproc.getRotationMatrix2D(new Point(16, 16), degrees,1 );
        Imgproc.warpAffine(image, dst, rotationMatrix, image.size());


        return dst;



    }

}
