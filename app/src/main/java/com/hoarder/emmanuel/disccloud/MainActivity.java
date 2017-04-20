package com.hoarder.emmanuel.disccloud;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Range;
import org.opencv.android.Utils;

import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;

import static com.hoarder.emmanuel.disccloud.R.layout.activity_scanner;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imencode;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.CV_RGBA2mRGBA;
import static org.opencv.imgproc.Imgproc.cvtColor;

public class MainActivity extends AppCompatActivity {




    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private Button camButton;
    private Button collectionButton;
    private Button marketplaceButton;
    public static final String TAG = "MAIN ACTIVITY";

    private ActionBarDrawerToggle drawerToggle;


    static {
        // make sure opencv cv has been loaded correctly
        if(OpenCVLoader.initDebug()) {
            Log.d(TAG, "OPENCV is loaded");
        }else{
            Log.d(TAG, "OPPENCV is no loaded");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set a Toolbar to replace the Actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        mDrawer.addDrawerListener(drawerToggle);


        camButton = (Button) findViewById(R.id.scanner_button);
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Scanner.class);
                startActivity(intent);

            }
        });

        collectionButton = (Button) findViewById(R.id.collection_button);
        collectionButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, My_Collection.class);
                startActivity(intent);

            }
        });

        try{
            ImageCorrection b = new ImageCorrection();

        }catch(Exception e ){

        }


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        // The action bar home/up acton should open or clsoe the drawer

        switch(item.getItemId()){
            case android.R.id.home:
                mDrawer.openDrawer((GravityCompat.START));
                return true;

        }

        return super.onOptionsItemSelected(item);

    }


    private ActionBarDrawerToggle setupDrawerToggle() {




        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }


















}
