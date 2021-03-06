package com.example.androidscript.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import com.example.androidscript.FloatingWidget.FloatingWidgetService;
import com.example.androidscript.R;
import com.example.androidscript.util.AutoClick;
import com.example.androidscript.util.DebugMessage;
import com.example.androidscript.util.ScreenShot;

import java.util.ArrayList;
import java.util.List;

public class StartService extends AppCompatActivity {
    public static final int PROJECTION_REQUEST_CODE = 123;
    public static final int FOREGROUND_REQUEST_CODE = 111;
    public static boolean SERVICE_STARTED = false;
    private MediaProjectionManager mediaProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_service);
        ScreenShot.setShotOrientation(getIntent().getStringExtra("Orientation").equals("Landscape"));

        if (!Settings.canDrawOverlays(getApplicationContext())) {//Floating Widget
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
        }
        //AutoClick
        try {
            if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED) == 0) {
                this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));//Get permission
            }
        } catch (Settings.SettingNotFoundException e) {
            DebugMessage.printStackTrace(e);
        }

        //ScreenShot, need to be foreground.(The rest parts are inside its class and onActivityResult.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            List<String> requestedPermissions = new ArrayList<>();
            requestedPermissions.add(Manifest.permission.FOREGROUND_SERVICE);//Stub to add more permissions.
            String[] requests = new String[requestedPermissions.size()];
            requestPermissions(requests, FOREGROUND_REQUEST_CODE);
        }

        if(!ScreenShot.ServiceStart){
            mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult((mediaProjectionManager).createScreenCaptureIntent(), PROJECTION_REQUEST_CODE);
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROJECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            new Handler().postDelayed(() -> {
                ScreenShot.setUpMediaProjectionManager(data, mediaProjectionManager);
                startService(new Intent(getApplicationContext(), ScreenShot.class));
            }, 1);
        }
        else{
            Toast.makeText(getApplicationContext(), "Authentication failed!", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    public static void StartFloatingWidget(AppCompatActivity appCompatActivity){
        //2022_01_21 move checking to Authorized
        if(SERVICE_STARTED){
            appCompatActivity.startService(new Intent(appCompatActivity, FloatingWidgetService.class));
            appCompatActivity.finishAffinity();

        }
    }

    public static boolean IsAuthorized(AppCompatActivity appCompatActivity){
        Toast.makeText(appCompatActivity.getApplicationContext(), "Please Authorize.", Toast.LENGTH_SHORT).show();
        final boolean DEBUG = false;
        try {
            if (Settings.canDrawOverlays(appCompatActivity.getApplicationContext())) {
                if(ScreenShot.ServiceStart){
                    if(Settings.Secure.getInt(appCompatActivity.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED) > 0 && AutoClick.running()){
                        SERVICE_STARTED = true;
                        Toast.makeText(appCompatActivity.getApplicationContext(), "Authorized", Toast.LENGTH_LONG).show();
                        return true;
                    }
                    else if(DEBUG) {
                        Toast.makeText(appCompatActivity.getApplicationContext(), "Need Accessibility Enabled!", Toast.LENGTH_LONG).show();
                    }
                }else if(DEBUG){
                    Toast.makeText(appCompatActivity.getApplicationContext(), "Can't Capture Screen!", Toast.LENGTH_LONG).show();
                }
            } else if(DEBUG){
                Toast.makeText(appCompatActivity.getApplicationContext(), "Can't Draw Over Layers!", Toast.LENGTH_LONG).show();
            }
        } catch (Settings.SettingNotFoundException e) {
            DebugMessage.printStackTrace(e);
        }
        SERVICE_STARTED = false;
        return false;
    }
}
