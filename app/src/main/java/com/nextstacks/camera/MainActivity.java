package com.nextstacks.camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FrameLayout mCameraPreview;

    private int cameraID;
    private Camera camera;
    private ImageView mIvPreviewImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView mIvCapture = findViewById(R.id.iv_capture_camera);
        ImageView mIvFlipCamera = findViewById(R.id.iv_flip_camera);
        mIvPreviewImg = findViewById(R.id.iv_img_preview);

        mIvFlipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isBackCamera = cameraID == Camera.CameraInfo.CAMERA_FACING_BACK ? true : false;
                camera.stopPreview();
                startCameraPreview(!isBackCamera);
            }
        });

        mIvCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        Bitmap capturedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        saveImageToDevice(capturedImage);
                    }
                });
            }
        });

        mCameraPreview = findViewById(R.id.camera_container);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 134);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 134) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startCameraPreview(true);
            } else {
                Toast.makeText(MainActivity.this, "User denied permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCameraPreview(boolean isBackCamera) {

        cameraID = isBackCamera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        camera = Camera.open(cameraID);
        CameraSurface cameraSurface = new CameraSurface(MainActivity.this, camera);
        mCameraPreview.addView(cameraSurface);
    }

    private void saveImageToDevice(Bitmap bitmap) {
        File appDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Nextstacks Camera");

        if (!appDirectory.exists()) {
            appDirectory.mkdir();
        }

        File imgFile = new File(appDirectory, "IMG_" + System.currentTimeMillis() + ".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mIvPreviewImg.setImageBitmap(bitmap);
        camera.startPreview();
    }

    private void readImages() {
        Uri imageURI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] proj = new String[]{MediaStore.Images.Media.DATA};

        ArrayList<String> imagePaths = new ArrayList<>();

        Cursor cursor = getApplicationContext().getContentResolver().query(imageURI, proj, null, null, null);

        if (cursor != null) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String image = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                imagePaths.add(image);
            }
        }
    }
}