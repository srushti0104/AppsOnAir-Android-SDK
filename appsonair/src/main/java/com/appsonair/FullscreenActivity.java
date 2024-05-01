package com.appsonair;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class FullscreenActivity extends AppCompatActivity {

    private static final String TAG = "FullscreenActivity";

    private ImageView fullscreenImageView;
    private ImageButton editButton;
    private ImageButton rightArrowButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        fullscreenImageView = findViewById(R.id.fullscreenImageView);
        editButton = findViewById(R.id.editButton);
        rightArrowButton = findViewById(R.id.right_icon);


        // Retrieve image path from Intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("IMAGE_PATH")) {
            final Uri imagePath = intent.getParcelableExtra("IMAGE_PATH");

            // Load the image into the ImageView
            fullscreenImageView.setImageURI(imagePath);

            // Set up click listener for the edit button
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Call the editScreenshotWithPhotoEditor function
                    editScreenshotWithPhotoEditor(FullscreenActivity.this, imagePath);
                }
            });

            rightArrowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateBugReportDialog(imagePath);
                }
            });
        } else {
            // Handle the case where no image path is provided
        }
    }
    private void updateBugReportDialog(Uri imagePath) {
        ScreenshotDetectionDelegate screenshotDelegate = new ScreenshotDetectionDelegate(this);
        screenshotDelegate.updateBugReportDialog(imagePath.toString());
        finish();
    }
    private void editScreenshotWithPhotoEditor(Context context, Uri imageUri) {
        Log.d(TAG, "editScreenshotWithPhotoEditor called");
        try {
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
                finish();
            } else {
                Log.e(TAG, "InputStream is null for URI: " + imageUri);
            }

        } catch (Exception e) {
            Log.e(TAG, "editScreenshotWithPhotoEditor " + e);
            e.printStackTrace();
        }
    }
}