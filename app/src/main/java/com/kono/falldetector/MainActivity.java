package com.kono.falldetector;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    float[] logx = new float[35];
    float[] logy = new float[35];
    float[] logz = new float[35];
    int counter = 0;
    int index = 0;
    float distance = 0;
    private Sensor sensor;
    private Sensor pSensor;
    private TextView tw_x;
    private TextView tw_y;
    private TextView tw_z;
    private TextView tw_distance;
    private Button resetButton;
    private TextView tw_isFalling;
    private SensorManager sensorManager;
    private boolean isStationary = false;
    private boolean maybeFall = false;
    private boolean fallhashappened = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager =
                (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        pSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        tw_x = findViewById(R.id.main_x_view);
        tw_y = findViewById(R.id.main_y_view);
        tw_z = findViewById(R.id.main_z_view);
        tw_distance = findViewById(R.id.main_distance_view);
        resetButton = findViewById(R.id.ResetButton);
        tw_isFalling = findViewById(R.id.main_tw_isFalling);
        tw_isFalling.setBackgroundColor(Color.GREEN);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStationary = false;
                tw_isFalling.setBackgroundColor(Color.GREEN);
                tw_isFalling.setText("Checking");
                maybeFall = false;
                fallhashappened = false;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, sensor,
                                       SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, pSensor,
                                       SensorManager.SENSOR_DELAY_NORMAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(fallhashappened) {
            return;
        }
        if(event.sensor == pSensor) {
            this.distance = event.values[0];
            //only for debugging
            tw_distance.setText(String.valueOf(distance));
            return;
        }
        logx[index] = event.values[0];
        logy[index] = event.values[1];
        logz[index] = event.values[2];
        if(!maybeFall) {
            fallDetection();
        }
        //only for debugging
        tw_x.setText(String.valueOf(event.values[0]));
        tw_y.setText(String.valueOf(event.values[1]));
        tw_z.setText(String.valueOf(event.values[2]));

        index = ++index == 35 ? 0 : index;
        if(maybeFall) {
            if(counter < 150) {
                counter++;
                return;
            } else {
                if(isStationary() & distance == 0.0) {
                    fallhashappened = true;
                    tw_isFalling.setText("Fall has happened");
                    tw_isFalling.setBackgroundColor(Color.RED);
                } else {
                    maybeFall = false;
                    tw_isFalling.setText("Checking");
                    tw_isFalling.setBackgroundColor(Color.GREEN);
                    counter = 0;
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean isStationary() {
        float sumx = 0;
        float sumy = 0;
        float sumz = 0;
        for(int i = 0; i < 34; i++) {
            sumx += logx[i];
            sumy += logy[i];
            sumz += logz[i];
        }
        if(Math.abs(sumx) / 35 - Math.abs(logx[index]) < 0.5 & Math.abs(sumy) / 35 - Math.abs(logy[index]) < 0.5 & Math.abs(sumz) / 35 - Math.abs(logz[index]) < 0.5) {
            tw_isFalling.setBackgroundColor(Color.YELLOW);
            tw_isFalling.setText("Stationary");
            return true;
        }
        return false;
    }

    private boolean notMoving() {
        int lastElement = index == 0 ? 34 : index - 1;
        if(Math.abs(logx[index] - logx[lastElement]) < 1 & (Math.abs(logy[index]) - logy[lastElement]) < 1) {
            return true;
        }
        return false;
    }

    private void fallDetection() {
        int nextElement = index == 34 ? 0 : index + 1;
        if(Math.abs(logx[nextElement] - logx[index]) > 8 & Math.abs(logy[nextElement]) - logy[index] > 8 | Math.abs(logy[nextElement] - logy[index]) > 8 & Math.abs(logz[nextElement]) - logz[index] > 8) {
            maybeFall = true;
            tw_isFalling.setBackgroundColor(Color.YELLOW);
            tw_isFalling.setText("Fall Might Happened");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}