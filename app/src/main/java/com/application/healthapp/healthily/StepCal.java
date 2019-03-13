package com.application.healthapp.healthily;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;


public class StepCal extends AppCompatActivity implements SensorEventListener, StepListener {
    private TextView textView;
    private TextView TvSteps;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;
    private DecoView decoView;
    private SeriesItem seriesItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_cal);
        //decoview health circle
        decoView = (DecoView) findViewById(R.id.dynamicArcView);
        seriesItem = new SeriesItem.Builder(Color.parseColor("#FFE2E2E2"))
                .setRange(0, 5000, 0)
                .build();

        int backIndex = decoView.addSeries(seriesItem);
        final SeriesItem seriesItem = new SeriesItem.Builder(Color.parseColor("#FFFF8800"))
                .setRange(0, 5000, 0)
                .setInitialVisibility(false)
                .build();

        int series1Index = decoView.addSeries(seriesItem);
        //get step count value and pass it to seriesItem
        //This is listener which changeg decoview according the changes in step count
        final TextView textPercentage = (TextView) findViewById(R.id.textView4);
        seriesItem.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                float percentFilled = ((currentPosition - seriesItem.getMinValue()) / (seriesItem.getMaxValue() - seriesItem.getMinValue()));
                textPercentage.setText(String.format("%.0f%% Steps", percentFilled * 100f));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

        decoView.addEvent(new DecoEvent.Builder(5000)
                .setIndex(backIndex)
                .build());

        decoView.addEvent(new DecoEvent.Builder(16.3f)
                .setIndex(series1Index)
                .setDelay(5000)
                .build());

        decoView.addEvent(new DecoEvent.Builder(30f)
                .setIndex(series1Index)
                .setDelay(10000)
                .build());

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        Button BtnStart = (Button) findViewById(R.id.btn_start);
        Button BtnStop = (Button) findViewById(R.id.btn_stop);

        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                numSteps = 0;
                sensorManager.registerListener(StepCal.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

            }
        });


        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(StepCal.this);

            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }
}

