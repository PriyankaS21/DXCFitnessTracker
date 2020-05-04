package com.example.dxcfitnesstracker.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dxcfitnesstracker.R;
import com.example.dxcfitnesstracker.model.ProfileDataModel;


import java.util.ArrayList;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileViewHolder> {

    private ArrayList<ProfileDataModel> dataSet;

    public ProfileAdapter(ArrayList<ProfileDataModel> data) {
        this.dataSet = data;

    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_card_items, parent, false);

        ProfileViewHolder myViewHolder = new ProfileViewHolder(view);
        view.setOnClickListener(ProfileFragment.myOnClickListener);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        TextView textViewName = holder.textViewText;
        ImageView imageView = holder.imageViewIcon;

        textViewName.setText(dataSet.get(position).getListText());
        imageView.setImageResource(dataSet.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}