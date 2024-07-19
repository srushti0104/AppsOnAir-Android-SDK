package com.appsonair;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;


import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AppsOnAirServices {

    static String appId;
    static Boolean showNativeUI;

    public static void setAppId(Context context, boolean showNativeUI) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);

            Bundle bundle = appInfo.metaData;
            if (bundle != null) {
                AppsOnAirServices.appId =  bundle.getString("app_id");
                Log.d("App Id", appId);
            } else {
                // Handle case where metadata bundle is null
                Log.d("App Id", "Else");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Handle exception
            Log.d("App ID", "Catch");
        }
//        AppsOnAirServices.appId = appId;
        AppsOnAirServices.showNativeUI = showNativeUI;
    }


    public static void checkForAppUpdate(Context context, UpdateCallBack callback) {
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                String url =  BuildConfig.Base_URL + AppsOnAirServices.appId;
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .method("GET", null)
                        .build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.d("EX:", String.valueOf(e));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        try {
                            if (response.code() == 200) {
                                String myResponse = response.body().string();
                                JSONObject jsonObject = new JSONObject(myResponse);
                                JSONObject updateData = jsonObject.getJSONObject("updateData");
                                boolean isAndroidUpdate = updateData.getBoolean("isAndroidUpdate");
                                boolean isMaintenance = jsonObject.getBoolean("isMaintenance");
                                if (isAndroidUpdate) {
                                    boolean isAndroidForcedUpdate = updateData.getBoolean("isAndroidForcedUpdate");
                                    String androidBuildNumber = updateData.getString("androidBuildNumber");
                                    PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                                    int versionCode = info.versionCode;
                                    int buildNum = 0;

                                    if (!(androidBuildNumber.equals(null))) {
                                        buildNum = Integer.parseInt(androidBuildNumber);
                                    }
                                    boolean isUpdate = versionCode < buildNum;
                                    if (showNativeUI && isUpdate && (isAndroidForcedUpdate || isAndroidUpdate)) {
                                        Intent intent = new Intent(context, AppUpdateActivity.class);
                                        intent.putExtra("res", myResponse);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }
                                } else if (isMaintenance && showNativeUI) {
                                    Intent intent = new Intent(context, MaintenanceActivity.class);
                                    intent.putExtra("res", myResponse);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                } else {
                                    //TODO : There is No Update and No Maintenance.
                                }
                                callback.onSuccess(myResponse);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            callback.onFailure(e.getMessage());
                            Log.d("AAAA", String.valueOf(e.getMessage()));

                        }
                    }
                });
            }

            @Override
            public void onLost(Network network) {
                //Lost connection
            }
        };

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }
}
