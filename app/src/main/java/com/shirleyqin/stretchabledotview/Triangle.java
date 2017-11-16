package com.shirleyqin.stretchabledotview;

/**
 * Created by shirleyqin on 2017-11-16.
 */

public class Triangle {
    public int a = 0;
    public int b = 0;
    private int c = 0;

    public void reset() {
        a = 0;
        b = 0;
        c = 0;
    }

    public int getC() {
        c = (int)Math.sqrt((Math.pow(a, 2)
                + Math.pow(b, 2)));
        return c;
    }
}
