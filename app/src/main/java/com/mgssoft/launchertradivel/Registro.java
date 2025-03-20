package com.mgssoft.launchertradivel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mgssoft.launchertradivel.services.LocationData;
import com.mgssoft.launchertradivel.services.LocationService;

import org.json.JSONException;
import org.json.JSONObject;

public class Registro extends AppCompatActivity {

    final Context context = this;
    TextView textView;
    Button button;
    ProgressBar progressBar;

    private PowerManager.WakeLock wakeLock;
    private LocationService gps;
    private double latitudeCurrentPosition;
    private double longitudeCurrentPosition;

    @SuppressLint({"MissingPermission", "InvalidWakeLockTag"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakelockTag");

        textView = findViewById(R.id.tvRegistro);
        button = findViewById(R.id.btAceptar);
        progressBar = findViewById(R.id.progressBar);

        button.setOnClickListener(view -> onBackPressed());

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        try {
            //region Nuevo
            wakeLock.acquire();

            gps = new LocationService(context);
            if (gps.canGetLocation()) { // gps enabled} // return boolean true/false
                latitudeCurrentPosition = gps.getLatitude(); // returns latitude
                longitudeCurrentPosition = gps.getLongitude(); // returns longitude

                //TODO DESCOMENTAR
                String url = String.format("http://%1$s:%2$s/notification/registerLocation", "portal.afinsoftware.com", "443");

                LocationData locationData = new LocationData();
                locationData.dni = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE).getString("dni", "");

                locationData.lat = latitudeCurrentPosition;
                locationData.lon = longitudeCurrentPosition;

                Gson gson = new GsonBuilder().setLenient().create();
                JSONObject data = null;
                try {
                    data = new JSONObject(gson.toJson(locationData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                        response -> {
                            Log.i("notificacion", String.format("Localizado: %s", response));
                            changeView();
                        },
                        error -> {
                            Log.e("notificacion", String.format("ERROR: %s", error.toString()));
                            changeView();
                        }
                );

                Volley.newRequestQueue(context).add(jsonRequest);

            }

            wakeLock.release();
            //endregion Nuevo

            //region Antiguo
/*
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            final LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null && location.getLatitude() != 0 && location.getLongitude() != 0) {
                        //TODO ELIMINAR
                        //changeView();
                        //Toast.makeText(context,"onLocationChanged",Toast.LENGTH_SHORT).show();


                        //TODO DESCOMENTAR
                        String url = String.format("http://%1$s:%2$s/notification/registerLocation", "195.81.223.157", "81");

                        LocationData locationData = new LocationData();
                        locationData.dni = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE).getString("dni", "");

                        locationData.lat = location.getLatitude();
                        locationData.lon = location.getLongitude();

                        Gson gson = new GsonBuilder().setLenient().create();
                        JSONObject data = null;
                        try {
                            data = new JSONObject(gson.toJson(locationData));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                                response -> {
                                    Log.i("notificacion", String.format("Localizado: %s", response));
                                    changeView();
                                },
                                error -> {
                                    Log.e("notificacion", String.format("ERROR: %s", error.toString()));
                                    changeView();
                                }
                        );

                        Volley.newRequestQueue(context).add(jsonRequest);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (locationManager != null) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }

 */
            //endregion Antiguo
        } catch (Exception e) {
            e.printStackTrace();
            changeView();
        }
        finally {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void changeView() {
        textView.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}
