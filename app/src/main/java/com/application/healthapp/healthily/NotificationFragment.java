package com.application.healthapp.healthily;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment implements SensorEventListener, StepListener  {

    private TextView TvSteps;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;
    private DecoView decoView;
    private SeriesItem seriesItem;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private StorageReference storageReference;
    private String weight;
    private String height;
    private String name;
    private Button cal;
    private TextView tvDistance;
    private TextView tvCaloriesBurned;
    private TextView tvCalpriesBurnedPerMile;
    private double newSteps;



    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
        //capture button clicks


    }


    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //get weight and height for calorie calculation
        tvDistance = getView().findViewById(R.id.textView3);
        tvCaloriesBurned = getView().findViewById(R.id.textView5);
        tvCalpriesBurnedPerMile = getView().findViewById(R.id.textView6);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d("TAG", document.getString("name")); //Print the name
                        name = document.get("name").toString(); //Print name
                        String v = document.get("goal").toString(); //Print goal
                        weight = document.get("weight").toString(); //Print Weight
                        height = document.get("height").toString(); //Print Height

                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }

            }
        });


        decoView = (DecoView) getView().findViewById(R.id.dynamicArcView);
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
        final TextView textPercentage = (TextView) getView().findViewById(R.id.textView4);
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

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
//        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) getView().findViewById(R.id.tv_steps);
        Button BtnStart = (Button) getView().findViewById(R.id.btn_start);
        Button BtnStop = (Button) getView().findViewById(R.id.btn_stop);

        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                numSteps = 0;
                sensorManager.registerListener(NotificationFragment.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

            }
        });

        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(NotificationFragment.this);
                calculator();
            }

            private void calculator() {
                double walkingFactor;
                double multiplication_factor;
                double CaloriesBurnedPerMile;
                double strip;
                double stepCountMile; // step/mile
                double conversationFactor;
                double CaloriesBurned;

                DecimalFormat formatter = new DecimalFormat("##.##");
                double distance;
                strip = Integer.parseInt(height) * 0.415;
                distance = ( newSteps * strip) / 100000;

                walkingFactor = distance/0.57;
                CaloriesBurnedPerMile = walkingFactor * (Double.parseDouble(weight) * 2.2); //weight 2.2
                strip = Double.parseDouble(height) * 0.415;  //height
                stepCountMile = 160934.4 / strip;
                conversationFactor = CaloriesBurnedPerMile / stepCountMile;
                CaloriesBurned = conversationFactor*100;
                CaloriesBurnedPerMile = CaloriesBurnedPerMile/10;

                //System.out.println("Calories burned:" + formatter.format(CaloriesBurned) + " cal");
                  //numsteps
                formatter.format(distance);
                formatter.format(CaloriesBurned);
                formatter.format(CaloriesBurnedPerMile);
                //System.out.println("Distance: " + formatter.format(distance) + " km");
                Log.e("tag", "calculator: " + formatter.format( distance));
                Log.e("tag", "calcu " + CaloriesBurned);
                Log.e("tag", "abcd: " + CaloriesBurnedPerMile);

                tvDistance.setText("Distance: " +formatter.format(distance) + " km");
                tvCaloriesBurned.setText(formatter.format(CaloriesBurned) + " Calories Burned");
                tvCalpriesBurnedPerMile.setText(formatter.format(CaloriesBurnedPerMile) + " Cal Burned Per Mile");


            }
        });



    }




    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
        newSteps = numSteps;


    }
}
