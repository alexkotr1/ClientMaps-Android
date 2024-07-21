package com.alexk.clientmap;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {
    private final Response.Listener<NetworkResponse> listener;
    private final Map<String, String> params;
    private final Map<String, DataPart> byteData;
    private final String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";

    public VolleyMultipartRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.listener = listener;
        this.params = new HashMap<>();
        this.byteData = new HashMap<>();
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    public void addByteData(String key, DataPart dataPart) {
        byteData.put(key, dataPart);
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        return getMultipartBody();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    private byte[] getMultipartBody() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            // Add file data
            for (Map.Entry<String, DataPart> entry : byteData.entrySet()) {
                DataPart dataPart = entry.getValue();
                byteArrayOutputStream.write(("--" + boundary + "\r\n").getBytes());
                byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + dataPart.fileName + "\"\r\n").getBytes());
                byteArrayOutputStream.write(("Content-Type: " + dataPart.mimeType + "\r\n\r\n").getBytes());
                byteArrayOutputStream.write(dataPart.content);
                byteArrayOutputStream.write("\r\n".getBytes());
            }

            // Add params
            for (Map.Entry<String, String> entry : params.entrySet()) {
                byteArrayOutputStream.write(("--" + boundary + "\r\n").getBytes());
                byteArrayOutputStream.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes());
                byteArrayOutputStream.write(entry.getValue().getBytes());
                byteArrayOutputStream.write("\r\n".getBytes());
            }

            byteArrayOutputStream.write(("--" + boundary + "--\r\n").getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        listener.onResponse(response);
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String mimeType;

        public DataPart(String fileName, byte[] content, String mimeType) {
            this.fileName = fileName;
            this.content = content;
            this.mimeType = mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getMimeType() {
            return mimeType;
        }
    }
}
