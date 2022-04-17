package com.example.fakebook;

public class elemento {
    public String uri;
    public String autor;
    public String lati;
    public String longi;
    public String fecha;
    public String hora;


    public elemento(String uri, String autor, String lati, String longi, String fecha) {
        this.uri = uri;
        this.autor = autor;
        this.lati = lati;
        this.longi = longi;
        this.fecha = fecha.split("T")[0];
        this.hora = fecha.split("T")[1].split("\\.")[0];
    }
}
