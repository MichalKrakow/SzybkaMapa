package com.ranger.szybkamapa.app.Map;

import android.graphics.Point;

/**
 * Created by Michal on 2014-12-07.
 */
public class MapCalibrationPoint {

    public final Point pxLoc;
    public final FPoint geoLoc;

    public MapCalibrationPoint(Point pxLoc, FPoint geoLoc){
        this.pxLoc = pxLoc;
        this.geoLoc = geoLoc;
    }

    @Override
    public String toString() {
        return "MapCalibrationPoint [" + pxLoc.x + "," + pxLoc.y +"] " + "[" + geoLoc.x + ","+ geoLoc.y + "]";
    }

}
