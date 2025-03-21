package com.mgssoft.launchertradivel.services;


/**
* Clase que representa los datos necesarios para registrar un dispositivo en el sistema de notificaciones.
*/
public class RegistrationData {
    //Token de notificación del dispositivo, proporcionado por Firebase.
    public String token = "";

    //DNI del usuario asociado al dispositivo.
    public String dni = "";
}
