package com.example.android.fitnesstracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private boolean isSensorPresent = false;
    private int stepCount = 0;

    private TextView tvSteps, tvDistance, tvCalories;
    private float strideLength = 0.78f; // average stride in meters
    private float weight = 60f; // in kg

    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSteps = findViewById(R.id.tv_steps);
        tvDistance = findViewById(R.id.tv_distance);
        tvCalories = findViewById(R.id.tv_calories);
        pieChart = findViewById(R.id.pieChart); // make sure it's in your XML

        // Permission check for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1001);
            }
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            isSensorPresent = true;
        } else {
            Toast.makeText(this, "Step Counter Sensor not available!", Toast.LENGTH_LONG).show();
        }

        setupPieChart(0, 0f, 0f); // initial empty chart
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorPresent) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        stepCount = (int) event.values[0];
        tvSteps.setText("Steps: " + stepCount);

        float distanceInMeters = stepCount * strideLength;
        float distanceInKm = distanceInMeters / 1000f;
        tvDistance.setText(String.format("Distance: %.2f km", distanceInKm));

        float caloriesBurned = stepCount * 0.04f;
        tvCalories.setText(String.format("Calories: %.1f kcal", caloriesBurned));

        setupPieChart(stepCount, distanceInKm, caloriesBurned);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void setupPieChart(int steps, float distance, float calories) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(steps, "Steps"));
        entries.add(new PieEntry(distance, "Distance (km)"));
        entries.add(new PieEntry(calories, "Calories"));

        PieDataSet dataSet = new PieDataSet(entries, "Fitness Stats");
        dataSet.setColors(Color.BLUE, Color.GREEN, Color.MAGENTA);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // refresh chart
    }
}
