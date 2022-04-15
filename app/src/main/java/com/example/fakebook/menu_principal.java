package com.example.fakebook;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Hex;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.*;
import okhttp3.internal.http.StatusLine;

public class menu_principal extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {
    private final OkHttpClient httpClient = new OkHttpClient();
    File photoFile;
    Boolean isFABOpen;
    FloatingActionButton fab;
    FloatingActionButton fab1;
    FloatingActionButton fab2;
    String usuario;
    ArrayList<elemento> milista;
    MyRecyclerViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usuario = extras.getString("usuario");
        }

        setContentView(R.layout.activity_menu_principal);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        closeFABMenu();
        fab.bringToFront();
        fab.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        milista = new ArrayList<elemento>();
        cargarFotos();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, milista);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));


    }

    private void cargarFotos() {
        try {
            String[] fotos = getLista();

            for(int i = 0; i<fotos.length; i++){
                poblarLista(fotos[i]);
                System.out.println(milista.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void recargarFotos(){
        try {
            milista.removeAll(milista);
            String[] fotos = getLista();
            for(int i = 0; i<fotos.length; i++){
                poblarLista(fotos[i]);
                System.out.println(milista.size());
            }
            adapter.notifyDataSetChanged();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void nuevo(View view) {
        dispatchTakePictureIntent();
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Log.d("aaa", "bbb");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Intent elIntentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(elIntentFoto, 1);
            }
        }
    }

    private void showFABMenu() { //controlamos como se abre el boton de la esquina inferior derecha
        isFABOpen = true;
        fab1.setClickable(true); //hacemos que los botones que aparecen sean clicables
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.setClickable(true);
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));

    }

    private void closeFABMenu() { //controlamos como se cierra el boton de la esquina inferior derecha
        isFABOpen = false;
        fab1.animate().translationY(0);
        fab1.setClickable(false); //hacemos que no se puedan clicar los botones que se ocultan
        fab2.animate().translationY(0);
        fab2.setClickable(false);
    }


    public void abrirPreferencias(View view) {
        recargarFotos();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap laminiatura = (Bitmap) extras.get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            laminiatura.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] fototransformada = stream.toByteArray();
            String fotoen64 = Base64.encodeToString(fototransformada, Base64.DEFAULT);
            largeLog("mifotoen64", fotoen64);
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location location = getLastKnownLocation();
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                Log.d("enviarimagen", "enviarimagen");
                enviarimagen(usuario, fotoen64, generarTitulo(),getFecha(), String.valueOf(longitude), String.valueOf(latitude));
                recargarFotos();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }




    public static void largeLog(String tag, String content) {
        if (content.length() > 4000) {
            Log.d(tag, content.substring(0, 4000));
            largeLog(tag, content.substring(4000));
        } else {
            Log.d(tag, content);
        }
    } //funciona T.T



    public Boolean enviarimagen(String usuario, String imagenb64, String titulo, String fecha, String longi, String lat) throws ExecutionException, InterruptedException { //FUNCIONAAAAAAAAAAAAAAAA


        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                RequestBody formBody = new FormBody.Builder()
                        .add("usuario", usuario)
                        .add("titulo", titulo)
                        .add("image", imagenb64)
                        .add("fecha", fecha)
                        .add("lat", lat)
                        .add("longi", longi)
                        .build();

                Request request = new Request.Builder()
                        .url("http://sanjuesc.xyz/imagen.php")
                        .addHeader("User-Agent", "OkHttp Bot")
                        .post(formBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                }

                return true;
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    public String generarTitulo(){
        String mytime = getFecha().replace(" ","_").replace(":", "-");
        return mytime+usuario;
    }

    public String getFecha(){
        return (DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString());
    }

    public String[] getLista() throws ExecutionException, InterruptedException {
        Callable<String[]> callable = new Callable<String[]>() {
            @Override
            public String[] call() throws Exception {
                String[] fotos;
                Request request = new Request.Builder()
                        .url("http://sanjuesc.xyz:8888/obtenerImagenes")
                        .addHeader("User-Agent", "OkHttp Bot")
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    fotos = response.body().string().split(",");
                }
                return fotos;

            }
        };

        Future<String[]> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    public Boolean poblarLista(String nombre) throws ExecutionException, InterruptedException { //FUNCIONAAAAAAAAAAAAAAAA


        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                JSONObject jsonObject;
                Request request = new Request.Builder()
                        .url("http://sanjuesc.xyz:8888/fotos/"+nombre)
                        .addHeader("User-Agent", "OkHttp Bot")
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    jsonObject = new JSONObject(response.body().string().replace("[", "").replace("]", ""));
                    System.out.println(jsonObject);
                    milista.add(new elemento(jsonObject.getString("nombre"), jsonObject.getString("user"), jsonObject.getString("lat"), jsonObject.getString("lon")));
                    return response.isSuccessful();
                }
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    @Override
    public void onItemClick(View view, int position) {

    }



    private Location getLastKnownLocation() {
        Location l=null;
        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                l = mLocationManager.getLastKnownLocation(provider);
            }
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}