package com.ranger.szybkamapa.app.Tools;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Michal on 2015-01-11.
 */
public class BitmapResources {
    private static BitmapResources mInstance = null;

    private String mString;
    ArrayList<Bitmap> bitmapArray;

    private BitmapResources(){
        bitmapArray = new ArrayList<Bitmap>();
    }

    public static BitmapResources getInstance(){
        if(mInstance == null)
        {
            mInstance = new BitmapResources();
        }
        return mInstance;
    }
    public void addBitmap(int i,Bitmap b)
    {
        bitmapArray.add(i,b); // Add a bitmap
    }
    public Bitmap getBitmap(int i)
    {
        return bitmapArray.get(i);
    }
}