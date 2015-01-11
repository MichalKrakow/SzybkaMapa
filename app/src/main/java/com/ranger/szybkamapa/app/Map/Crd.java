package com.ranger.szybkamapa.app.Map;

/**
 * Created by Michal on 2015-01-11.
 */
public class Crd {
    public double x;
    public double y;

    public Crd(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    @Override
    public String toString()
    {
        return "CRD: "+ this.x + " / " + this.y;
    }
}
