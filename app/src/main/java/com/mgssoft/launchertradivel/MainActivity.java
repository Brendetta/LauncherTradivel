package com.mgssoft.launchertradivel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mgssoft.launchertradivel.services.LocationService;
import com.mgssoft.launchertradivel.services.RegistrationData;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Actividad principal de la aplicación.
 * Permite al usuario ingresar su DNI, registrarlo en el servidor y lanzar una aplicación externa.
 */
public class MainActivity extends AppCompatActivity {

    private static final String synergyPackage = "com.exact.synergy";//"com.exact.synergy.exactess"; //Nombre (nuevo y antiguo) del paquete de la aplicación externa.
    private ImageButton ibLaunch, ibCheck;//Botones para lanzar la app externa y registrar el usuario.
    private View progress;//Indicador de progreso de carga.
    private MainActivity context;
    private EditText etUsuario;//Campo de entrada para el DNI del usuario.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        //Inicialización de vistas.
        ibLaunch = findViewById(R.id.ibLaunch);
        ibLaunch.setImageDrawable(getDrawable(R.drawable.ic_launcher_button));
        progress = findViewById(R.id.progressBar);
        ibCheck = findViewById(R.id.checkUser);
        etUsuario = findViewById(R.id.etUsuario);

        //Configuración del botón de verificación de usuario.
        ibCheck.setOnClickListener(view -> {
            String dni = etUsuario.getText().toString();
            if (!dni.equals("")) {
                progress.setVisibility(View.VISIBLE);
                SharedPreferences sharedPreferences = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE);
                sharedPreferences.edit().putString("dni", dni).apply();

                //TODO ELIMINAR
                /*progress.setVisibility(View.GONE);
                ibCheck.setImageResource(R.drawable.ic_check_green);
                getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE).edit().putBoolean("cambioToken", false).apply();
                setButtons();*/

                //URL para registrar el usuario en el servidor.
                //Según configuración actual se genera la URL ""http://intranet.tradivel.com:81/notification/register"
                //***Recordatorio***, si se cambia la IP o el dominio en algún momento, se debe cambiar también en network_security_config.xml
                String url = String.format("http://%1$s:%2$s/notification/register", "intranet.tradivel.com", "81");

                //Creación de objeto con los datos de registro.
                RegistrationData registrationData = new RegistrationData();
                registrationData.token = sharedPreferences.getString("token", "");
                registrationData.dni = dni;

                Gson gson = new GsonBuilder().setLenient().create();
                JSONObject data = null;
                try {
                    data = new JSONObject(gson.toJson(registrationData));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Enviar solicitud al servidor.
                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                        response -> {
                            Log.i("notificacion", String.format("Registrado: %s", response));
                            progress.setVisibility(View.GONE);
                            ibCheck.setImageResource(R.drawable.ic_check_green);
                            getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE).edit().putBoolean("cambioToken", false).apply();
                            setButtons();
                        },
                        error -> {
                            Log.e("notificacion", String.format("ERROR: %s", error.toString()));
                            Toast.makeText(context, "Error al registrar usuario, compruebe el DNI.", Toast.LENGTH_SHORT).show();
                            progress.setVisibility(View.GONE);
                            ibCheck.setImageResource(R.drawable.ic_check_red);
                            getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE).edit().putBoolean("cambioToken", true).apply();
                        });

                Volley.newRequestQueue(context).add(jsonRequest);
            }
        });

        setButtons();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setButtons();
    }



    //Configura los botones en función del estado de registro del usuario.
    private void setButtons() {
        SharedPreferences preferences = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE);
        etUsuario.setText(preferences.getString("dni", ""));


        if (preferences.getBoolean("cambioToken", true)) {
            //Usuario no registrado o token cambiado.
            ibCheck.setImageResource(R.drawable.ic_check_red);
            ibLaunch.setColorFilter(Color.argb(255, 100, 100, 100), PorterDuff.Mode.MULTIPLY);
            ibLaunch.setOnClickListener(view -> Toast.makeText(context, "Es necesario registrar el usuario primero.", Toast.LENGTH_SHORT).show());
        } else {
            //Usuario registrado correctamente.
            ibCheck.setImageResource(R.drawable.ic_check_green);
            try {
                //Verifica si la aplicación externa está instalada.
                getPackageManager().getApplicationInfo(synergyPackage, 0);
                ibLaunch.clearColorFilter();
                ibLaunch.setOnClickListener(view -> {
                    Intent intent = getPackageManager().getLaunchIntentForPackage(synergyPackage);
                    startActivity(intent);
                });
            } catch (PackageManager.NameNotFoundException e) {
                //Si la app no está instalada, redirige a Google Play para descargarla.
                ibLaunch.setColorFilter(Color.argb(255, 100, 100, 100), PorterDuff.Mode.MULTIPLY);
                ibLaunch.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(String.format("https://play.google.com/store/apps/details?id=%1$s", synergyPackage)));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                });
            }
        }
    }
}
