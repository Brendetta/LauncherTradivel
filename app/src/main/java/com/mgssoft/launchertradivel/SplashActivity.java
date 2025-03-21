package com.mgssoft.launchertradivel;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
* Actividad de presentación (Splash) de la aplicación.
* Verifica permisos necesarios y disponibilidad de Google Play Services antes de iniciar la aplicación.
*/
public class SplashActivity extends AppCompatActivity {
    private View progressBar;

    private final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 987;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.progressBar);

        checkPermissions(); //Verificar permisos requeridos.
    }

    //Verifica y solicita los permisos necesarios para la aplicación.
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Lista de permisos requeridos.
            List<String> itemsPermission = new LinkedList<>(Arrays.asList(
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.WAKE_LOCK,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION));

            //Desde Android 10 (API 29) en adelante, se requiere permiso de ubicación en segundo plano.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                itemsPermission.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }

            requestLocationPermission(); //Pedimos ubicación primero.
        } else {
            checkPermissionManageOverlay(); //Si la versión es menor a Android 6, pasamos al siguiente permiso (permiso de overlay).
        }
    }

    //Solicita permiso de ubicación dentro de la app.
    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                checkPermissionManageOverlay(); //Si ya está concedido, pasamos al siguiente permiso.
            }
        }
    }

    //Verifica y solicita el permiso para mostrar superposiciones (ventanas flotantes).
    //En caso de no tener permiso de superposición, se muestra un diálogo antes de redirigir.
    //Este permiso se debe activar manualmente por el usuario de forma obligatoria.
    private void checkPermissionManageOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permiso requerido")
                    .setMessage("Se necesita permiso para mostrar ventanas sobre otras aplicaciones.")
                    .setPositiveButton("Conceder", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        Toast.makeText(this, "No se podrá mostrar contenido flotante sin este permiso", Toast.LENGTH_LONG).show();
                    })
                    .show();
        } else {
            initApp(); //Si ya está concedido, iniciamos la aplicación.
        }
    }

    //Inicializa la aplicación después de que los permisos hayan sido concedidos.
    private void initApp() {
        Pair<Boolean, String> isPlayServicesAvailable = isPlayServicesAvailable();

        if (!isPlayServicesAvailable.first) {
            new AlertDialog.Builder(this)
                    .setTitle("Atención")
                    .setMessage("Es necesario instalar los Servicios de Google")
                    .setPositiveButton("Salir", (dialog, which) -> finish())
                    .show();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    //Verificar Google Play Services está disponible en el dispositivo.
    //@return un par que indica si está disponible y un mensaje informativo.
    private Pair<Boolean, String> isPlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        boolean isAvailable = resultCode == ConnectionResult.SUCCESS;
        String message = isAvailable ? "Google Play Services disponible" : "Dispositivo no soportado";

        return new Pair<>(isAvailable, message);
    }

    //Manejo de respuestas de la solicitud de permisos.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionManageOverlay(); //Ahora pedimos el permiso de superposición.
            } else {
                //Si el usuario no concede el permiso de ubicación, mostramos un Snackbar con opción para reintentar.
                Snackbar.make(progressBar, "Se requiere el permiso de ubicación", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Aceptar", view -> requestLocationPermission())
                        .show();
            }
        }
    }

    //Manejar la respuesta del permiso de superposición.
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    initApp();
                } else {
                    Toast.makeText(this, "El permiso de superposición es necesario", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
