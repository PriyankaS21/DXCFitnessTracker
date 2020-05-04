package com.example.dxcfitnesstracker.util;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.M)
public class API23Wrapper {

    public static void setAlarmWhileIdle(AlarmManager am, int type, long time,
                                         PendingIntent intent) {
        am.setAndAllowWhileIdle(type, time, intent);
    }
}
