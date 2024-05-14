package com.alexk.clientmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class NetworkListener extends BroadcastReceiver {

    private static final String TAG = NetworkListener.class.getSimpleName();
    private final Listener listener;

    public NetworkListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                boolean isInternet = capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
                listener.onNetworkChange(isInternet);
            } else {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                listener.onNetworkChange(networkInfo != null && networkInfo.isConnected());
            }
        } else {
            listener.onNetworkChange(false);
        }
    }

    public interface Listener {
        void onNetworkChange(boolean isInternet);
    }
}
