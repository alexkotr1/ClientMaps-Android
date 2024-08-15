package com.alexk.clientmap;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    TextView nameView, placeView;
    ImageView image_available;
    Button callButton;
    Button addContact;
    Button linkContact;

    public MyViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        nameView = itemView.findViewById(R.id.name);
        placeView = itemView.findViewById(R.id.place);
        callButton = itemView.findViewById(R.id.phone);
        addContact = itemView.findViewById(R.id.saveContact);
        linkContact = itemView.findViewById(R.id.linkContact);
        image_available = itemView.findViewById(R.id.imageView);
        if (Helper.isDarkModeEnabled(context)){
            nameView.setTextColor(Color.WHITE);
            placeView.setTextColor(Color.WHITE);
        }
    }
}
