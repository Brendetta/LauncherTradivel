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
/**
Servicio personalizado para manejar los mensajes FCM (Firebase Cloud Messaging).
*/

public class CustomFCMService extends FirebaseMessagingService {

    //Se ejecuta cuando se recibe un nuevo mensaje de Firebase.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.i("Notificacion Recibida","Start Activity");

        //Inicia la actividad Registro cuando se recibe una notificación.
        startActivity(new Intent(this, Registro.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    //Se ejecuta cuando se genera un nuevo token FCM.
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i("Nuevo Token",token);

        //Guarda el nuevo token en SharedPreferences.
        SharedPreferences sharedPreferences = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("token", token)
                .putBoolean("cambioToken", true).apply();

        //TODO: ELIMINAR ESTA LÍNEA EN PRODUCCIÓN.
        //Toast.makeText(this,"NUEVO TOKEN",Toast.LENGTH_SHORT).show();

        //Recupera el DNI almacenado en SharedPreferences.
        String dni = sharedPreferences.getString("dni", "");
        if (!dni.equals("")) {

            //Construye la URL para registrar el nuevo token en el servidor.
            //Según configuración actual se genera la URL ""http://intranet.tradivel.com:81/notification/register"
            //***Recordatorio***, si se cambia la IP o el dominio en elgún momento, se debe cambiar también en network_security_config.xml
            String url = String.format("http://%1$s:%2$s/notification/register", "intranet.tradivel.com", "81"); //195.81.223.157 --> IP usada antiguamente.

            //Crea un objeto con los datos de registro.
            RegistrationData registrationData = new RegistrationData();
            registrationData.token = token;
            registrationData.dni = dni;

            //Convierte el objeto a JSON.
            Gson gson = new GsonBuilder().setLenient().create();
            JSONObject data = null;
            try {
                data = new JSONObject(gson.toJson(registrationData));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Crea una solicitud HTTP POST para registrar el token en el servidor.
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                    response -> Log.i("notificacion", String.format("Registrado: %s", response)),
                    error -> Log.e("notificacion", String.format("ERROR: %s", error.toString())));

            //Agrega la solicitud a la cola de peticiones de Volley.
            Volley.newRequestQueue(this).add(jsonRequest);
        }
    }
}
