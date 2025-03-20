package com.mgssoft.launchertradivel;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private View progressBar;

    private final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 987;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.progressBar);

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<String> itemsPermission = new LinkedList<>(
                    Arrays.asList(
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.WAKE_LOCK,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                itemsPermission.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

            Dexter.withActivity(this)
                    .withPermissions(itemsPermission)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                checkPermissionManageOverlay();
                            } else {
                                Snackbar.make(progressBar, "Es necesario dar permisos a la aplicación", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Aceptar", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                checkPermissions();
                                            }
                                        }).show();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();
        } else
            checkPermissionManageOverlay();
    }

    private void checkPermissionManageOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        } else
            initApp();
    }

    private void initApp() {

        Pair<Boolean, String> isPlayServicesAvailable = isPlayServicesAvailable();

        if (!isPlayServicesAvailable.first) {
            AlertDialog.Builder alertDialogGoogleService = new AlertDialog.Builder(this);
            alertDialogGoogleService.setTitle("Atención");
            alertDialogGoogleService.setMessage("Es necesario instalar los Servicios de Google");

            alertDialogGoogleService.setPositiveButton("Salir", (dialog, which) -> {
                if (!BuildConfig.DEBUG) {
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                }

                finish();
            });
            alertDialogGoogleService.show();
        } else {
/*
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
                String newToken = instanceIdResult.getToken();
                Log.i("newToken", newToken);
                getSharedPreferences("LAUNCHERTRADIVEL", Context.MODE_PRIVATE)
                        .edit()
                        .putString("token", newToken)
                        .putBoolean("cambioToken", true)
                        .apply();

            });
            */
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private Pair<Boolean, String> isPlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        String message;
        boolean isAvailable;

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode))
                message = googleApiAvailability.getErrorString(resultCode);
            else
                message = "Dispositivo no soportado";

            isAvailable = false;
        } else {
            message = "Google Play Services disponible";
            isAvailable = true;
        }

        return new Pair<>(isAvailable, message);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!Settings.canDrawOverlays(this)) {
                    //Permiso denegado
                    Snackbar.make(progressBar, "Es necesario dar permisos a la aplicación", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Aceptar", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    checkPermissionManageOverlay();
                                }
                            }).show();
                } else
                    initApp();
            }
        }
    }

}
