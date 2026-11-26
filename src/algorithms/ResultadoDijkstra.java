package algorithms;

import java.util.List;

public class ResultadoDijkstra {
    private final int origen;
    private final int destino;
    private final List<Integer> ruta;
    private final double costoTotal;
    private final boolean alcanzable;

    public ResultadoDijkstra(int origen, int destino, List<Integer> ruta, double costoTotal, boolean alcanzable) {
        this.origen = origen;
        this.destino = destino;
        this.ruta = ruta;
        this.costoTotal = costoTotal;
        this.alcanzable = alcanzable;
    }

    public int getOrigen() {
        return origen;
    }

    public int getDestino() {
        return destino;
    }

    public List<Integer> getRuta() {
        return ruta;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public boolean isAlcanzable() {
        return alcanzable;
    }

    @Override
    public String toString() {
        if (!alcanzable) {
            return "No existe ruta entre " + origen + " y " + destino;
        }
        return "Ruta: " + ruta + " | Costo total: " + costoTotal;
    }
}
