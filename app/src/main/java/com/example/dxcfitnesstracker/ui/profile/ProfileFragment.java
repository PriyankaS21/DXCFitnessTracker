package com.example.dxcfitnesstracker.ui.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dxcfitnesstracker.data.profile.profiledata.ProfileData;
import com.example.dxcfitnesstracker.R;
import com.example.dxcfitnesstracker.model.ProfileDataModel;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<ProfileDataModel> data;
    static View.OnClickListener myOnClickListener;

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
                    stepSizeDialog();
                    break;
                case 1:
                    stepGoalDialog();
                    break;

                case 2:
                    break;

                case 3:
                    break;

                case 4:
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

        private void notification() {

        }
    }
}
