package com.example.dxcfitnesstracker.ui.trackSteps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.dxcfitnesstracker.BuildConfig;
import com.example.dxcfitnesstracker.R;
import com.example.dxcfitnesstracker.charts.PieChart;
import com.example.dxcfitnesstracker.model.PieModel;
import com.example.dxcfitnesstracker.ui.Database;
import com.example.dxcfitnesstracker.ui.profile.ProfileFragment;
import com.example.dxcfitnesstracker.util.Logger;
import com.example.dxcfitnesstracker.util.Util;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

public class MainFragment extends Fragment implements SensorEventListener {

    private PieChart pg;
    TextView stepsView, totalView, averageView, today_goal_value, calorie;
    private PieModel sliceGoal, sliceCurrent;
    public static int todayOffset, total_start, goal, since_boot, total_days;
    public final static NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
    public static boolean showSteps = true;
    int steps_today;
    public static double total_calorie_burnt = 0.0;
    float distance_today, distance_total;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(getActivity()).setTitle(R.string.track_step_fragment);
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_main, null);
        stepsView = v.findViewById(R.id.steps);
        totalView = v.findViewById(R.id.total);
        averageView = v.findViewById(R.id.average);
        today_goal_value = v.findViewById(R.id.today_goal_value);
        calorie = v.findViewById(R.id.calorie);

        pg = v.findViewById(R.id.graph);

        // slice for the steps taken today
        sliceCurrent = new PieModel("", 0, Color.parseColor("#99CC00"));
        pg.addPieSlice(sliceCurrent);

        // slice for the "missing" steps until reaching the goal
        sliceGoal = new PieModel("", ProfileFragment.DEFAULT_GOAL, Color.parseColor("#CC0000"));
        pg.addPieSlice(sliceGoal);

        pg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showSteps = !showSteps;
                stepsDistanceChanged();
            }
        });

        pg.setDrawValueInPie(false);
        pg.setUsePieRotation(true);
        pg.startAnimation();
        calculateCalorieBurnt();
        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);

        Database db = Database.getInstance(getActivity());

        if (BuildConfig.DEBUG) db.logState();
        // read today's offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        goal = prefs.getInt("goal", ProfileFragment.DEFAULT_GOAL);
        today_goal_value.setText(goal + " steps");

        since_boot = db.getCurrentSteps();
        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        //register a sensorlistener to live update the UI if a step is taken
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) {
            new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
                    .setMessage(R.string.no_sensor_explain)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialogInterface) {
                            getActivity().finish();
                        }
                    }).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        } else {
            sm.registerListener((SensorEventListener) this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        db.close();

        stepsDistanceChanged();
        calculateCalorieBurnt();
    }

    /**
     * Call this method if the Fragment should update the "steps"/"km" text in
     * the pie graph as well as the pie and the bars graphs.
     */
    private void stepsDistanceChanged() {
        if (showSteps) {
            ((TextView) getView().findViewById(R.id.unit)).setText(getString(R.string.steps));
        } else {
            String unit = getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                    .getString("stepsize_unit", ProfileFragment.DEFAULT_STEP_UNIT);
            if (unit.equals("cm")) {
                unit = "km";
            } else {
                unit = "mi";
            }
            ((TextView) getView().findViewById(R.id.unit)).setText(unit);
        }
        updatePie();
        calculateCalorieBurnt();


    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener((SensorEventListener) this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Logger.log(e);
        }
        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
        db.close();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (BuildConfig.DEBUG) Logger.log(
                "UI - sensorChanged | todayOffset: " + todayOffset + " since boot: " +
                        event.values[0]);
        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            Database db = Database.getInstance(getActivity());
            db.insertNewDay(Util.getToday(), (int) event.values[0], total_calorie_burnt);
            db.close();
        }
        since_boot = (int) event.values[0];
        updatePie();
        calculateCalorieBurnt();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void updatePie() {
        if (BuildConfig.DEBUG) Logger.log("UI - update steps: " + since_boot);
        // todayOffset might still be Integer.MIN_VALUE on first start
        steps_today = Math.max(todayOffset + since_boot, 0);
        sliceCurrent.setValue(steps_today);
        if (goal - steps_today > 0) {
            // goal not reached yet
            if (pg.getData().size() == 1) {
                // can happen if the goal value was changed: old goal value was
                // reached but now there are some steps missing for the new goal
                pg.addPieSlice(sliceGoal);
            }
            sliceGoal.setValue(goal - steps_today);
        } else {
            // goal reached
            pg.clearChart();
            pg.addPieSlice(sliceCurrent);
        }
        pg.update();
        if (showSteps) {
            stepsView.setText(formatter.format(steps_today));
            totalView.setText(formatter.format(total_start + steps_today));
            averageView.setText(formatter.format((total_start + steps_today) / total_days));
        } else {
            // update only every 10 steps when displaying distance
            SharedPreferences prefs =
                    getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            float stepsize = prefs.getFloat("stepsize_value", ProfileFragment.DEFAULT_STEP_SIZE);
            distance_today = steps_today * stepsize;
            distance_total = (total_start + steps_today) * stepsize;
            if (prefs.getString("stepsize_unit", ProfileFragment.DEFAULT_STEP_UNIT)
                    .equals("cm")) {
                distance_today /= 100000;
                distance_total /= 100000;
            } else {
                distance_today /= 5280;
                distance_total /= 5280;
            }
            stepsView.setText(formatter.format(distance_today));
            totalView.setText(formatter.format(distance_total));
            averageView.setText(formatter.format(distance_total / total_days));
        }
    }

    private void calculateCalorieBurnt() {
        double calBurnt = (steps_today * 0.04);
        //String cal = Double.toString(calBurnt);
        String cal = String.format("%.2f", calBurnt);
        calorie.setText(cal);
        Database db = Database.getInstance(getActivity());
        int weight = db.getWeight();
        int height = db.getHeight();


        total_calorie_burnt = (weight * 0.035) + (Math.pow((distance_total / total_days), 2) / (height)) * 0.029 * weight;

        db.saveCurrentCalorie(total_calorie_burnt);


    }
}
