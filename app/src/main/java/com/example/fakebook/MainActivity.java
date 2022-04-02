package com.example.fakebook;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private Boolean isFABOpen;
    private FloatingActionButton fab ;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        closeFABMenu();
        fab.bringToFront();
        fab.setOnClickListener(view -> {
            if(!isFABOpen){
                showFABMenu();
            }else{
                closeFABMenu();
            }
        });


    }

    private void showFABMenu(){ //controlamos como se abre el boton de la esquina inferior derecha
        isFABOpen=true;
        fab1.setClickable(true); //hacemos que los botones que aparecen sean clicables
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.setClickable(true);
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));

    }

    private void closeFABMenu(){ //controlamos como se cierra el boton de la esquina inferior derecha
        isFABOpen=false;
        fab1.animate().translationY(0);
        fab1.setClickable(false); //hacemos que no se puedan clicar los botones que se ocultan
        fab2.animate().translationY(0);
        fab2.setClickable(false);
    }


}