package com.surya945.cc.fusionalgorithm9axis;

/**
 * Created by cc on 13-08-2017.
 */

public class Utility {
    private Utility(){}
    public static boolean IsFilterX=false;
    public static boolean IsFilterY=false;
    public static boolean IsFilterZ=false;
    public static boolean IsFilterNormal=true;
    public static boolean IsFilterAss=true;
    public static boolean IsFilterGyro=true;
    public static int WindowSize=15;
    public static  int PolyOrder=7;
    public static int GraphSize=200;
    public static String IsFilterXString="IsFilterX";
    public static String IsFilterYString="IsFilterY";
    public static String IsFilterZString="IsFilterZ";
    public static String IsFilterNormalString="IsFilterNormal";
    public static String WindowSizeString="WindowSize";
    public static String PolyOrderString="PolyOrder";
    public static String GraphSizeString="GraphSize";
    public static SGolay sGolay;

    public static final int X=1;
    public static final int XF=2;
    public static final int Y=3;
    public static final int YF=4;
    public static final int Z=5;
    public static final int ZF=6;
    public static final int Normal=7;
    public static final int NormalF=8;
    public static final int Acc=9;
    public static final int Gyro=10;
    public static final int AccFilter=11;
    public static final int GyroFilter=12;
}
