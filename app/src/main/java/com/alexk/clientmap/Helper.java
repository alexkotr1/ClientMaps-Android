package com.alexk.clientmap;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.alexk.clientmap.AddController.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class Helper {
    public static String HOST = "http://159.223.16.89:3000/";
    public static String ADD_ENDPOINT = HOST + "add/";
    public static String EDIT_ENDPOINT = HOST + "edit/";
    public static String DELETE_ENDPOINT = HOST + "delete/";
    public static String DATA_ENDPOINT = HOST + "data/";
    public static String PASSWORD = "6NaUPgrWuaZXVqYw2KQP";

    public static void Alert(String title, String message, Context context, Class<?> redirectClass) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (redirectClass != null) {
                            Intent intent = new Intent(context, redirectClass);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void Toast(String message, Context context, Class<?> redirectClass) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        if (redirectClass != null) {
            Intent intent = new Intent(context, redirectClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static JSONObject ClientToRequestBody(Client client) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray names = new JSONArray(client.getNames());
            jsonObject.put("name", client.getName())
                    .put("phone", client.getPhone())
                    .put("comments", client.getComments())
                    .put("latitude", client.getLatitude())
                    .put("longitude", client.getLongitude())
                    .put("id", client.getId())
                    .put("place", client.getPlace())
                    .put("names", names);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static boolean isDarkModeEnabled(Context context){
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
    public static boolean hasInternetAccess(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void uploadClient(List<Client> clients, Context context){
        for(Client client : clients){
            Request(ADD_ENDPOINT, null, ClientToRequestBody(client), context, new OnRequestSuccessListener() {
                @Override
                public void onSuccess(int statusCode) {
                    client.deleteLocalClient(context);
                }

                @Override
                public void onError(int statusCode) {
                    if (statusCode == 409 || statusCode == 200){
                        client.deleteLocalClient(context);
                    }
                }
            });
        }
    }
    public static void cacheClients(List<Client> clients, Context context) {
        if (clients.size() == 0) return;
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("clients.ser", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(clients);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Client> retrieveCache(Context context) {
        List<Client> list = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput("clients.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            list = (List<Client>) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public static void Request(String url, String id,JSONObject requestBody, Context context, OnRequestSuccessListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url + (id == null || id.isEmpty() ? "": (id + "/"))  + PASSWORD , requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            listener.onSuccess(200);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = -1;
                        Log.d("VOLLEY ERROR",Boolean.toString(error instanceof TimeoutError));
                        if (error instanceof TimeoutError){
                            statusCode = 400;
                        }
                        if (error != null && error.networkResponse != null){
                            statusCode = error.networkResponse.statusCode;
                        }
                        if (listener != null) {
                            switch (statusCode) {
                                case 200:
                                case -1:
                                    listener.onSuccess(200);
                                    break;
                                case 409:
                                    listener.onError(409);
                                    break;
                                default:
                                    listener.onError(statusCode);
                                    break;
                            }
                        }
                    }
                });
        Volley.newRequestQueue(context).add(request);
    }
    public interface OnRequestSuccessListener {
        void onSuccess(int statusCode);
        void onError(int statusCode);
    }

}
