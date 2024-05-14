package com.alexk.clientmap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexk.clientmap.Client;
import com.alexk.clientmap.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.RequestBody;

public class ClientNamesAdapter extends RecyclerView.Adapter<ClientNamesAdapter.ViewHolder> {
    Context context;
    Client client;
    private final ArrayList<String> mClientNames;

    public ClientNamesAdapter(ArrayList<String> clientNames, Client client, Context context) {
        this.context = context;
        this.client = client;
        this.mClientNames = clientNames;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.name_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String clientName = mClientNames.get(holder.getBindingAdapterPosition());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_delete_only, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(android.view.MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                if (client != null) {
                                        String[] names = client.getNames();
                                        String[] newArray = new String[names.length - 1];
                                        int index = 0;
                                        for (int i = 0; i < names.length; i++) {
                                            if (!names[i].equals(clientName)) {
                                                newArray[index] = names[i];
                                                index++;
                                            }
                                        }
                                        client.setNames(newArray);
                                    removeItem(holder.getBindingAdapterPosition(),client.getId(),Helper.ClientToRequestBody(client));
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
                return true;
            }
        });
        holder.bind(clientName);
    }

    @Override
    public int getItemCount() {
        if (mClientNames == null) return 0;
        return mClientNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mClientNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mClientNameTextView = itemView.findViewById(R.id.name);
            if (Helper.isDarkModeEnabled(context)){
                mClientNameTextView.setTextColor(Color.WHITE);
            }
        }

        public void bind(String clientName) {
            mClientNameTextView.setText(clientName);
        }
    }
    public void removeItem(int position, String id, JSONObject requestBody) {
        Helper.Request(Helper.EDIT_ENDPOINT,id,requestBody,context,new Helper.OnRequestSuccessListener(){
            @Override
            public void onSuccess(int statusCode) {
                mClientNames.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
                Helper.Toast("Επιτυχής Διαγραφή!",context,null);
            }

            @Override
            public void onError(int statusCode) {
                Log.d("STATUS",Integer.toString(statusCode));
                if (statusCode == 200){
                    mClientNames.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    Helper.Toast("Επιτυχής Διαγραφή!",context,null);
                }
                else {
                    Helper.Alert("Προσοχή", "Κάτι πήγε στραβά!", context,null);
                }
            }
        });
    }
}