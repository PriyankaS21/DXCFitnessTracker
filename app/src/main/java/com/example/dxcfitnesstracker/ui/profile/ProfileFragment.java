package com.example.dxcfitnesstracker.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dxcfitnesstracker.data.profile.profiledata.ProfileData;
import com.example.dxcfitnesstracker.R;
import com.example.dxcfitnesstracker.model.ProfileDataModel;
import com.example.dxcfitnesstracker.ui.Database;
import com.example.dxcfitnesstracker.ui.trackSteps.MainFragment;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<ProfileDataModel> data;
    static View.OnClickListener myOnClickListener;
    public EditText age, height, weight;
    public static double bmi = 24;
    public static int ageValue, heightValue, weightValue;
    private static boolean split_active;
    int totalSteps = 0;
    String ageText, heightText, weightText;

    public final static int DEFAULT_GOAL = 1000;
    public final static float DEFAULT_STEP_SIZE = Locale.getDefault() == Locale.US ? 2.5f : 75f;
    public final static String DEFAULT_STEP_UNIT = Locale.getDefault() == Locale.US ? "ft" : "cm";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().setTitle(R.string.profile);
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myOnClickListener = new MyOnClickListener(getContext());

        recyclerView = (RecyclerView) view.findViewById(R.id.profile_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<ProfileDataModel>();
        for (int i = 0; i < ProfileData.list_string.length; i++) {
            data.add(new ProfileDataModel(
                    ProfileData.list_string[i],
                    ProfileData.drawableArray[i]
            ));
        }
        adapter = new ProfileAdapter(data);
        recyclerView.setAdapter(adapter);

    }

    private class MyOnClickListener implements View.OnClickListener {
        AlertDialog.Builder builder;
        View v;
        final SharedPreferences prefs =
                Objects.requireNonNull(getActivity()).getSharedPreferences("pedometer", Context.MODE_PRIVATE);

        private MyOnClickListener(Context context) {
        }


        @Override
        public void onClick(View v) {
            int itemPosition = recyclerView.getChildAdapterPosition(v);
            switch (itemPosition) {

                case 0:
                    getPersonalInfoDialog();
                    break;

                case 1:
                    stepSizeDialog();
                    break;
                case 2:
                    stepGoalDialog();
                    break;

                case 3:
                    getTotalStepCountDialog().show();
                    break;

                case 4:
                    break;

                case 5:
                    instructionsDialog();
                    break;
            }
        }

        void stepSizeDialog() {

            builder = new AlertDialog.Builder(getActivity());
            v = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.stepsize, null);
            final RadioGroup unit = (RadioGroup) v.findViewById(R.id.unit);
            final EditText value = (EditText) v.findViewById(R.id.value);
            unit.check(
                    prefs.getString("stepsize_unit", DEFAULT_STEP_UNIT).equals("cm") ? R.id.cm :
                            R.id.ft);
            value.setText(String.valueOf(prefs.getFloat("stepsize_value", DEFAULT_STEP_SIZE)));
            builder.setView(v);
            builder.setTitle(R.string.set_step_size);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        prefs.edit().putFloat("stepsize_value",
                                Float.valueOf(value.getText().toString()))
                                .putString("stepsize_unit",
                                        unit.getCheckedRadioButtonId() == R.id.cm ? "cm" : "ft")
                                .apply();
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        private void stepGoalDialog() {
            builder = new AlertDialog.Builder(getActivity());
            final NumberPicker np = new NumberPicker(getActivity());
            np.setMinValue(1);
            np.setMaxValue(100000);
            np.setValue(prefs.getInt("goal", 1000));
            builder.setView(np);
            builder.setTitle(R.string.set_goal);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    np.clearFocus();
                    prefs.edit().putInt("goal", np.getValue()).apply();
                    dialog.dismiss();
                    Objects.requireNonNull(getActivity()).startService(new Intent(getActivity(), SensorListener.class)
                            .putExtra("updateNotificationState", true));
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            Dialog dialog = builder.create();
            Objects.requireNonNull(dialog.getWindow()).setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();
        }


        private void getPersonalInfoDialog() {
            builder = new AlertDialog.Builder(getActivity());
            v = getActivity().getLayoutInflater().inflate(R.layout.dialog_personal_information, null);
            builder.setView(v);
            builder.setTitle(R.string.personalInfoTitle);
            builder.setCancelable(false);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    age = v.findViewById(R.id.age_edit_text);
                    height = v.findViewById(R.id.height_edit_text);
                    weight = v.findViewById(R.id.weight_edit_text);
                    ageText = age.getText().toString();
                    heightText = height.getText().toString();
                    weightText = weight.getText().toString();

                    if (ageText.isEmpty()) {
                        age.setError("Please enter the value");
                    } else if (heightText.isEmpty()) {
                        height.setError("Please enter the value");

                    } else if (weightText.isEmpty()) {
                        weight.setError("Please enter the value");
                    } else {
                        ageValue = Integer.parseInt(ageText);
                        weightValue = Integer.parseInt(weightText);
                        heightValue = Integer.parseInt(heightText);

                        Database db = Database.getInstance(getActivity());
                        boolean result = db.dataExists();
                        if (result) {
                            db.clearData();
                            db.insertPersonalInfo(weightValue, heightValue, ageValue);
                        } else {
                            db.insertPersonalInfo(weightValue, heightValue, ageValue);
                        }

                        //bmi = weightValue/Math.pow(heightValue,2);
                        // prefs.edit().putLong("bmi", Double.doubleToRawLongBits(bmi)).apply();
                    }
                    dialog.dismiss();
                }
            });
            Dialog dialog = builder.create();
            dialog.show();
        }

        private void instructionsDialog() {
            builder = new AlertDialog.Builder(getActivity());
            v = Objects.requireNonNull(getActivity()).getLayoutInflater().inflate(R.layout.instructions, null);
            builder.setView(v);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            Dialog dialog = builder.create();
            dialog.show();
        }

        //TODO implement notification after finishing the code
        private void notification() {

        }

        private Dialog getTotalStepCountDialog() {
            totalSteps = MainFragment.total_start + Math.max(MainFragment.todayOffset + MainFragment.since_boot, 0);
            final Dialog d = new Dialog(getContext());
            d.setTitle(R.string.stepCount);
            d.setContentView(R.layout.dialog_total_step_count);

            final SharedPreferences prefs =
                    getContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE);

            long split_date = prefs.getLong("split_date", -1);
            int split_steps = prefs.getInt("split_steps", totalSteps);
            ((TextView) d.findViewById(R.id.steps))
                    .setText(MainFragment.formatter.format(totalSteps - split_steps));
            float stepsize = prefs.getFloat("stepsize_value", ProfileFragment.DEFAULT_STEP_SIZE);
            float distance = (totalSteps - split_steps) * stepsize;
            if (prefs.getString("stepsize_unit", ProfileFragment.DEFAULT_STEP_UNIT).equals("cm")) {
                distance /= 100000;
                ((TextView) d.findViewById(R.id.distanceunit)).setText("km");
            } else {
                distance /= 5280;
                ((TextView) d.findViewById(R.id.distanceunit)).setText("mi");
            }
            ((TextView) d.findViewById(R.id.distance))
                    .setText(MainFragment.formatter.format(distance));
            ((TextView) d.findViewById(R.id.date)).setText(getContext().getString(R.string.since,
                    java.text.DateFormat.getDateTimeInstance().format(split_date)));

            final View started = d.findViewById(R.id.started);
            final View stopped = d.findViewById(R.id.stopped);

            split_active = split_date > 0;

            started.setVisibility(split_active ? View.VISIBLE : View.GONE);
            stopped.setVisibility(split_active ? View.GONE : View.VISIBLE);

            final Button startstop = (Button) d.findViewById(R.id.start);
            startstop.setText(split_active ? R.string.stop : R.string.start);
            startstop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (!split_active) {
                        prefs.edit().putLong("split_date", System.currentTimeMillis())
                                .putInt("split_steps", totalSteps).apply();
                        split_active = true;
                        d.dismiss();
                    } else {
                        started.setVisibility(View.GONE);
                        stopped.setVisibility(View.VISIBLE);
                        prefs.edit().remove("split_date").remove("split_steps").apply();
                        split_active = false;
                    }
                    startstop.setText(split_active ? R.string.stop : R.string.start);
                }
            });

            d.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    d.dismiss();
                }
            });

            return d;
        }

    }

}
