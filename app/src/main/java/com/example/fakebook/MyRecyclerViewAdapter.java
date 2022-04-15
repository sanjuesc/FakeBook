package com.example.fakebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Request;
import okhttp3.Response;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<elemento> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    //metodo constructor estandar
    MyRecyclerViewAdapter(Context context, List<elemento> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    //"infla" el layout cuando es necesario
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    //Une la informacion al textview correspondiente de su linea (position)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        elemento actual = mData.get(position);
        holder.myTextView.setText(actual.autor);

        try{
            cargarImagen(actual.uri, holder.myImageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    // Numero total de lineas (elementos)
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // Guarda y recicla las views a medida que se hace scroll por la aplicacion
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ImageView myImageView;
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.info_text);
            myImageView= itemView.findViewById(R.id.imageView2);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    //Con esto logramos la informacion de la linea id
    elemento getItem(int id) {
        return mData.get(id);
    }

    // añadimos el listener
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //se implementara este metodo para poder detectar y responder a los clics
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


    public Boolean cargarImagen(String nombre, ImageView imageView) throws ExecutionException, InterruptedException { //FUNCIONAAAAAAAAAAAAAAAA


        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                URL url = new URL("http://sanjuesc.xyz/algo/"+nombre+".jpg");
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                imageView.setImageBitmap(bmp);

                return true;
            }
        };

        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(callable);

        return future.get();
    }


}