package com.ranger.szybkamapa.app.Map;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Environment;

import com.ranger.szybkamapa.app.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public int image_width;
    public int image_height;
    public Crd mapPixelSize;


    public Map(File file)
    {
        this.file = file;
        name = file.getName();
    }
    public boolean setupMap()
    {
        this.initialized = readMapFile();
        return this.initialized;
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
                mapPixelSize = new Crd(this.getMapCalibrationPoint(2).pxLoc.x - this.getMapCalibrationPoint(0).pxLoc.x,
                                       this.getMapCalibrationPoint(2).pxLoc.y - this.getMapCalibrationPoint(0).pxLoc.y);

            }
            else {
                throw new IOException("Nieprawidłowy plik .map");
            }
            tempPoints = null;

            File jpg = new File(image);
            if(jpg.isFile())
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                BitmapFactory.decodeFile(image, options);
                image_width = options.outWidth;
                image_height = options.outHeight;
            }
            else
            {
                throw new IOException("Brak pliku:\n" + jpg.getName());
            }

        }
        catch (IOException e) {

            MainActivity.showMsg(e.getMessage());
            return false;
        }
        return true;
    }

}