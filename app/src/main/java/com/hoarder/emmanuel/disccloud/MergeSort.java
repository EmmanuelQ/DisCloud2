package com.hoarder.emmanuel.disccloud;

import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emmanuelsq on 27/01/17.
 */


public class MergeSort {
    private ArrayList<MatOfPoint> numbers;
    private ArrayList<MatOfPoint> helper;

    private int number;


    MergeSort(ArrayList<MatOfPoint> list){
        this.numbers = list;

        mergesort(0, 10);


    }



    ArrayList<MatOfPoint> getList(){
        return numbers;
    }


    private void mergesort(int low, int high) {
        // check if low is smaller then high, if not then the array is sorted
        if (low < high) {
            // Get the index of the element which is in the middle
            int middle = low + (high - low) / 2;
            // Sort the left side of the array
            mergesort(low, middle);
            // Sort the right side of the array
            mergesort(middle + 1, high);
            // Combine them both
            merge(low, middle, high);
        }
    }

    private void merge(int low, int middle, int high) {

        // Copy both parts into the helper array
        for (int i = low; i <= high; i++) {
            helper.set(i, numbers.get(i));

        }

        int i = low;
        int j = middle + 1;
        int k = low;
        // Copy the smallest values from either the left or the right side back
        // to the original array
        while (i <= middle && j <= high) {
            if (Imgproc.contourArea(helper.get(i)) <= Imgproc.contourArea(helper.get(j))) {

                numbers.set(k, helper.get(i));
                i++;
            } else {
                numbers.set(k, helper.get(i));
                j++;
            }
            k++;
        }
        // Copy the rest of the left side of the array into the target array
        while (i <= middle) {
            numbers.set(k, helper.get(i));
            k++;
            i++;
        }

    }
}
