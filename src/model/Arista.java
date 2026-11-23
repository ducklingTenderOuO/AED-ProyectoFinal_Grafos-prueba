package model;

import java.util.Objects;

public class Arista implements Comparable<Arista> {
    private int origen;
    private int destino;
    private double peso;

    public Arista(int origen, int destino, double peso) {
        this.origen = origen;
        this.destino = destino;
        this.peso = peso;
    }

    public int getOrigen() {
        return origen;
    }

    public void setOrigen(int origen) {
        this.origen = origen;
    }

    public int getDestino() {
        return destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    @Override
    public int compareTo(Arista otra) {
        return Double.compare(this.peso, otra.peso);
    }

    public int compararPorVertice(Arista otra) {
        int compOrigen = Integer.compare(this.origen, otra.origen);
        if (compOrigen != 0) return compOrigen;
        return Integer.compare(this.destino, otra.destino);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arista arista = (Arista) o;
        return origen == arista.origen && destino == arista.destino;
    }

    @Override
    public int hashCode() {
        return Objects.hash(origen, destino);
    }

    @Override
    public String toString() {
        return origen + " -> " + destino + " (" + peso + ")";
    }
}
