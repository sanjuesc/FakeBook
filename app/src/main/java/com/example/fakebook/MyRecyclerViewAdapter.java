package com.example.fakebook;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

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
        holder.fecha.setText(actual.fecha);
        holder.hora.setText(actual.hora);
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.cardView.getContext(), R.color.fondo));
        Random random = new Random();
        switch (random.nextInt(6 - 1 + 1) + 1){
            case 1:
                holder.avatar.setImageResource(R.drawable.ic_avatar1_foreground);
                break;
            case 2:
                holder.avatar.setImageResource(R.drawable.ic_avatar2_foreground);
                break;
            case 3:
                holder.avatar.setImageResource(R.drawable.ic_avatar3_foreground);
                break;
            case 4:
                holder.avatar.setImageResource(R.drawable.ic_avatar4_foreground);
                break;
            case 5:
                holder.avatar.setImageResource(R.drawable.ic_avatar5_foreground);
                break;
            default:
                holder.avatar.setImageResource(R.drawable.ic_avatar1_foreground);
        }
        try{
            cargarImagen(actual.uri, holder.myImageView, position);

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
        TextView fecha;
        TextView hora;
        CardView cardView;
        ImageView avatar;
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.info_text);
            myImageView= itemView.findViewById(R.id.imageView2);
            fecha = itemView.findViewById(R.id.textFecha);
            hora = itemView.findViewById(R.id.textHora);
            cardView = itemView.findViewById(R.id.card_view);
            avatar = itemView.findViewById(R.id.avatar);
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


    public void cargarImagen(String nombre, ImageView imageView, int posicion) throws ExecutionException, InterruptedException { //FUNCIONAAAAAAAAAAAAAAAA
        Bitmap bmp = MisBitmaps.getInstance().getArray().get(posicion);
        imageView.setImageBitmap(bmp);

    }


}