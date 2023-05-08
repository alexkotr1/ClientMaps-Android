package com.alexk.clientmap;

import android.location.Location;

public interface LocationUtilitiesListener {
    void onLocationReceived(Location location);
}