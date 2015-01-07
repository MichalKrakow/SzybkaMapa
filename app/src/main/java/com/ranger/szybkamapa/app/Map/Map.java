package com.ranger.szybkamapa.app.Map;

import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Michal on 2014-12-07.
 */
public class Map {
    public String image;
    public File file;
    public String name;
    public Vector<MapCalibrationPoint> points;
    public Boolean initialized = false;

    public Map(File file)
    {
        this.file = file;
        name = file.getName();
    }
    public boolean setupMap()
    {
        return readMapFile();
    }
    public MapCalibrationPoint getMapCalibrationPoint(int i)
    {
        if(points == null)
        readMapFile();

        return points.get(i);
    }
    private boolean readMapFile()
    {

        try {

            Vector<FPoint> tempPoints = new Vector<FPoint>();
            points = new Vector<MapCalibrationPoint>();

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                if(line.contains(".jpg"))
                {
                    image = Environment.getExternalStorageDirectory().toString()+"/mapy/" + line;
                }

                if(line.contains("MMPXY"))
                {
                    String[] l = line.split(",");
                    tempPoints.add(new FPoint( Double.parseDouble(l[2]) ,Double.parseDouble(l[3]) ));
                }

                if(line.contains("MMPLL"))
                {
                    String[] l = line.split(",");
                    tempPoints.add(new FPoint( Double.parseDouble(l[2]) ,Double.parseDouble(l[3]) ));
                }
            }

            if(tempPoints.size() == 8)
            {

                for(int i=0;i<4;i++)
                {
                    points.add(new MapCalibrationPoint( new Point( (int)tempPoints.get(i).x ,(int)tempPoints.get(i).y ) , new FPoint(tempPoints.get(i+4).x,tempPoints.get(i+4).y)));
                    //Log.v("GOTOWY PUNKT",points.get(i).toString());
                }
            }
            tempPoints = null;

        }
        catch (IOException e)
        {
            return false;
        }
        this.initialized = true;
        return true;
    }
}