package com.example.fakebook;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;


import java.util.concurrent.ExecutionException;

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

    public void registrarse(View view) throws ExecutionException, InterruptedException {
        EditText textUsuario = (EditText)findViewById(R.id.editTextUsuario);
        String usuario = textUsuario.getText().toString();

    }

}
