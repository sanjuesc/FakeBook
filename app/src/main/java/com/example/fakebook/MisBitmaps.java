package com.example.fakebook;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class MisBitmaps { //he creado un singleton de bitmaps por que si no los tengo guardados
    private static MisBitmaps mInstance;
    private ArrayList<Bitmap> list = null;

    public static MisBitmaps getInstance() {
        if(mInstance == null)
            mInstance = new MisBitmaps();

        return mInstance;
    }

    private MisBitmaps() {
        list = new ArrayList<Bitmap>();
    }

    public ArrayList<Bitmap> getArray() {
        return this.list;
    }
    //Add element to array
    public void addToArray(Bitmap value) {
        list.add(Bitmap.createScaledBitmap(value, 3*value.getWidth(), 3*value.getHeight(), false)); //no me termina de gustar lo de escalar la imagen
                                                                                                                          // pero sale muy pequeña sino
    }


}
