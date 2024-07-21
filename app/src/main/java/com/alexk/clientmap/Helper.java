package com.alexk.clientmap;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
public class Helper extends AppCompatActivity {
    public static String HOST = "";
    public static String ADD_ENDPOINT = HOST + "add/";
    public static String EDIT_ENDPOINT = HOST + "edit/";
    public static String DELETE_ENDPOINT = HOST + "delete/";
    public static String DATA_ENDPOINT = HOST + "data/";
    public static String PASSWORD = "";
    public static String USER = "";
    public static String UPLOAD_ENDPOINT = HOST + "upload/";
    public static String DOWNLOAD_ENDPOINT = HOST + "download/";
    public static void loadCredentials(Context context){
        Preferences prefs = new Preferences(context);
        HOST = !prefs.getHost().isEmpty() ? prefs.getHost() : HOST;
        PASSWORD = !prefs.getPass().isEmpty() ? prefs.getPass() : PASSWORD;
        USER = !prefs.getUser().isEmpty() ? prefs.getUser() : USER;
        ADD_ENDPOINT = HOST + "add/";
        EDIT_ENDPOINT = HOST + "edit/";
        DELETE_ENDPOINT = HOST + "delete/";
        DATA_ENDPOINT = HOST + "data/";
        UPLOAD_ENDPOINT = HOST + "upload/";
        DOWNLOAD_ENDPOINT = HOST + "download/";
    }

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
                public void onSuccess(int statusCode, String response) {
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
    private static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    public static void uploadImage(Context context, Bitmap bitmap, String UPLOAD_URL, OnRequestSuccessListener listener) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, UPLOAD_URL ,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        listener.onSuccess(200, null);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            listener.onError(error.networkResponse.statusCode);
                        } else {
                            listener.onError(-1);
                        }
                    }
                });

        multipartRequest.addByteData("image", new VolleyMultipartRequest.DataPart("image.jpg", imageData, "image/jpeg"));

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(multipartRequest);
    }

    public static void Request(String url, String id,JSONObject requestBody, Context context, OnRequestSuccessListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url + (id == null || id.isEmpty() ? "": (id + "/"))  + PASSWORD , requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (listener != null) {
                            try {
                                Log.d("RESPONSE",response.getString("clientId"));
                                listener.onSuccess(200, response.getString("clientId"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
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
                                    listener.onSuccess(200, "error");
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
        void onSuccess(int statusCode, String clientId);
        void onError(int statusCode);
    }
    public static void AdjustImageView(ImageView imageView, int width, int height){
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = width;
        params.height = height;
        imageView.setLayoutParams(params);
    }
}