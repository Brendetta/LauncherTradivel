package com.mgssoft.launchertradivel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
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
import com.mgssoft.launchertradivel.services.RegistrationData;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String synergyPackage = "com.exact.synergy";
    private ImageButton ibLaunch, ibCheck;
    private View progress;
    private EditText etUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ibLaunch = findViewById(R.id.ibLaunch);
        ibLaunch.setImageDrawable(getDrawable(R.drawable.ic_launcher_button));
        progress = findViewById(R.id.progressBar);
        ibCheck = findViewById(R.id.checkUser);
        etUsuario = findViewById(R.id.etUsuario);

        ibCheck.setOnClickListener(view -> verificarUsuario());

        setButtons();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setButtons();
    }

    private void verificarUsuario() {
        String dni = etUsuario.getText().toString();
        if (dni.isEmpty()) {
            Toast.makeText(this, "Ingrese un DNI.", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE);
        sharedPreferences.edit().putString("dni", dni).apply();

        //Construye la URL para registrar el nuevo token en el servidor.
        //Según configuración actual se genera la URL ""http://intranet.tradivel.com:81/notification/register"
        //***Recordatorio***, si se cambia la IP o el dominio en elgún momento, se debe cambiar también en network_security_config.xml
        String url = "http://intranet.tradivel.com:81/notification/register";
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

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    Log.i("notificacion", "Registrado: " + response);
                    progress.setVisibility(View.GONE);
                    ibCheck.setImageResource(R.drawable.ic_check_green);
                    sharedPreferences.edit().putBoolean("cambioToken", false).apply();
                    setButtons(); //Vuelve a validar estado de botones.
                    startActivity(new Intent(this, Registro.class));
                },
                error -> {
                    Log.e("notificacion", "ERROR: " + error);
                    Toast.makeText(this, "Error al registrar usuario, compruebe el DNI.", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                    ibCheck.setImageResource(R.drawable.ic_check_red);
                    sharedPreferences.edit().putBoolean("cambioToken", true).apply();
                    setButtons(); //Deshabilita ibLaunch si el DNI es inválido.
                });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    private void setButtons() {
        SharedPreferences preferences = getSharedPreferences("LAUNCHERTRADIVEL", MODE_PRIVATE);
        etUsuario.setText(preferences.getString("dni", ""));

        boolean usuarioValido = !preferences.getBoolean("cambioToken", true);

        if (usuarioValido) {
            habilitarLanzamiento();
        } else {
            deshabilitarLanzamiento();
        }
    }

    private void habilitarLanzamiento() {
        ibCheck.setImageResource(R.drawable.ic_check_green);
        ibLaunch.clearColorFilter();
        ibLaunch.setOnClickListener(view -> openSynergyApp());
    }

    private void deshabilitarLanzamiento() {
        ibCheck.setImageResource(R.drawable.ic_check_red);
        ibLaunch.setColorFilter(Color.argb(255, 100, 100, 100), PorterDuff.Mode.MULTIPLY);
        ibLaunch.setOnClickListener(view ->
                Toast.makeText(this, "Es necesario registrar un usuario válido.", Toast.LENGTH_SHORT).show()
        );
    }

    private void openSynergyApp() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(synergyPackage);
        if (intent != null) {
            startActivity(intent);
        } else {
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + synergyPackage));
            marketIntent.setPackage("com.android.vending");
            startActivity(marketIntent);
        }
    }
}
