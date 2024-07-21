package com.alexk.clientmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

public class ImageDownloader {

    public interface OnImageDownloadListener {
        void onImageDownloaded(Bitmap bitmap);
        void onError(VolleyError error);
    }

    public static void downloadImage(Context context, String url, final OnImageDownloadListener listener) {
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        ImageRequest imageRequest = new ImageRequest(
                url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        if (listener != null) {
                            listener.onImageDownloaded(response);
                        }
                    }
                },
                0,
                0,
                Bitmap.Config.RGB_565, // image config
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ImageDownloadError", "Error: " + error.getMessage());
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                }
        );

        requestQueue.add(imageRequest);
    }
}
