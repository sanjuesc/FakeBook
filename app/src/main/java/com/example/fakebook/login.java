package com.example.fakebook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.messaging.FirebaseMessaging;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class login extends AppCompatActivity {
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button botonLogin = findViewById(R.id.botonLogin);
        botonLogin.setEnabled(false); //Obtenemos el boton y lo deshabilitamos para que no se pueda hacer login
                                      // (mas adelante lo habilitaremos)

        EditText textUsuario = findViewById(R.id.editTextUsuario);
        EditText textContraseña = findViewById(R.id.editTextPassword);

        textUsuario.addTextChangedListener(watcher); //obtenemos el campo del usuario y de la contraseña y los añadimos
        textContraseña.addTextChangedListener(watcher); //un listener para que cuando ambos campos sean validos
                                                        //se habilite el boton de login
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("yoyo", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    token = task.getResult();
                    // Log and toast
                    Log.d("yoyo", token);
                    Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
                });

    }



    public void comprobarCredenciales(View v) throws ExecutionException, InterruptedException {
        EditText textUsuario = findViewById(R.id.editTextUsuario);
        String usuario = textUsuario.getText().toString();
        EditText textContraseña = findViewById(R.id.editTextPassword);
        String contra = textContraseña.getText().toString(); //obtenemos el usuario y la contraseña introducidas
        if(enviar(usuario, contra)){
            Intent i = new Intent(login.this, menu_principal.class);
            i.putExtra("usuario", usuario);
            finish(); //cerramos esta actividad
            startActivity(i); //y empezamos la nueva
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //este codigo se encarga de cerrar el teclado cuando clicamos fuera de un cuadro de texto
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private final TextWatcher watcher = new TextWatcher() { //El listener que hemos puesto antes a ambos campos de texto
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) { //si algun campo de texto es cambiado comprobaremos si los valores son validos
            EditText textUsuario = findViewById(R.id.editTextUsuario);
            EditText textContraseña = findViewById(R.id.editTextPassword);
            Button botonLogin = findViewById(R.id.botonLogin);
            if (textContraseña.getText().toString().length()==0||textUsuario.getText().toString().length()==0) {
                //en este caso solo hemos comprobado si su longitud es mayor de 0, pero podriamos poner que el usuario tenga un @
                // o una longitud minima si quisieramos
                botonLogin.setEnabled(false);
            } else {
                botonLogin.setEnabled(true); //si la longitud era mayor que 0, habilitamos el boton de iniciar sesion
            }
        }
    };




    public void abrirRegistrarse(View view){
        Intent i = new Intent(login.this, registrarse.class);
        startActivity(i); //abrimos la actividad de registrarse
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); //y cambiamos la animacion con la que se abre
    }

    public Boolean enviar(String usuario, String contra) throws ExecutionException, InterruptedException {


        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                URL url = new URL("http://sanjuesc.xyz:8888/user/login");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestProperty("Accept", "application/json");
                String payload = "{\"usuario\":\""+usuario+"\", \"pass\":\""+contra+"\", \"registrationToken\":\""+token+"\"}";// This should be your json body i.e. {"Name" : "Mohsin"}
                Log.d("aaa", payload);
                byte[] out = payload.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = connection.getOutputStream();
                stream.write(out);
                Log.d("aaa",connection.getResponseCode() + " " + connection.getResponseMessage()); // THis is optional
                connection.disconnect();
                return connection.getResponseCode()==200;
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


}

