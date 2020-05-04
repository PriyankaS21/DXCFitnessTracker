package com.example.dxcfitnesstracker.ui.reports;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dxcfitnesstracker.R;
import com.example.dxcfitnesstracker.charts.BarChart;
import com.example.dxcfitnesstracker.model.BarModel;
import com.example.dxcfitnesstracker.ui.Database;
import com.example.dxcfitnesstracker.ui.Dialog_Statistics;
import com.example.dxcfitnesstracker.ui.profile.ProfileFragment;
import com.example.dxcfitnesstracker.ui.trackSteps.MainFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsFragment extends Fragment {
    BarChart barChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.track_reports);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.bargraph);
        updateBars();
    }

    /**
     * Updates the bar graph to show the steps/distance of the last week. Should
     * be called when switching from step count to distance.
     */
    private void updateBars() {
        SimpleDateFormat df = new SimpleDateFormat("E", Locale.getDefault());
        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        if (barChart.getData().size() > 0) barChart.clearChart();
        int steps;
        float distance, stepsize = ProfileFragment.DEFAULT_STEP_SIZE;
        boolean stepsize_cm = true;
        if (!MainFragment.showSteps) {
            // load some more settings if distance is needed
            stepsize = prefs.getFloat("stepsize_value", ProfileFragment.DEFAULT_STEP_SIZE);
            stepsize_cm = prefs.getString("stepsize_unit", ProfileFragment.DEFAULT_STEP_UNIT)
                    .equals("cm");
        }
        barChart.setShowDecimal(!MainFragment.showSteps); // show decimal in distance view only
        BarModel bm;
        Database db = Database.getInstance(getActivity());
        List<Pair<Long, Integer>> last = db.getLastEntries(8);
        db.close();
        for (int i = last.size() - 1; i > 0; i--) {
            Pair<Long, Integer> current = last.get(i);
            steps = current.second;
            if (steps > 0) {
                bm = new BarModel(df.format(new Date(current.first)), 0,
                        steps > MainFragment.goal ? Color.parseColor("#99CC00") : Color.parseColor("#0099cc"));
                if (MainFragment.showSteps) {
                    bm.setValue(steps);
                } else {
                    distance = steps * stepsize;
                    if (stepsize_cm) {
                        distance /= 100000;
                    } else {
                        distance /= 5280;
                    }
                    distance = Math.round(distance * 1000) / 1000f; // 3 decimals
                    bm.setValue(distance);
                }
                barChart.addBar(bm);
            }
        }
        if (barChart.getData().size() > 0) {
            barChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Dialog_Statistics.getDialog(getActivity(), MainFragment.since_boot).show();
                }
            });
            barChart.startAnimation();
        } else {
            barChart.setVisibility(View.GONE);
        }
    }
}
