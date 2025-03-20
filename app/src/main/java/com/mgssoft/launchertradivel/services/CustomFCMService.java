package com.mgssoft.launchertradivel.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mgssoft.launchertradivel.Registro;

import org.json.JSONException;
import org.json.JSONObject;

public class CustomFCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.i("Notificacion Recibida","Start Activity");
        startActivity(new Intent(this, Registro.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i("Nuevo Token",token);
        SharedPreferences sharedPreferences = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("token", token)
                .putBoolean("cambioToken", true).apply();

        //TODO ELIMINAR
        //Toast.makeText(this,"NUEVO TOKEN",Toast.LENGTH_SHORT).show();

        String dni = sharedPreferences.getString("dni", "");
        if (!dni.equals("")) {
            //TODO DESCOMENTAR
            String url = String.format("http://%1$s:%2$s/notification/register", "portal.afinsoftware.com", "443");

            RegistrationData registrationData = new RegistrationData();
            registrationData.token = token;
            registrationData.dni = dni;

            Gson gson = new GsonBuilder().setLenient().create();
            JSONObject data = null;
            try {
                data = new JSONObject(gson.toJson(registrationData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> Log.i("notificacion", String.format("Registrado: %s", response)),
                    error -> Log.e("notificacion", String.format("ERROR: %s", error.toString())));

            Volley.newRequestQueue(this).add(jsonRequest);
        }
    }
}
