package com.example.dxcfitnesstracker.widget;

import android.content.Intent;

import com.example.dxcfitnesstracker.R;
import com.example.dxcfitnesstracker.ui.Database;
import com.example.dxcfitnesstracker.ui.MainActivity;
import com.example.dxcfitnesstracker.ui.trackSteps.MainFragment;
import com.example.dxcfitnesstracker.util.Util;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;


public class DashClock extends DashClockExtension {

    @Override
    protected void onUpdateData(int reason) {
        ExtensionData data = new ExtensionData();
        Database db = Database.getInstance(this);
        int steps = Math.max(db.getCurrentSteps() + db.getSteps(Util.getToday()), 0);
        data.visible(true).status(MainFragment.formatter.format(steps))
                .icon(R.drawable.ic_dashclock)
                .clickIntent(new Intent(DashClock.this, MainActivity.class));
        db.close();
        publishUpdate(data);
    }

}
