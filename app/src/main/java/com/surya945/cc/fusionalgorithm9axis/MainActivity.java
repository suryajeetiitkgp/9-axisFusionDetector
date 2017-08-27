package com.surya945.cc.fusionalgorithm9axis;


import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    List<Double> normalData =new ArrayList<>(500);

    TextView X_value;
    TextView Y_value;
    TextView Z_value;
    TextView Gyro1;
    TextView Gyro2;
    TextView Gyro3;

    TextView currentDataLength;
    EditText fileName;
    Button saveButton;
    Button startButton;
    Button stopButton;
    int frameLength=20;
    int polynomialOrder=7;
    int graphSize=200;
    int index=frameLength;
    boolean capturingData =true;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    GraphView graph_Acce,graph_Gyro,graph_AcceFiltered,graph_GyroFiltered;
    private LineGraphSeries<DataPoint> series_Acc, series_Gyro,series_AccF, series_GyroF;

    HandlerClass handlerClass=new HandlerClass();
    ExecutorService sGolayThreadsAss,sGolayThreadsGyro;
    public List<AccelerationData>Data=new ArrayList<>();
    List<Double> AssData =new ArrayList<>(500);
    List<Double> GyroData =new ArrayList<>(500);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sGolayThreadsAss= Executors.newFixedThreadPool(1);
        sGolayThreadsGyro= Executors.newFixedThreadPool(1);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }

        X_value=(TextView)findViewById(R.id.acceX);
        Y_value=(TextView)findViewById(R.id.acceY);
        Z_value=(TextView)findViewById(R.id.acceZ);

        Gyro1=(TextView)findViewById(R.id.gyro1);
        Gyro2=(TextView)findViewById(R.id.gyro2);
        Gyro3=(TextView)findViewById(R.id.gyro3);

        graph_Acce=(GraphView)findViewById(R.id.graphViewAcce);
        graph_Acce.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph_Acce.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        graph_Gyro=(GraphView)findViewById(R.id.graphViewGyro);
        graph_Gyro.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph_Gyro.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        graph_AcceFiltered=(GraphView)findViewById(R.id.graphViewAcceFiltered);
        graph_AcceFiltered.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph_AcceFiltered.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        graph_GyroFiltered=(GraphView)findViewById(R.id.graphViewGyroFiltered);
        graph_GyroFiltered.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph_GyroFiltered.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        // data
        series_Acc = new LineGraphSeries<DataPoint>();
        series_Acc.setTitle("Acce Normal");
        series_Acc.setColor(Color.RED);

        series_Gyro = new LineGraphSeries<DataPoint>();
        series_Gyro.setTitle("Gyro Normal");
        series_Gyro.setColor(Color.RED);

        series_AccF = new LineGraphSeries<DataPoint>();
        series_AccF.setTitle("Acce filtered");
        series_AccF.setColor(Color.GREEN);

        series_GyroF = new LineGraphSeries<DataPoint>();
        series_GyroF.setTitle("Gyro Filtered");
        series_GyroF.setColor(Color.GREEN);

        graph_Acce.getLegendRenderer().setVisible(true);
        graph_Acce.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph_Gyro.getLegendRenderer().setVisible(true);
        graph_Gyro.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph_AcceFiltered.getLegendRenderer().setVisible(true);
        graph_AcceFiltered.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph_GyroFiltered.getLegendRenderer().setVisible(true);
        graph_GyroFiltered.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        graph_Acce.addSeries(series_Acc);
        graph_Gyro.addSeries(series_Gyro);
        graph_AcceFiltered.addSeries(series_AccF);
        graph_GyroFiltered.addSeries(series_GyroF);

        Utility.sGolay=new SGolay(Utility.PolyOrder,Utility.WindowSize);

    }


    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    class HandlerClass extends Handler {
        HandlerClass() {
        }

        public void handleMessage(Message msg) {
            DataAndFilteredData dataAndFilteredData=(DataAndFilteredData ) msg.obj;
            switch (msg.what) {
                case Utility.Acc:
                    series_Acc.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.data),true,Utility.GraphSize);

                    return;
                case Utility.Gyro:
                    series_Gyro.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.data),true,Utility.GraphSize);

                    return;
                case Utility.AccFilter:
                    series_AccF.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.filteredData),true,Utility.GraphSize);

                    return;
                case Utility.GyroFilter:
                    series_GyroF.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.filteredData),true,Utility.GraphSize);

                    return;
                default:
                    return;
            }
        }
    }
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        //  mSensorManager.unregisterListener(this);
    }
    int delay=0;
    double normal;
    int indexAcc=Utility.WindowSize,indexGyro=Utility.WindowSize;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (capturingData && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
           /* delay++;
            if (delay == 1) {
                delay = 0;
            }*/
            if (delay == 0) {
                float x, y, z;
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];
                final double  normal=Math.sqrt(x * x + y * y + z * z);
                final   AccelerationData accelerationData=new AccelerationData(indexAcc,normal,0);
                normalData.add(normal);
                X_value.setText(Float.toString(x));
                Y_value.setText(Float.toString(y));
                Z_value.setText(Float.toString(z));

                if (normalData.size()>2*Utility.WindowSize+1) {
                    if(Utility.IsFilterAss){
                        final RealMatrix frameData = GetFrameData(indexAcc, Utility.WindowSize, AssData);
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(indexAcc,accelerationData.normal,0);
                        sGolayThreadsAss.execute(new Runnable() {
                            @Override
                            public void run() {
                                double filteredData = Utility.sGolay.GetFiltredData(frameData);
                                dataAndFilteredData.filteredData = filteredData;
                                accelerationData.filtered=filteredData;
                                handlerClass.obtainMessage(Utility.Acc, dataAndFilteredData).sendToTarget();
                                handlerClass.obtainMessage(Utility.AccFilter, dataAndFilteredData).sendToTarget();
                            }
                        });
                    }
                    if(Utility.IsFilterAss) {
                        indexAcc++;
                    }

                }else {
                    if(Utility.IsFilterAss){
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(indexAcc,accelerationData.normal,0);
                        handlerClass.obtainMessage(Utility.Acc, dataAndFilteredData).sendToTarget();
                    }

                }
            }
        }
        if (capturingData && sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
           /* delay++;
            if (delay == 1) {
                delay = 0;
            }*/
            if (delay == 0) {
                float x, y, z;
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];
                final double  normal=Math.sqrt(x * x + y * y + z * z);
                final   AccelerationData accelerationData=new AccelerationData(indexGyro,normal,0);

                Gyro1.setText(Float.toString(x));
                Gyro2.setText(Float.toString(y));
                Gyro3.setText(Float.toString(z));

                if (normalData.size()>2*Utility.WindowSize+1) {
                    if(Utility.IsFilterAss){
                        final RealMatrix frameData = GetFrameData(indexGyro, Utility.WindowSize, GyroData);
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(indexGyro,accelerationData.normal,0);
                        sGolayThreadsGyro.execute(new Runnable() {
                            @Override
                            public void run() {
                                double filteredData = Utility.sGolay.GetFiltredData(frameData);
                                dataAndFilteredData.filteredData = filteredData;
                                accelerationData.filtered=filteredData;
                                handlerClass.obtainMessage(Utility.Gyro, dataAndFilteredData).sendToTarget();
                                handlerClass.obtainMessage(Utility.GyroFilter, dataAndFilteredData).sendToTarget();
                            }
                        });
                    }

                    if(Utility.IsFilterGyro) {
                        indexGyro++;
                    }

                }else {
                    if(Utility.IsFilterGyro){
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(indexGyro,accelerationData.normal,0);
                        handlerClass.obtainMessage(Utility.Gyro, dataAndFilteredData).sendToTarget();
                    }

                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public RealMatrix GetFrameData(int index, int frameSize, List<Double> data){
        double[] frameData=new double[2*frameSize+1];
        for (int i=-frameSize;i<=frameSize && index+i<data.size();i++){
            frameData[i+frameSize]=data.get(index+i);
        }
        RealMatrix frameDataMatrix=new Array2DRowRealMatrix(frameData);
        return  frameDataMatrix.transpose();
    }
}
