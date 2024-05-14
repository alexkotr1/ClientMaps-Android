package com.alexk.clientmap;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;

public class ClientRequest extends Request<List<Client>> {
    private final Response.Listener<List<Client>> listener;

    public ClientRequest(String url, Response.Listener<List<Client>> listener,
                         Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response<List<Client>> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Gson gson = new Gson();
            Type type = new TypeToken<List<Client>>(){}.getType();
            List<Client> clients = gson.fromJson(jsonString, type);
            return Response.success(clients, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonParseException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(List<Client> response) {
        listener.onResponse(response);
    }
}
