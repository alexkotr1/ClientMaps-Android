package com.alexk.clientmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    Context context;
    List<Client> clients;
    private static List<Client> filteredClients;
    public MyAdapter(Context context, List<Client> items) {
        this.context = context;
        this.clients = items;
        filteredClients = new ArrayList<>(items);
    }
    public void setContext(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.client_view,parent,false),context);
    }
    public void filter(String text) {
        filteredClients.clear();
        if (text.isEmpty()) {
            filteredClients.addAll(clients);
        } else {
            text = text.toLowerCase();
            for (Client client : clients) {
                if (client.getName().toLowerCase().contains(text)) {
                    filteredClients.add(client);
                }
                else {
                    String[] names = client.getNames();
                    for (String s : names){
                        if (s.toLowerCase().contains(text.toLowerCase())){
                            filteredClients.add(client);
                        }
                    }
                }
            }
        }
        if (filteredClients.isEmpty()) {
        }
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (filteredClients.size() == 0) {
            return;
        }
        final Client client = filteredClients.get(position);
        holder.nameView.setText(client.getName());
        holder.phoneView.setText(client.getPhone());
        holder.placeView.setText(client.getPlace());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try {
                    String uri = "geo:" + client.getLatitude() + "," + client.getLongitude() + "?q=" + client.getLatitude() + "," + client.getLongitude() + "(" + client.getName() + ")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.google.android.apps.maps");
                    context.startActivity(intent);
                } catch (Exception e){
                    e.printStackTrace();
                   if (context instanceof Activity){
                       Activity activity = (Activity) context;
                       if (!activity.isFinishing()){
                           Helper.Alert("Προσοχή","Η εφαρμογή Google Maps δεν είναι εγκατεστημένη!",context,null);
                       }
                   }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                if (client.has_image()){
                    popupMenu.getMenuInflater().inflate(R.menu.popup_menu_w_view, popupMenu.getMenu());
                }
                else {
                    popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(android.view.MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.view:
                                ImageDownloader.downloadImage(context, Helper.DOWNLOAD_ENDPOINT + Helper.PASSWORD + '/' + client.getId(),  new ImageDownloader.OnImageDownloadListener() {
                                    @Override
                                    public void onImageDownloaded(Bitmap bitmap) {
                                        Intent intent = new Intent(context, FullScreenImageActivity.class);
                                        intent.putExtra("image", bitmap);
                                        context.startActivity(intent);
                                    }

                                    @Override
                                    public void onError(VolleyError error) {
                                        // Handle the error
                                        Log.e("MainActivity", "Image download failed: " + error.getMessage());
                                    }
                                });

                                return true;
                            case R.id.edit:
                                Intent intent = new Intent(context, EditController.class);
                                intent.putExtra("name", client.getName());
                                intent.putExtra("phone", client.getPhone());
                                intent.putExtra("comments", client.getComments());
                                intent.putExtra("latitude", client.getLatitude());
                                intent.putExtra("longitude", client.getLongitude());
                                intent.putExtra("id",client.getId());
                                intent.putExtra("names",client.getNames());
                                intent.putExtra("place",client.getPlace());
                                intent.putExtra("has_image",client.has_image());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                return true;
                            case R.id.delete:
                                removeItem(holder.getBindingAdapterPosition(),client.getId());
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
    }

    @Override
    public int getItemCount() {
        if (filteredClients.isEmpty()){
            return 0;
        }
        return filteredClients.size();
    }

    public void removeItem(int position, String id) {
        Helper.Request(Helper.DELETE_ENDPOINT,id,null,context,new Helper.OnRequestSuccessListener(){
            @Override
            public void onSuccess(int statusCode, String response) {
                clients.remove(position);
                filteredClients.remove(position);
                notifyItemRemoved(position);
                Helper.Toast("Επιτυχής Διαγραφή!",context,null);
            }

            @Override
            public void onError(int statusCode) {
                if (statusCode == 200){
                    clients.remove(position);
                    filteredClients.remove(position);
                    notifyItemRemoved(position);
                    Helper.Toast("Επιτυχής Διαγραφή!",context,null);
                }
                else {
                    Helper.Alert("Προσοχή", "Κάτι πήγε στραβά!", context,null);
                }
            }
        });
    }
}
