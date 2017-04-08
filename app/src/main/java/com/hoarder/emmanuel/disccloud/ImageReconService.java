package com.hoarder.emmanuel.disccloud;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.util.Base64;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by emmanuelsq on 26/02/17.
 */

public class ImageReconService extends IntentService {
    private final IBinder mBinder = new LocalBinder();
    public String text;




    public ImageReconService(){
        super("ImageReconService");
    }


    public IBinder onBind(Intent intent){
        return mBinder;
    }


    @Override
    protected void onHandleIntent(Intent workIntent){

        String dataString = workIntent.getDataString();


    }


    public String sendRequest(String[] hashes){


        HashMap<String, String[]> data = new HashMap<>();
        data.put("hashes",  hashes);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://discloud.herokuapp.com/searchcover";


        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(data), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {
                    text = response.getString("title");
                    text += ","+response.getString("artist");

                }catch (JSONException e){
                    text = "Error in the json:  " +e;
                }

            }}, new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        text = "Error in server:  " +error;
                    }
                });

                queue.add(stringRequest);


        return text;
    }


    public class LocalBinder extends Binder{
        public ImageReconService getService(){
            return ImageReconService.this;

        }
    }
}
