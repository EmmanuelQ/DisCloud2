package com.hoarder.emmanuel.disccloud;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by emmanuelsq on 26/02/17.
 */

public class ImageReconService extends IntentService {



    public ImageReconService(){
        super("ImageReconService");

    }


    @Override
    protected void onHandleIntent(Intent workIntent){

        String dataString = workIntent.getDataString();


    }
}
