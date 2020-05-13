package com.example.dxcfitnesstracker.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.dxcfitnesstracker.BuildConfig;
import com.example.dxcfitnesstracker.ui.trackSteps.MainFragment;
import com.example.dxcfitnesstracker.util.Logger;
import com.example.dxcfitnesstracker.util.Util;


public class ShutdownRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Logger.log("shutting down");

        context.startService(new Intent(context, SensorListener.class));

        // if the user used a root script for shutdown, the DEVICE_SHUTDOWN
        // broadcast might not be send. Therefore, the app will check this
        // setting on the next boot and displays an error message if it's not
        // set to true
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                .putBoolean("correctShutdown", true).apply();

        Database db = Database.getInstance(context);
        // if it's already a new day, add the temp. steps to the last one
        if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
            int steps = db.getCurrentSteps();
            double calorie = db.getCurrentCalorie();
            db.insertNewDay(Util.getToday(), steps, calorie);
        } else {
            db.addToLastEntry(db.getCurrentSteps(), db.getCurrentCalorie());
        }
        // current steps will be reset on boot @see BootReceiver
        db.close();
    }

}
