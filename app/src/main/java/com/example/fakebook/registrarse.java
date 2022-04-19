package com.example.fakebook;

import android.content.Context;
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


import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class registrarse extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);
        /*
        Al igual que en la pantalla de login, el boton de registrarse solo se activara cuando ambos campos
        tengan valores validos
        Ver los comentarios que hay en la clase login para saber mas sobre como funciona
         */
        Button botonLogin = (Button) findViewById(R.id.crearCuenta);
        botonLogin.setEnabled(false);
        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
        textUsuario.addTextChangedListener(watcher);
        textContraseña.addTextChangedListener(watcher);


    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { //ver metodo de mismo nombre en clase login
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private final TextWatcher watcher = new TextWatcher() {//ver metodo de mismo nombre en clase login
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {}
        @Override
        public void afterTextChanged(Editable s) {
            EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
            EditText textContraseña = (EditText)findViewById(R.id.editTextPassword);
            Button botonRegistro = (Button)findViewById(R.id.crearCuenta);
            if (textContraseña.getText().toString().length()==0||textUsuario.getText().toString().length()==0) {
                botonRegistro.setEnabled(false);
            } else {
                botonRegistro.setEnabled(true);
            }
        }
    };

    public void registrar(View view) throws ExecutionException, InterruptedException {
        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        String usuario = textUsuario.getText().toString();

        EditText textContra = (EditText)findViewById(R.id.editTextPassword);
        String contra = textContra.getText().toString();
        boolean exito=enviar(usuario, contra); //intentamos crear el usuario

        Context context = getApplicationContext();
        CharSequence text;
        if(exito){ //y mostramos el mensaje correspondiente al resultado
            text = "Usuario creado correctamente";
        }else{
            text = "No se ha podido crear el usuario";
        }

        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

    }
    public Boolean enviar(String usuario, String contra) throws ExecutionException, InterruptedException {


        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                URL url = new URL("http://sanjuesc.xyz:8888/user/add"); //uri donde hay que hacer la peticion
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT"); //tipo de peticion
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type","application/json"); //enviaremos un json
                connection.setRequestProperty("Accept", "application/json");
                String payload = "{\"usuario\":\""+usuario+"\", \"pass\":\""+contra+"\"}"; //el json de la peticion
                Log.d("aaa", payload);
                byte[] out = payload.getBytes(StandardCharsets.UTF_8);
                OutputStream stream = connection.getOutputStream();
                stream.write(out);
                Log.d("aaa",connection.getResponseCode() + " " + connection.getResponseMessage());
                connection.disconnect();
                return connection.getResponseCode()==200; //devolvemos True si la respuesta es 200 OK
            }
        };
        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable); //lo ejecutamos en un hilo nuevo para no bloquear el de la aplicacion
        return future.get();
    }


}
