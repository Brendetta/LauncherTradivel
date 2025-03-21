package com.mgssoft.launchertradivel.services;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;


/**
Servicio que gestiona la obtención de la ubicación del usuario mediante GPS o red.
*/

public class LocationService extends Service implements LocationListener {

    private final Context mContext;

    //Flag para verificar si el GPS está habilitado.
    private boolean isGPSEnabled = false;

    //Flag para verificar si la red está habilitada.
    private boolean isNetworkEnabled = false;

    //Flag para indicar si es posible obtener la ubicación.
    private boolean canGetLocation = false;

    private Location location; //Ubicación actual.
    private double latitude; //Latitud actual.
    private double longitude; //Longitud actual.

    //Distancia mínima para actualizar la ubicación (en metros)(actualmente no se usa, la llamada está deshabilitada/comentada, pero podría implementarse)
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    //Tiempo mínimo para actualizar la ubicación (en milisegundos)(actualmente no se usa, la llamada está deshabilitada/comentada, pero podría implementarse)
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    //Administrador de ubicación.
    protected LocationManager locationManager;


    /**
     * Constructor de la clase.
     * @param context Contexto de la aplicación.
     */
    public LocationService(Context context) {
        this.mContext = context;
        getLocation();
    }


    //Obtiene la ubicación actual del usuario utilizando GPS o red.
    //@return Objeto Location con la ubicación actual.
    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            //Verifica si el GPS está habilitado.
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //Verifica si la red está habilitada.
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                //Ningún proveedor de ubicación está habilitado.
            } else {
                //Obtiene la ubicación a través de la red si está disponible.
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        /*locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                         */

                        //Solicita una actualización de ubicación a través de la red.
                        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this,null);
                        Log.d("Network", "Network");

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }

                //Si el GPS está habilitado, intenta obtener la ubicación a través de él.
                if (isGPSEnabled) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            /*locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                             */

                            //Solicita una actualización de ubicación a través del GPS.
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,null);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }

                canGetLocation = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Detiene las actualizaciones de GPS.
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationService.this);
        }
    }


    //Obtiene la latitud actual.
    //@return Latitud en formato double.
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    //Obtiene la longitud actual.
    //@return Longitud en formato double.
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    //Verifica si se puede obtener la ubicación.
    //@return true si se puede obtener la ubicación, false en caso contrario.
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    //Muestra un cuadro de diálogo para habilitar el GPS en la configuración del dispositivo
    //Deshabilitado/no se usa aunque se puede implementar.
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        //Configura el título y el mensaje del cuadro de diálogo.
        alertDialog.setTitle("Configuración de GPS");
        alertDialog.setMessage("El GPS no está habilitado. ¿Quieres ir al menú de ajustes?");

        //Botón para ir a la configuración.
        alertDialog.setPositiveButton("Ajustes", (dialogInterface, i) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
        });

        //Botón para cancelar.
        alertDialog.setNegativeButton("Cancelar", (dialogInterface, i) -> {
        });

        //Muestra el cuadro de diálogo.
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        //Se llama cuando cambia la ubicación (implementación vacía por ahora).
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Se llama cuando un proveedor de ubicación se desactiva.
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Se llama cuando un proveedor de ubicación se activa.
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Se llama cuando el estado del proveedor cambia (obsoleto en nuevas versiones de Android).
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null; //No se usa ya que esta clase no es un servicio vinculado.
    }

}
