package com.surya945.cc.fusionalgorithm9axis;

/**
 * Created by cc on 27-07-2017.
 */

public  class AccelerationData{
    double normal,filtered;
    int index;

    public AccelerationData(int index,double normal,double filtered){
        this.index=index;
        this.normal=normal;
        this.filtered=filtered;
    }
}