package com.opd.therament.utilities;

import android.content.Context;
import android.net.NetworkInfo;

public class ConnectivityManager {

    public Boolean checkConnectivity(Context context) {

        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        boolean isConnected = false;

        if (activeNetwork != null) {

            if (activeNetwork.isConnected()) {
                isConnected = activeNetwork.isConnected();
            }
        }

        return isConnected;
    }
}
