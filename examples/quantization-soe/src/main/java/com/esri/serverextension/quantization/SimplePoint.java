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
    public boolean hasZeroOffsetAndIsAhead(SimplePoint end, long offX, long offY){

        long voffX = offX-x;
        long voffY = offY-y;
        long vLineX = end.x-x;
        long vLineY = end.y-y;
        long offset = vLineY*voffX-vLineX*voffY;
        if (offset != 0){
            return false;
        }
        long station = vLineX*voffX+vLineY*voffY;
        return (station > 0);
    }


    public boolean equals(SimplePoint pt){
        return (pt.x == x && pt.y == y);
    }
    public boolean equals(long xx, long yy){
        return (xx == x && yy == y);
    }
}
