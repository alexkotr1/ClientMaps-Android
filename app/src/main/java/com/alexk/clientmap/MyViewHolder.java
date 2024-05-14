package com.alexk.clientmap;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView nameView,phoneView, placeView;

    public MyViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        nameView = itemView.findViewById(R.id.name);
        phoneView = itemView.findViewById(R.id.phone);
        placeView = itemView.findViewById(R.id.place);
        if (Helper.isDarkModeEnabled(context)){
            nameView.setTextColor(Color.WHITE);
            phoneView.setTextColor(Color.WHITE);
            placeView.setTextColor(Color.WHITE);
        }
    }
}
