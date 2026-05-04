package com.example.cryptotracker.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    private NetworkUtils() {
    }

    public static boolean isNetworkAvailable(Context context) {
        // ConnectivityManager не е част от самия Context като поле, но Context ни дава достъп до него чрез Android системата
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); // Android, дай ми системната услуга, която знае за интернет връзките.

        /*
        ConnectivityManager  е обектът, който знае за мрежите:
            има ли активна мрежа;
            какъв тип е;
            има ли internet capability;
            какви са network capabilities.
         */

        if (connectivityManager == null) {
            return false;
        }

        Network activeNetwork = connectivityManager.getActiveNetwork();

        if (activeNetwork == null) {
            return false;
        }

        NetworkCapabilities networkCapabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork);

        if (networkCapabilities == null) {
            return false;
        }

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
