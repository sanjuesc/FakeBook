package com.example.fakebook;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.*;

public class menu_principal extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {
    private final OkHttpClient httpClient = new OkHttpClient();
    File photoFile;
    String mCurrentPhotoPath;
    Boolean isFABOpen;
    FloatingActionButton fab; //boton para abrir fab
    FloatingActionButton fab1;//nuevo
    FloatingActionButton fab2;//recargar
    String usuario; //nombre del usuario
    ArrayList<elemento> milista; //lista del recyclerview
    MyRecyclerViewAdapter adapter; //adapter


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras(); //recogemos el nombre del usuario
        if (extras != null) {
            usuario = extras.getString("usuario");
        }

        setContentView(R.layout.activity_menu_principal);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2); //recogemos tambien los tres botones fab
        closeFABMenu(); //cerramos el menu
        fab.bringToFront(); //y traemos el boton de abrir el menu al frente (estos dos pasos no son necesarios pero mejor asegurarse de que el menu se crea correctamente)
        fab.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = { //permisos que requiere la aplicación
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (!hasPermissions(this, PERMISSIONS)) { //si no tenemos alguno de ellos lo pedimos
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        milista = new ArrayList<elemento>();
        cargarFotos(); //cargamos las fotos
        RecyclerView recyclerView = findViewById(R.id.recyclerView); //y configuramos el recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, milista);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));


    }

    private void cargarFotos() {
        try {
            MisBitmaps.getInstance().getArray().removeAll(MisBitmaps.getInstance().getArray()); //vaciamos el singleton
            String[] fotos = getLista(); //obtenemos una lista de los nombres de las imagenes

            for(int i = 0; i<fotos.length; i++){

                poblarLista(fotos[i]); //obtenemos los detalles de la imagen
                cargarBitmap(fotos[i]); //obtenmos la imagen (en formato bitmap)
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void recargarFotos(){ //igual que cargarFotos pero vacia el recyclerview
        try {
            MisBitmaps.getInstance().getArray().removeAll(MisBitmaps.getInstance().getArray());
            milista.removeAll(milista);
            String[] fotos = getLista();
            for(int i = 0; i<fotos.length; i++){
                poblarLista(fotos[i]);
                cargarBitmap(fotos[i]);
                System.out.println(milista.size());
            }
            adapter.notifyDataSetChanged(); // y notifica que ha cambiado el dataset para que se cargue de nuevo


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(""); //creamos un dialog para confirmar si se quiere ver la localización de la foto
        builder.setMessage("Se abrira la posición de donde se ha tomado la foto");

        builder.setPositiveButton(getString(R.string.aceptar), (dialogInterface, i) -> {
            elemento actual = adapter.getItem(position);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+actual.lati+","+actual.longi+"?q="+actual.lati+","+actual.longi));
                startActivity(intent); //y abrimos Google Maps sobre la localizacion con un marcador
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> {
            //realmente no hay que hacer nada
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }



    public void nuevo(View view) {//cuando se hace click en el boton de añadir una nueva imagen llamamos al metodo encargado de hacer la foto
        dispatchTakePictureIntent();
    }


    public static boolean hasPermissions(Context context, String... permissions) { //metodo que uso para comprobar si se tienen los permisos que le entran como parametros
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private File createImageFile() throws IOException { //metodo para crear un File desde una foto
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  //nombre
                ".jpg",         // extension
                storageDir      //directorio
        );
        mCurrentPhotoPath = image.getAbsolutePath(); //y guardamos el path para usarlo mas tarde
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // antes de nada nos aseguramos de que exista un intent capaz de hacer una foto
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // creamos el File donde irá la foto
            photoFile = null;
            try {
                photoFile = createImageFile(); //guardamos el valor
            } catch (IOException ex) {
                Log.d("Error", ex.getMessage());
            }
            // Si no hay errores continuamos
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
        if (requestCode == 1 && resultCode == RESULT_OK) { //si el requestCode es 1 (se ha definido antes que el codigo para la foto seria 1)
            Bundle extras = data.getExtras();
            Bitmap laminiatura = (Bitmap) extras.get("data"); //esto lo he sacado de los apuntes de egela, no hay mucho que explicar
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            laminiatura.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] fototransformada = stream.toByteArray();
            String fotoen64 = Base64.encodeToString(fototransformada, Base64.DEFAULT); //obtenemos la imagen y la convertimos a base64
            largeLog("mifotoen64", fotoen64); //como la imagen en base64 tiene muchos caracteres no entra en un solo Log.d
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //si no se tienen los permisos volvemos a pedirlos
                    int PERMISSION_ALL = 1;
                    String[] PERMISSIONS = { //permisos que requiere la aplicación
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.INTERNET,
                            android.Manifest.permission.ACCESS_NETWORK_STATE,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    };
                    if (!hasPermissions(this, PERMISSIONS)) { //si no tenemos alguno de ellos lo pedimos
                        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                    }
                    return;
                }
                Location location = getLastKnownLocation();
                double longitude = location.getLongitude();
                double latitude = location.getLatitude(); //obtenemos las coordenadas de donde se ha sacado la foto
                enviarimagen(usuario, fotoen64, generarTitulo(),getFecha(), String.valueOf(longitude), String.valueOf(latitude)); //se envia todo al servidor
                recargarFotos(); //recagarmos el recyclerview para mostrar la nueva imagen
                enviarNoti(); //una vez un usuario ha subido una foto, se notificará al resto de usuarios
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }




    public static void largeLog(String tag, String content) { //he creado este metodo para poder mostrar por logs Strings de mas de 4000 caracteres, no es necesario realmente
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

                RequestBody formBody = new FormBody.Builder() //enviamos la imagen al servidor junto a otros datos
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


    public String generarTitulo(){ //se genera el nombre de la imagen por ejemplo 18-04-2022_10-00-00ander
        String mytime = getFecha().replace(" ","_").replace(":", "-");
        return mytime+usuario;
    }

    public String getFecha(){ //obtener la fecha con la hora en formato 24H (importante para poder ordenar las imagenes en orden cronologico)
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
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response); //comprobamos si la respuesta es 200 OK
                    /*
                    Obtenemos una lista con los nombres de las imagenes
                    Dividimos esa lista en Strings individuales y creamos un array de java
                     */

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
                        .url("http://sanjuesc.xyz:8888/fotos/"+nombre) //llamada para obtener los detalles de la foto
                        .addHeader("User-Agent", "OkHttp Bot")
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {

                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    jsonObject = new JSONObject(response.body().string().replace("[", "").replace("]", "")); //un poco hack, pero siendo un Json de un solo elemento es la manera mas facil de parsearlo
                    milista.add(new elemento(jsonObject.getString("nombre"), jsonObject.getString("user"), jsonObject.getString("lat"), jsonObject.getString("lon"), jsonObject.getString("fecha")));
                    //añadimos a la lista un nuevo elemento con los detalles de la foto actual
                    return response.isSuccessful();

                }
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


    private Location getLastKnownLocation() { //https://stackoverflow.com/questions/34498147/location-getlatitude-on-a-null-object-reference
        //a veces la localizacion que se obtiene es null (ya que no hay una ultima localización), con este codigo evitamos eso
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


    public Boolean cargarBitmap(String nombre) throws ExecutionException, InterruptedException { //FUNCIONAAAAAAAAAAAAAAAA


        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                URL url = new URL("http://sanjuesc.xyz/algo/"+nombre+".jpg"); //obtenemos la imagen desde la url donde se almacenan las imagenes
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                MisBitmaps.getInstance().addToArray(bmp); //y lo añadimos al singleton
                return true;
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }

    public void enviarNoti() throws ExecutionException, InterruptedException {
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Request request = new Request.Builder()
                        .url("http://sanjuesc.xyz:8888/firebase/notification/"+usuario) //hacemos una peticion http a mi api con el nombre del usuario que ha subido la foto
                        //para que la api envie una notificación al resto de usuarios diciendo que el usuario actual ha subido una foto
                        .addHeader("User-Agent", "OkHttp Bot")
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {

                    System.out.println(response.body().string());
                }
                return true;

            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        future.get();
    }



}