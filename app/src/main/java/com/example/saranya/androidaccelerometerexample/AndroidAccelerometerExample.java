package com.example.saranya.androidaccelerometerexample;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static android.content.ContentValues.TAG;

public class AndroidAccelerometerExample extends Activity implements SensorEventListener {

    private float lastX, lastY, lastZ;

    private static final char CSV_DELIM = ',';
    private static final String CSV_HEADER = "X Axis,Y Axis,Z Axis";
    private PrintWriter printWriter;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float vibrateThreshold = 0;

    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ;
    Button btn = null;

    public Vibrator v;
    StringBuffer sb = new StringBuffer();

    File accelerometerDataFile = null;
    File linearAcceclerationDataFile = null;


   /* public AndroidAccelerometerExample()
    {
        dataFile = new File(getExternalCacheDir(), "linearAcceleration.csv");
        try
        {
            printWriter =
                    new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));

            printWriter.println(CSV_HEADER);
        }
        catch (IOException e)
        {

        }
    }*/

    private void writeSensorEvent(PrintWriter printWriter,
                                  float x,
                                  float y,
                                  float z)
    {
        if (printWriter != null)
        {
            StringBuffer sb = new StringBuffer()
                    .append(x).append(CSV_DELIM)
                    .append(y).append(CSV_DELIM)
                    .append(z).append(CSV_DELIM);

            printWriter.println(sb.toString());
            if (printWriter.checkError())
            {

            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;

        } else {
            // fai! we dont have an accelerometer!
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

    }

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);
        btn = (Button) findViewById(R.id.locationBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AndroidAccelerometerExample.this,CurrentLocationActivity.class));
            }
        });

    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        accelerometerDataFile =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "accelerometer.csv");
        linearAcceclerationDataFile = new File(getExternalCacheDir(), "linearAcceleration.csv");
        try
        {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(accelerometerDataFile)));

            // NOTE:- TO OPEN FILE IN APPEND MODE
            //printWriter = new PrintWriter(new BufferedWriter(new FileWriter(accelerometerDataFile,true)));
            printWriter.println(CSV_HEADER);

            printWriter.println(sb.toString());
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not open CSV file(s)", e);
        }

        if (printWriter != null)
        {
            printWriter.close();
        }

        if (printWriter.checkError())
        {
            Log.e(TAG, "Error closing writer");
        }
        sensorManager.unregisterListener(this);
        this.stop();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        sb = sb
                .append(lastX).append(CSV_DELIM)
                .append(lastY).append(CSV_DELIM)
                .append(lastZ).append(CSV_DELIM).append("\n");
       // writeSensorEvent(printWriter,deltaX,deltaY,deltaZ);
        /*String line = Float.toString(deltaX)+","+Float.toString(deltaY)+","+Float.toString(deltaZ)+"\n";
        File file = null;
        FileOutputStream fos = null;
        try
        {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"vib.csv");
            fos = new FileOutputStream(file);
            fos.write(line.getBytes());
            fos.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }*/
        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change    of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        // if the change is below 2, it is just plain noise
        if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if ((deltaZ > vibrateThreshold) ||(deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)){
            v.vibrate(50);
        }
    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(Float.toString(deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }

    public void stop()
    {
        if (printWriter != null)
        {
            printWriter.close();
        }

        if (printWriter.checkError())
        {

        }
    }
}

