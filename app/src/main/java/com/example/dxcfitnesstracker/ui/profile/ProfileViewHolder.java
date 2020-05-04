package com.example.dxcfitnesstracker.ui.profile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dxcfitnesstracker.R;

public class ProfileViewHolder extends RecyclerView.ViewHolder {
    TextView textViewText;
    ImageView imageViewIcon;


    public ProfileViewHolder(@NonNull View itemView) {
        super(itemView);
        this.textViewText = (TextView) itemView.findViewById(R.id.profile_list_text);
        this.imageViewIcon = (ImageView) itemView.findViewById(R.id.profile_list_icon);
    }
}
