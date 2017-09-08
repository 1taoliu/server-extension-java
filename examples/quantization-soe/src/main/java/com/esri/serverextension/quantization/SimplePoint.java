package com.esri.serverextension.quantization;

/**
 * Created by kcoffin on 9/8/17.
 */
public class SimplePoint {
    public long x;
    public long y;

    public SimplePoint(long x, long y){
        this.x = x;
        this.y = y;
    }

    public boolean equals(SimplePoint pt){
        return (pt.x == x && pt.y == y);
    }
    public boolean equals(long xx, long yy){
        return (xx == x && yy == y);
    }
}
