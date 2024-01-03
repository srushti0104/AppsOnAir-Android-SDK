package com.appsonair;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import java.util.Objects;
import androidx.core.content.ContextCompat;
import android.app.Activity;
import androidx.core.app.ActivityCompat;
import java.io.InputStream;
import java.io.File;


    public class ScreenshotDetectionDelegate {

    private final Context context;
//    private final ScreenshotDetectionListener listener;
    private ContentObserver contentObserver;

    private boolean isListening = false;
    private String previousPath = "";
    private boolean isDialogOpen = false;
    private static final String TAG = "DetectorModule";


    public ScreenshotDetectionDelegate(Context context) {
        this.context = context;
    }

    public void startScreenshotDetection() {
        if (!isReadMediaPermissionGranted()) {
            // If permission is not granted, request it

            requestReadMediaPermission();
        }
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                if (isReadMediaPermissionGranted() && uri != null) {
                    String path = getFilePathFromContentResolver(context, uri);
                    if (path != null && isScreenshotPath(path)) {
                        previousPath = path;
                        onScreenCaptured(path);
                    }
                }
                else {
                    onScreenCapturedWithDeniedPermission();
                }
            }
        };

        context.getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
        );
        isListening = true;
    }

    private void requestReadMediaPermission() {
        // Request permission from the user
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions((Activity) context, permissions, REQUEST_MEDIA_PERMISSION);
        }
    }

    // Add this constant at the beginning of the class
    private static final int REQUEST_MEDIA_PERMISSION = 123; // Use any unique value

    // Override onRequestPermissionsResult in your activity or fragment to handle the result


    public void stopScreenshotDetection() {
        context.getContentResolver().unregisterContentObserver(Objects.requireNonNull(contentObserver));
        isListening = false;
    }

    private void onScreenCaptured(String path) {
        Log.d(TAG, "onScreenCaptured called"); // Log this line

        // Show native modal only if the dialog is not already open
        if (!isDialogOpen) {
//            showNativeModal(path);
            editScreenshotWithPhotoEditor(context,path);
        }

        // Open and edit the screenshot with PhotoEditor
    }

    private void onScreenCapturedWithDeniedPermission() {
        if (!isDialogOpen) {
            showNativeModal("Permission denied for reading media images");
        }    }

    private boolean isScreenshotPath(String path) {
        return path != null && path.toLowerCase().contains("screenshots") && !previousPath.equals(path);
    }

        private void editScreenshotWithPhotoEditor(Context context, String imagePath) {
            Log.d(TAG, "editScreenshotWithPhotoEditor called");
            try {
                // Create a File object from the imagePath
                File imageFile = new File(imagePath);

                // Convert the File to a content URI
                Uri imageUri = Uri.fromFile(imageFile);

                // Use ContentResolver to open the input stream
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);

                if (inputStream != null) {
                    // If the inputStream is not null, proceed to open the EditImageActivity
                    Intent intent = new Intent(context, EditImageActivity.class);
                    intent.setAction(Intent.ACTION_EDIT);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    intent.setDataAndType(imageUri, "image/*");
                    context.startActivity(intent);
                } else {
                    Log.e(TAG, "InputStream is null for URI: " + imageUri);
                }

            } catch (Exception e) {
                Log.e(TAG, "editScreenshotWithPhotoEditor " + e);
                e.printStackTrace();
            }
        }
    private String getFilePathFromContentResolver(Context context, Uri uri) {
        try {
            String[] projection = {
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA
            };
            android.database.Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                cursor.close();
                return path;
            }
        } catch (Exception e) {
            // Handle the exception appropriately
        }
        return null;
    }

    private boolean isReadMediaPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String readMediaPermission = "android.permission.READ_MEDIA_IMAGES";
            return ContextCompat.checkSelfPermission(context, readMediaPermission) == PackageManager.PERMISSION_GRANTED;
        } else {
            // For versions prior to Android 6, permissions are granted at install time
            return true;
        }
    }

    public void setDialogOpen(boolean bool){
        this.isDialogOpen =bool;
    }

    private void showNativeModal(final String path) {
        isDialogOpen = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Screenshot Captured")
                .setMessage("Screenshot captured at path: " + path)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        isDialogOpen = false; // Reset the flag when the dialog is dismissed
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
