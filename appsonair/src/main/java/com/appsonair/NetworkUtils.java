package com.appsonair;// NetworkUtils.java

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkUtils {

    private Context mContext;
    private NetworkStateReceiver mNetworkStateReceiver;
    private NetworkStateChangeListener mListener;

    public NetworkUtils(Context context) {
        mContext = context;
        mNetworkStateReceiver = new NetworkStateReceiver();
    }

    public interface NetworkStateChangeListener {
        void onNetworkStateChanged(boolean isConnected);
    }

    public void setNetworkStateChangeListener(NetworkStateChangeListener listener) {
        mListener = listener;
    }

    public void registerNetworkStateReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetworkStateReceiver, filter);
    }

    public void unregisterNetworkStateReceiver() {
        mContext.unregisterReceiver(mNetworkStateReceiver);
    }

    private class NetworkStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    boolean isConnected = isConnected();
                    if (mListener != null) {
                        mListener.onNetworkStateChanged(isConnected);
                    }
                }
            }
        }

        private boolean isConnected() {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
            return false;
        }
    }
}
